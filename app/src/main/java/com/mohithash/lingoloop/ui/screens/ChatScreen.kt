@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.lingoloop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.mohithash.lingoloop.data.Message
import com.mohithash.lingoloop.ui.AppViewModel
import com.mohithash.lingoloop.ui.Job

@Composable
fun ChatScreen(vm: AppViewModel, onBack: () -> Unit) {
    val session by vm.current.collectAsState()
    val s = session ?: run { onBack(); return }
    val msgs by vm.messages.collectAsState()
    val turn by vm.turn.collectAsState()
    val explain by vm.explain.collectAsState()
    val cs = MaterialTheme.colorScheme
    var input by remember { mutableStateOf("") }
    var showTranslations by remember { mutableStateOf(false) }
    var explaining by remember { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    LaunchedEffect(msgs.size, turn) { if (msgs.isNotEmpty()) list.animateScrollToItem(msgs.size + 1) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Column { Text(vm.scenarioOf(s).emoji + " " + s.title); Text(s.target, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton({ showTranslations = !showTranslations }) { Icon(Icons.Default.Translate, "Translations", tint = if (showTranslations) cs.primary else cs.onSurfaceVariant) } })
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().imePadding().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(input, { input = it }, placeholder = { Text("Reply in ${s.target}…") }, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraLarge, maxLines = 4)
                FilledIconButton({ vm.send(input); input = "" }, enabled = input.isNotBlank() && turn != Job.Loading, modifier = Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
            }
        },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs, key = { it.id }) { m -> Bubble(m, showTranslations) { explaining = m.text; vm.explainSentence(m.text) } }
            if (turn == Job.Loading) item { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { LoadingIndicator(Modifier.size(28.dp)); Text("…", color = cs.onSurfaceVariant) } }
            (turn as? Job.Failed)?.let { f -> item { Text(f.message, color = cs.error) } }
            item { Spacer(Modifier.size(8.dp)) }
        }
    }
    explaining?.let {
        AlertDialog(onDismissRequest = { explaining = null; vm.clearExplain() }, title = { Text("Break it down") },
            text = { when (val e = explain) { Job.Loading -> Row(verticalAlignment = Alignment.CenterVertically) { LoadingIndicator(); Spacer(Modifier.size(10.dp)); Text("Thinking…") }
                is Job.Done -> Text(e.value); is Job.Failed -> Text(e.message, color = cs.error); Job.Idle -> Text("") } },
            confirmButton = { TextButton({ explaining = null; vm.clearExplain() }) { Text("Close") } })
    }
}

@Composable
private fun Bubble(m: Message, showTranslation: Boolean, onExplain: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val user = m.role == "user"
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (user) Alignment.End else Alignment.Start) {
        if (m.correction.isNotBlank()) {
            // Correction of the learner's previous message sits above the tutor's reply.
            Column(Modifier.widthIn(max = 320.dp).background(cs.tertiaryContainer, MaterialTheme.shapes.large).padding(12.dp)) {
                Text("✏️ " + m.correction, color = cs.onTertiaryContainer, style = MaterialTheme.typography.bodyMedium)
                if (m.explanation.isNotBlank()) Text(m.explanation, color = cs.onTertiaryContainer.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.size(4.dp))
        }
        if (m.praise.isNotBlank()) Text("🌟 " + m.praise, style = MaterialTheme.typography.labelMedium, color = cs.primary, modifier = Modifier.padding(bottom = 4.dp))
        Box(Modifier.widthIn(max = 320.dp).background(if (user) cs.primary else cs.surfaceContainerHigh,
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (user) 20.dp else 6.dp, bottomEnd = if (user) 6.dp else 20.dp))
            .clickable(enabled = !user, onClick = onExplain).padding(14.dp)) {
            Column {
                Text(m.text, color = if (user) cs.onPrimary else cs.onSurface, style = MaterialTheme.typography.bodyLarge)
                if (!user && showTranslation && m.translation.isNotBlank()) Text(m.translation, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
