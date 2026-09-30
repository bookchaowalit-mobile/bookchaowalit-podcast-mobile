package com.bookchaowalit.podcast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PodcastEdgeCaseTest {
    private fun ep(id: String, dur: Int = 600) = Episode(id, "Episode $id", dur, 0)

    private fun queueOf(vararg ids: String) = PlayQueue().apply { ids.forEach { addToEnd(ep(it)) } }

    @Test
    fun playNextOfAnEarlierEpisodeKeepsTheCurrentOne() {
        val q = queueOf("A", "B", "C")
        q.next() // B playing
        q.playNext(ep("A"))
        assertEquals("B", q.current?.id)
        assertEquals(listOf("B", "A", "C"), q.episodes.map { it.id })
        assertEquals("A", q.next()?.id)
    }

    @Test
    fun playNextOfTheCurrentEpisodeIsANoOp() {
        val q = queueOf("A", "B", "C")
        q.next()
        q.playNext(ep("B"))
        assertEquals("B", q.current?.id)
        assertEquals(listOf("A", "B", "C"), q.episodes.map { it.id })
    }

    @Test
    fun playNextOfALaterEpisodeMovesItUp() {
        val q = queueOf("A", "B", "C", "D")
        q.playNext(ep("D"))
        assertEquals("A", q.current?.id)
        assertEquals(listOf("A", "D", "B", "C"), q.episodes.map { it.id })
    }

    @Test
    fun emptyQueueBehaviour() {
        val q = PlayQueue()
        assertNull(q.current)
        assertNull(q.next())
        assertNull(q.previous())
        q.remove("missing")
        assertEquals(0, q.remainingSec(ProgressTracker()))
        q.playNext(ep("X"))
        assertEquals("X", q.current?.id)
        q.remove("X")
        assertEquals(-1, q.currentIndex)
    }

    @Test
    fun addToEndIgnoresDuplicates() {
        val q = queueOf("A", "B")
        q.addToEnd(ep("A"))
        assertEquals(listOf("A", "B"), q.episodes.map { it.id })
    }

    @Test
    fun durationParseRejectsMalformedValues() {
        for (bad in listOf("", " ", ":", "1:", ":30", "1:2:3:4", "1:60", "1:00:60", "-5", "1.5", "abc", "99999999999999999999")) {
            assertNull(Durations.parse(bad), bad)
        }
        assertEquals(5400, Durations.parse("90:00"))   // MM may exceed 59 when there is no hour part
        assertEquals(65, Durations.parse("1:5"))
        assertEquals(0, Durations.parse("0"))
    }

    @Test
    fun formatBoundaries() {
        assertEquals("0:00", Durations.format(0))
        assertEquals("59:59", Durations.format(3599))
        assertEquals("1:00:00", Durations.format(3600))
        assertEquals("100:00:00", Durations.format(360000))
        assertFailsWith<IllegalArgumentException> { Durations.format(-1) }
    }

    @Test
    fun progressEdgeCases() {
        val p = ProgressTracker()
        val zero = ep("z", 0)
        p.update(zero, 10)
        assertFalse(p.isPlayed("z")) // zero-length episodes are never auto-marked played
        val e = ep("e", 100)
        p.update(e, -30)
        assertEquals(0, p.position("e"))
        p.update(e, 1000)
        assertTrue(p.isPlayed("e"))
        assertEquals(0, p.resumeAt(e))
        p.markUnplayed("e")
        assertFalse(p.isPlayed("e"))
        assertEquals(100, p.remainingSec(e))
        assertFailsWith<IllegalArgumentException> { Episode("bad", "Bad", -1, 0) }
    }
}
