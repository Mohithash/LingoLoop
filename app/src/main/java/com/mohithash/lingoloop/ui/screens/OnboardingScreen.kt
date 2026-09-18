@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.lingoloop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.lingoloop.domain.LANGUAGES
import com.mohithash.lingoloop.domain.LEVELS
import com.mohithash.lingoloop.domain.Profile
import com.mohithash.lingoloop.ui.AppViewModel
import com.mohithash.lingoloop.ui.Label
import com.mohithash.lingoloop.ui.StatCard

@Composable
fun ProfileForm(initial: Profile, onChange: (Profile) -> Unit) {
    var native by remember { mutableStateOf(initial.native) }
    var target by remember { mutableStateOf(initial.target) }
    var level by remember { mutableStateOf(initial.level) }
    var goal by remember { mutableStateOf(initial.goal) }
    fun emit() = onChange(initial.copy(native = native, target = target, level = level, goal = goal))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Label("I want to learn")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { LANGUAGES.forEach { l -> FilterChip(selected = target == l, onClick = { target = l; emit() }, label = { Text(l) }) } }
        OutlinedTextField(native, { native = it; emit() }, label = { Text("I speak") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        Label("My level")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LEVELS.forEach { l -> FilterChip(selected = level == l, onClick = { level = l; emit() }, label = { Text(l + when (l) { "A1" -> " · beginner"; "B1" -> " · intermediate"; "C1" -> " · advanced"; else -> "" }) }) }
        }
        Label("Why")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("travel", "work", "family", "exam", "fun").forEach { g -> FilterChip(selected = goal == g, onClick = { goal = g; emit() }, label = { Text(g) }) } }
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Profile()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("Speak from day one") }, subtitle = { Text("Role‑play real situations, get corrected gently, and keep the words you meet.") }, scrollBehavior = scroll) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { ProfileForm(Profile()) { draft = it } }
            Button({ vm.saveProfile(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Let's go", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
