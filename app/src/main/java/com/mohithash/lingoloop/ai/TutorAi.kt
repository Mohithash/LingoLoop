package com.mohithash.lingoloop.ai

import com.mohithash.lingoloop.data.Message
import com.mohithash.lingoloop.domain.Profile
import com.mohithash.lingoloop.domain.Scenario
import com.mohithash.lingoloop.domain.TutorTurn

class TutorAi(private val client: AiClient) {
    private val turnSchema = Schema.obj(
        "reply" to Schema.str, "reply_translation" to Schema.str, "correction" to Schema.str, "explanation" to Schema.str,
        "new_words" to Schema.arr(Schema.obj("word" to Schema.str, "meaning" to Schema.str, "example" to Schema.str)), "praise" to Schema.str,
    )

    fun system(p: Profile, sc: Scenario) = """You are a patient ${p.target} tutor for a ${p.level} learner whose native language is ${p.native}. Goal: ${p.goal}.
        |Scenario: ${sc.brief}
        |Every turn: stay in character and reply in ${p.target} at a level the learner can follow (${p.level}; keep sentences short for A1/A2). Ask one question to keep the conversation going.
        |reply_translation: the reply in ${p.native}.
        |correction: if the learner's last message had mistakes, the corrected version in ${p.target}; otherwise empty string. Do not correct the very first "start" message.
        |explanation: one short sentence in ${p.native} explaining the main mistake, or empty.
        |new_words: 0-3 useful words/phrases from your reply the learner may not know, with ${p.native} meaning and a short example.
        |praise: 3-6 words of specific encouragement in ${p.native}, or empty.""".trimMargin()

    suspend fun turn(ai: AiSettings, p: Profile, sc: Scenario, history: List<Message>, userText: String): TutorTurn {
        val msgs = history.takeLast(16).map { ChatMsg(it.role, it.text) } + ChatMsg("user", userText)
        val raw = client.chat(ai, system(p, sc), msgs, turnSchema, 1500)
        return client.json.decodeFromString(TutorTurn.serializer(), client.extractJson(raw))
    }

    suspend fun explain(ai: AiSettings, p: Profile, text: String): String =
        client.chat(ai, "You are a ${p.target} tutor. Explain the grammar/vocabulary of the given ${p.target} sentence in ${p.native}, briefly (max 4 short bullet points).",
            listOf(ChatMsg("user", text)), maxTokens = 500)
}
