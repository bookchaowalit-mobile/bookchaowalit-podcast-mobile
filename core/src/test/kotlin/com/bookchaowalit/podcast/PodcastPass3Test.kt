package com.bookchaowalit.podcast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PodcastPass3Test {

    @Test
    fun hugeDurationsAreRejectedInsteadOfOverflowing() {
        // 2e17 minutes * 60 overflows Long and used to wrap to a garbage (even negative) value,
        // which then crashed Episode(durationSec < 0) while parsing a feed.
        assertNull(Durations.parse("200000000000000000:00"))
        assertNull(Durations.parse("9223372036854775807:59:59"))
        assertNull(Durations.parse("2147483648"))
        assertEquals(Int.MAX_VALUE, Durations.parse("2147483647"))
    }

    @Test
    fun remainingTimeIsNeverNegativeWhenAnEpisodeGetsShorter() {
        val long = Episode("e1", "Ep", 600, 0)
        val progress = ProgressTracker()
        progress.update(long, 500)
        // Feed refresh re-publishes the same episode with a trimmed duration.
        val trimmed = long.copy(durationSec = 300)
        assertEquals(0, progress.remainingSec(trimmed))
        val queue = PlayQueue().apply { addToEnd(trimmed); addToEnd(Episode("e2", "Next", 100, 0)) }
        assertEquals(100, queue.remainingSec(progress))
        assertEquals(300, progress.resumeAt(trimmed))
    }

    @Test
    fun seekDoesNotOverflow() {
        assertEquals(3600, seek(Int.MAX_VALUE - 5, 30, 3600))
        assertEquals(0, seek(Int.MIN_VALUE + 5, -30, 3600))
    }
}
