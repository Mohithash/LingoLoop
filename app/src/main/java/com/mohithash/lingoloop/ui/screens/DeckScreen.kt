@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.lingoloop.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohithash.lingoloop.ui.AppViewModel
import com.mohithash.lingoloop.ui.EmptyState
import com.mohithash.lingoloop.ui.HeroCard
import com.mohithash.lingoloop.ui.Label
import com.mohithash.lingoloop.ui.StatCard
import com.mohithash.lingoloop.ui.theme.Brand

@Composable
fun DeckScreen(vm: AppViewModel, onReview: () -> Unit) {
    val cards by vm.cards.collectAsState()
    val due by vm.dueCount.collectAsState()
    val p by vm.profile.collectAsState()
    val cs = MaterialTheme.colorScheme
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("${p.target} words") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                HeroCard(colors = listOf(cs.secondary, cs.secondary.copy(alpha = 0.7f)), blobShape = MaterialShapes.Diamond) {
                    val on = cs.onSecondary
                    Label("Spaced repetition", on.copy(alpha = 0.8f))
                    Text(if (due > 0) "$due due now" else "All caught up", style = MaterialTheme.typography.headlineMedium, color = on)
                    Text("${cards.size} words in your deck", color = on.copy(alpha = 0.85f))
                    Button({ vm.startReview(); onReview() }, enabled = due > 0, shapes = ButtonDefaults.shapes(), colors = ButtonDefaults.buttonColors(containerColor = on, contentColor = cs.secondary), modifier = Modifier.padding(top = 6.dp)) { Text("Review") }
                }
            }
            item {
                StatCard {
                    Label("Add a word")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(word, { word = it }, label = { Text(p.target) }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                        OutlinedTextField(meaning, { meaning = it }, label = { Text(p.native) }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.large)
                    }
                    FilledTonalButton({ vm.addCard(word, meaning); word = ""; meaning = "" }, enabled = word.isNotBlank() && meaning.isNotBlank(), shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) { Text("Add to deck") }
                }
            }
            if (cards.isEmpty()) item { EmptyState(Icons.Default.Style, "Deck is empty", "Words from your conversations land here automatically.") }
            items(cards, key = { it.id }) { c ->
                val dueNow = c.due <= System.currentTimeMillis()
                ListItem(headlineContent = { Text(c.word, style = MaterialTheme.typography.titleMedium) }, supportingContent = { Text(c.meaning + if (c.example.isNotBlank()) "\n“${c.example}”" else "") },
                    trailingContent = { Row(verticalAlignment = Alignment.CenterVertically) { Text(if (dueNow) "due" else "${c.intervalDays.toInt()}d", style = MaterialTheme.typography.labelMedium, color = if (dueNow) cs.secondary else cs.onSurfaceVariant); IconButton({ vm.deleteCard(c.id) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) } } },
                    colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow), modifier = Modifier.clip(MaterialTheme.shapes.large))
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ReviewScreen(vm: AppViewModel, onBack: () -> Unit) {
    val queue by vm.reviewQueue.collectAsState()
    val cs = MaterialTheme.colorScheme
    var flipped by remember { mutableStateOf(false) }
    val rot by animateFloatAsState(if (flipped) 180f else 0f, label = "flip")
    val card = queue.firstOrNull()
    LaunchedEffect(card?.id) { flipped = false }
    Scaffold(topBar = { TopAppBar(title = { Text(if (queue.isEmpty()) "Done" else "${queue.size} left") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            if (card == null) { EmptyState(Icons.Default.Style, "Session complete", "Come back when more words are due."); Button(onBack, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Back to deck") }; return@Column }
            Card(Modifier.fillMaxWidth().weight(1f).graphicsLayer { rotationY = rot; cameraDistance = 12f * density }.clickable { flipped = !flipped },
                shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = if (rot <= 90f) cs.primaryContainer else cs.secondaryContainer)) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    if (rot <= 90f) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Label("Tap to reveal", cs.onPrimaryContainer.copy(alpha = 0.7f)); Text(card.word, style = MaterialTheme.typography.displaySmall, color = cs.onPrimaryContainer, textAlign = TextAlign.Center)
                    } else Column(Modifier.graphicsLayer { rotationY = 180f }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.meaning, style = MaterialTheme.typography.headlineMedium, color = cs.onSecondaryContainer, textAlign = TextAlign.Center)
                        if (card.example.isNotBlank()) Text("“${card.example}”", style = MaterialTheme.typography.bodyLarge, color = cs.onSecondaryContainer.copy(alpha = 0.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Again" to cs.errorContainer, "Hard" to cs.tertiaryContainer, "Good" to cs.primaryContainer, "Easy" to cs.secondaryContainer).forEachIndexed { i, (l, c) ->
                    FilledTonalButton({ vm.grade(card, i) }, enabled = flipped, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = c)) { Text(l) }
                }
            }
        }
    }
}
