package com.mohithash.lingoloop.domain

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val native: String = "English",
    val target: String = "Spanish",
    val level: String = "A2",
    val goal: String = "travel",
    val onboarded: Boolean = false,
)

@Serializable data class NewWord(val word: String, val meaning: String, val example: String = "")

/** What the tutor returns for every turn. */
@Serializable
data class TutorTurn(
    val reply: String = "",
    val reply_translation: String = "",
    val correction: String = "",
    val explanation: String = "",
    val new_words: List<NewWord> = emptyList(),
    val praise: String = "",
)

data class Scenario(val key: String, val title: String, val emoji: String, val brief: String)

val SCENARIOS = listOf(
    Scenario("free", "Free chat", "💬", "Casual conversation about whatever the learner brings up."),
    Scenario("cafe", "At a café", "☕", "You are a friendly waiter. The learner wants to order a drink and a snack, ask about prices, and pay."),
    Scenario("airport", "Airport check‑in", "✈️", "You are an airline check‑in agent. Handle passport, baggage, seat preference and gate directions."),
    Scenario("directions", "Asking directions", "🗺️", "You are a local on the street. The learner is lost and needs to reach the train station."),
    Scenario("doctor", "At the doctor", "🩺", "You are a doctor. Ask about symptoms, give simple advice, schedule a follow‑up."),
    Scenario("interview", "Job interview", "💼", "You are a hiring manager for a junior role. Ask about experience, strengths, availability."),
    Scenario("market", "Market haggling", "🥕", "You are a market vendor. The learner wants to buy produce and negotiate a price."),
    Scenario("hotel", "Hotel check‑in", "🏨", "You are a hotel receptionist. Reservation, room issues, breakfast, checkout time."),
)
val LANGUAGES = listOf("Spanish", "French", "German", "Italian", "Portuguese", "Japanese", "Korean", "Mandarin Chinese", "Hindi", "Arabic", "Russian", "Turkish", "Dutch", "English")
val LEVELS = listOf("A1", "A2", "B1", "B2", "C1")
