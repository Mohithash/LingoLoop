package com.mohithash.lingoloop.domain

import com.mohithash.lingoloop.data.Card

/** SM‑2 style scheduling. grade: 0 = again, 1 = hard, 2 = good, 3 = easy. */
object Srs {
    fun review(c: Card, grade: Int, now: Long = System.currentTimeMillis()): Card {
        val day = 86_400_000L
        if (grade == 0) return c.copy(reps = 0, intervalDays = 0.0, ease = maxOf(1.3, c.ease - 0.2), due = now + 10 * 60_000L)
        val ease = (c.ease + when (grade) { 1 -> -0.15; 3 -> 0.15; else -> 0.0 }).coerceIn(1.3, 3.0)
        val interval = when {
            c.reps == 0 -> if (grade == 3) 3.0 else 1.0
            c.reps == 1 -> if (grade == 3) 7.0 else 4.0
            else -> c.intervalDays * ease * (if (grade == 1) 0.6 else if (grade == 3) 1.3 else 1.0)
        }
        return c.copy(reps = c.reps + 1, intervalDays = interval, ease = ease, due = now + (interval * day).toLong())
    }
}
