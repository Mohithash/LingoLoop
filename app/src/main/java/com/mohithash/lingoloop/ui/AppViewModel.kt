package com.mohithash.lingoloop.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.lingoloop.App
import com.mohithash.lingoloop.ai.AiSettings
import com.mohithash.lingoloop.data.Card
import com.mohithash.lingoloop.data.Message
import com.mohithash.lingoloop.data.Session
import com.mohithash.lingoloop.domain.Profile
import com.mohithash.lingoloop.domain.SCENARIOS
import com.mohithash.lingoloop.domain.Scenario
import com.mohithash.lingoloop.domain.Srs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Job<out T> {
    data object Idle : Job<Nothing>
    data object Loading : Job<Nothing>
    data class Done<T>(val value: T) : Job<T>
    data class Failed(val message: String) : Job<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(private val app: App) : ViewModel() {
    private val db = app.db
    val client get() = app.client

    val ai: StateFlow<AiSettings> = app.store.flow("ai", AiSettings.serializer(), AiSettings())
    val profile: StateFlow<Profile> = app.store.flow("profile", Profile.serializer(), Profile())
    fun saveAi(a: AiSettings) = app.store.set("ai", AiSettings.serializer(), a)
    fun saveProfile(p: Profile) = app.store.set("profile", Profile.serializer(), p.copy(onboarded = true))

    val sessions = db.sessions().all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val current = MutableStateFlow<Session?>(null)
    val messages: StateFlow<List<Message>> = current.flatMapLatest { s -> if (s == null) flowOf(emptyList()) else db.messages().forSession(s.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val cards: StateFlow<List<Card>> = profile.flatMapLatest { db.cards().all(it.target) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dueCount: StateFlow<Int> = profile.flatMapLatest { db.cards().dueCount(it.target, System.currentTimeMillis()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val userMessages: StateFlow<Int> = db.messages().userCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _turn = MutableStateFlow<Job<Unit>>(Job.Idle)
    val turn: StateFlow<Job<Unit>> = _turn
    private val _explain = MutableStateFlow<Job<String>>(Job.Idle)
    val explain: StateFlow<Job<String>> = _explain

    fun scenarioOf(s: Session): Scenario = SCENARIOS.firstOrNull { it.key == s.scenario } ?: SCENARIOS[0]

    /** Start a session and have the tutor open the conversation. */
    fun startSession(sc: Scenario, onReady: () -> Unit) = viewModelScope.launch {
        val p = profile.value
        val id = db.sessions().insert(Session(scenario = sc.key, title = sc.title, target = p.target))
        val s = Session(id = id, scenario = sc.key, title = sc.title, target = p.target)
        current.value = s
        onReady()
        send("(start the conversation)", opener = true)
    }

    fun openSession(s: Session) { current.value = s }
    fun deleteSession(s: Session) = viewModelScope.launch { db.messages().deleteSession(s.id); db.sessions().delete(s.id); if (current.value?.id == s.id) current.value = null }

    fun send(text: String, opener: Boolean = false) {
        val s = current.value ?: return
        if (text.isBlank()) return
        _turn.value = Job.Loading
        viewModelScope.launch {
            if (!opener) db.messages().insert(Message(sessionId = s.id, role = "user", text = text.trim()))
            val history = db.messages().listForSession(s.id)
            _turn.value = runCatching { app.tutor.turn(ai.value, profile.value, scenarioOf(s), history.dropLast(if (opener) 0 else 1), if (opener) text else history.last().text) }.fold({ t ->
                db.messages().insert(Message(sessionId = s.id, role = "assistant", text = t.reply, translation = t.reply_translation,
                    correction = if (opener) "" else t.correction, explanation = if (opener) "" else t.explanation, praise = t.praise))
                t.new_words.forEach { w ->
                    if (w.word.isNotBlank() && db.cards().exists(w.word, profile.value.target) == 0)
                        db.cards().insert(Card(word = w.word, meaning = w.meaning, example = w.example, target = profile.value.target))
                }
                Job.Done(Unit)
            }, { Job.Failed(it.message ?: "Failed") })
        }
    }

    fun explainSentence(text: String) {
        _explain.value = Job.Loading
        viewModelScope.launch { _explain.value = runCatching { app.tutor.explain(ai.value, profile.value, text) }.fold({ Job.Done(it) }, { Job.Failed(it.message ?: "Failed") }) }
    }
    fun clearExplain() { _explain.value = Job.Idle }

    // ── Flashcards ───────────────────────────────────────────────────────
    val reviewQueue = MutableStateFlow<List<Card>>(emptyList())
    fun startReview() = viewModelScope.launch { reviewQueue.value = db.cards().due(profile.value.target, System.currentTimeMillis()) }
    fun grade(c: Card, g: Int) = viewModelScope.launch {
        val updated = Srs.review(c, g)
        db.cards().update(updated)
        reviewQueue.value = reviewQueue.value.drop(1) + (if (g == 0) listOf(updated) else emptyList())
    }
    fun addCard(word: String, meaning: String) = viewModelScope.launch {
        if (word.isNotBlank() && db.cards().exists(word, profile.value.target) == 0) db.cards().insert(Card(word = word.trim(), meaning = meaning.trim(), target = profile.value.target))
    }
    fun deleteCard(id: Long) = viewModelScope.launch { db.cards().delete(id) }
}
