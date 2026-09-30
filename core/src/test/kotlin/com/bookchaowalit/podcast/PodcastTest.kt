package com.bookchaowalit.podcast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PodcastTest {
    private fun ep(id: String, dur: Int = 600) = Episode(id, "Episode $id", dur, 0)

    @Test
    fun parsesItunesDurations() {
        assertEquals(3600, Durations.parse("3600"))
        assertEquals(125, Durations.parse("2:05"))
        assertEquals(3723, Durations.parse("01:02:03"))
        assertNull(Durations.parse("1:60"))
        assertNull(Durations.parse("abc"))
        assertNull(Durations.parse("1::2"))
        assertNull(Durations.parse("1:2:3:4"))
        assertNull(Durations.parse("-5"))
    }

    @Test
    fun formatsDurations() {
        assertEquals("0:00", Durations.format(0))
        assertEquals("2:05", Durations.format(125))
        assertEquals("1:02:03", Durations.format(3723))
    }

    @Test
    fun progressMarksPlayedNearTheEnd() {
        val p = ProgressTracker()
        val e = ep("1", 1000)
        p.update(e, 500)
        assertEquals(500, p.resumeAt(e))
        assertFalse(p.isPlayed("1"))
        p.update(e, 960)
        assertTrue(p.isPlayed("1"))
        assertEquals(0, p.resumeAt(e))
        p.update(e, 5000)
        assertEquals(1000, p.position("1"))
        p.markUnplayed("1")
        assertEquals(0, p.position("1"))
        assertFalse(p.isPlayed("1"))
    }

    @Test
    fun seekClamps() {
        assertEquals(0, seek(10, -15, 600))
        assertEquals(600, seek(590, 30, 600))
        assertEquals(40, seek(10, 30, 600))
    }

    @Test
    fun queueOperations() {
        val q = PlayQueue()
        assertNull(q.current)
        q.addToEnd(ep("a")); q.addToEnd(ep("b")); q.addToEnd(ep("a"))
        assertEquals(listOf("a", "b"), q.episodes.map { it.id })
        assertEquals("a", q.current?.id)
        q.playNext(ep("c"))
        assertEquals(listOf("a", "c", "b"), q.episodes.map { it.id })
        assertEquals("c", q.next()?.id)
        q.remove("a")
        assertEquals("c", q.current?.id)
        assertEquals("b", q.next()?.id)
        assertNull(q.next())
        assertEquals("c", q.previous()?.id)
        q.remove("c"); q.remove("b")
        assertNull(q.current)
        assertEquals(-1, q.currentIndex)
    }

    @Test
    fun queueRemainingTime() {
        val q = PlayQueue()
        val a = ep("a", 100); val b = ep("b", 200)
        q.addToEnd(a); q.addToEnd(b)
        val p = ProgressTracker()
        p.update(a, 40)
        assertEquals(260, q.remainingSec(p))
    }
}
