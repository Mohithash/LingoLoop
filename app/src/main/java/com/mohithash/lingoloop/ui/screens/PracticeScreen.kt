@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.lingoloop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.lingoloop.domain.SCENARIOS
import com.mohithash.lingoloop.ui.AppViewModel
import com.mohithash.lingoloop.ui.HeroCard
import com.mohithash.lingoloop.ui.Label
import com.mohithash.lingoloop.ui.prettyDate
import com.mohithash.lingoloop.ui.theme.Brand
import java.time.Instant
import java.time.ZoneId

@Composable
fun PracticeScreen(vm: AppViewModel, onChat: () -> Unit, onSettings: () -> Unit) {
    val p by vm.profile.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val due by vm.dueCount.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection), topBar = {
        MediumFlexibleTopAppBar(title = { Text("Practise ${p.target}") }, subtitle = { Text("${p.level} · ${if (due > 0) "$due words due" else "deck up to date"}") },
            actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }, scrollBehavior = scroll, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface))
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie9Sided) {
                    val on = cs.onPrimary
                    Label("Pick a scene", on.copy(alpha = 0.8f))
                    Text("Your tutor stays in character, corrects you gently, and saves new words to your deck.", color = on.copy(alpha = 0.9f))
                    if (!ai.configured) Text("Add your API key in Settings to start.", color = cs.secondaryContainer, style = MaterialTheme.typography.labelLarge)
                }
            }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SCENARIOS) { sc ->
                        Card(modifier = Modifier.width(150.dp).height(130.dp).clickable(enabled = ai.configured) { vm.startSession(sc, onChat) }, shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(containerColor = if (sc.key == "free") cs.secondaryContainer else cs.surfaceContainerHigh)) {
                            Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Text(sc.emoji, style = MaterialTheme.typography.headlineMedium)
                                Text(sc.title, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
            item { Text("Recent conversations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) }
            if (sessions.isEmpty()) item { Text("No conversations yet — tap a scene above.", color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp)) }
            items(sessions, key = { it.id }) { s ->
                val sc = vm.scenarioOf(s)
                ListItem(
                    leadingContent = { Text(sc.emoji, style = MaterialTheme.typography.headlineSmall) },
                    headlineContent = { Text("${s.title} · ${s.target}") },
                    supportingContent = { Text(Instant.ofEpochMilli(s.startedAt).atZone(ZoneId.systemDefault()).toLocalDate().toString().prettyDate()) },
                    trailingContent = { IconButton({ vm.deleteSession(s) }) { Icon(Icons.Default.Delete, null, tint = cs.onSurfaceVariant) } },
                    colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow),
                    modifier = Modifier.padding(horizontal = 16.dp).clip(MaterialTheme.shapes.large).clickable { vm.openSession(s); onChat() },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
