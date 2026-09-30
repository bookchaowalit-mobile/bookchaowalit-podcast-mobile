package com.bookchaowalit.podcast

import java.util.Locale

data class Episode(val id: String, val title: String, val durationSec: Int, val publishedEpochSec: Long) {
    init { require(durationSec >= 0) { "duration must be >= 0" } }
}

object Durations {
    /**
     * Parses an `itunes:duration` value: plain seconds ("3600"), "MM:SS" or
     * "HH:MM:SS". Returns null for malformed input instead of guessing.
     */
    fun parse(raw: String): Int? {
        val parts = raw.trim().split(':')
        if (parts.isEmpty() || parts.size > 3 || parts.any { it.isEmpty() || !it.all(Char::isDigit) }) return null
        val nums = parts.map { it.toLongOrNull() ?: return null }
        if (nums.size > 1 && nums.drop(1).any { it >= 60 }) return null
        // Bail out as soon as the running total exceeds Int range, before Long can overflow.
        var total = 0L
        for (n in nums) {
            total = total * 60 + n
            if (total > Int.MAX_VALUE) return null
        }
        return total.toInt()
    }

    /** "1:02:03" for >= 1h, otherwise "2:03". */
    fun format(totalSec: Int): String {
        require(totalSec >= 0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) "%d:%02d:%02d".format(Locale.ROOT, h, m, s) else "%d:%02d".format(Locale.ROOT, m, s)
    }
}

/** Per-episode listening progress. */
class ProgressTracker(private val playedThreshold: Double = 0.95) {
    private val positions = HashMap<String, Int>()
    private val played = HashSet<String>()

    fun update(episode: Episode, positionSec: Int) {
        val pos = positionSec.coerceIn(0, episode.durationSec)
        positions[episode.id] = pos
        if (episode.durationSec > 0 && pos >= episode.durationSec * playedThreshold) played += episode.id
    }

    fun position(id: String): Int = positions[id] ?: 0
    fun isPlayed(id: String): Boolean = id in played
    fun markUnplayed(id: String) { played -= id; positions.remove(id) }

    /** Resume point: restart from 0 once played, else the saved position. */
    fun resumeAt(episode: Episode): Int =
        if (isPlayed(episode.id)) 0 else position(episode.id).coerceAtMost(episode.durationSec)

    /** Never negative, even if the feed later reports a shorter duration than the saved position. */
    fun remainingSec(episode: Episode): Int = (episode.durationSec - position(episode.id)).coerceAtLeast(0)
}

/** Seek helper applying skip-back/skip-forward with clamping. */
fun seek(positionSec: Int, deltaSec: Int, durationSec: Int): Int =
    (positionSec.toLong() + deltaSec).coerceIn(0L, durationSec.toLong()).toInt()

/** Play queue with "play next", "add to end", removal and move. */
class PlayQueue {
    private val items = mutableListOf<Episode>()
    var currentIndex: Int = -1
        private set

    val episodes: List<Episode> get() = items.toList()
    val current: Episode? get() = items.getOrNull(currentIndex)

    fun addToEnd(e: Episode) {
        if (items.any { it.id == e.id }) return
        items += e
        if (currentIndex == -1) currentIndex = 0
    }

    /**
     * Puts [e] right after the current episode (moving it if already queued).
     * The current episode never changes; "play next" on the episode that is
     * already playing is a no-op.
     */
    fun playNext(e: Episode) {
        val existing = items.indexOfFirst { it.id == e.id }
        if (existing >= 0 && existing == currentIndex) return
        if (existing >= 0) {
            items.removeAt(existing)
            if (existing < currentIndex) currentIndex--
        }
        val at = if (currentIndex == -1) 0 else currentIndex + 1
        items.add(at.coerceAtMost(items.size), e)
        if (currentIndex == -1) currentIndex = 0
    }

    fun remove(id: String) {
        val i = items.indexOfFirst { it.id == id }
        if (i < 0) return
        items.removeAt(i)
        when {
            items.isEmpty() -> currentIndex = -1
            i < currentIndex -> currentIndex--
            currentIndex >= items.size -> currentIndex = items.lastIndex
        }
    }

    /** Advances; returns the new current episode or null at the end of the queue. */
    fun next(): Episode? {
        if (currentIndex + 1 >= items.size) return null
        currentIndex++
        return current
    }

    fun previous(): Episode? {
        if (currentIndex <= 0) return current
        currentIndex--
        return current
    }

    /** Total seconds left in the queue, starting with the rest of the current episode. */
    fun remainingSec(progress: ProgressTracker): Int =
        items.drop(maxOf(currentIndex, 0)).sumOf { progress.remainingSec(it) }
}
