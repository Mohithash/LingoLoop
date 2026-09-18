@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.lingoloop.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.lingoloop.ui.AnimatedNumber
import com.mohithash.lingoloop.ui.AppViewModel
import com.mohithash.lingoloop.ui.BarChart
import com.mohithash.lingoloop.ui.HeroCard
import com.mohithash.lingoloop.ui.Label
import com.mohithash.lingoloop.ui.StatCard
import com.mohithash.lingoloop.ui.theme.Brand
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ProgressScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val cards by vm.cards.collectAsState()
    val sessions by vm.sessions.collectAsState()
    val msgs by vm.userMessages.collectAsState()
    val p by vm.profile.collectAsState()
    val cs = MaterialTheme.colorScheme
    val mature = cards.count { it.intervalDays >= 21 }
    val learning = cards.count { it.reps > 0 && it.intervalDays < 21 }
    val fresh = cards.size - mature - learning
    val perDay = (0 until 7).map { LocalDate.now().minusDays(6L - it) }.map { d ->
        d.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() } to cards.count { Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate() == d }
    }
    Scaffold(topBar = { TopAppBar(title = { Text("Progress") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Sunny) {
                val on = cs.onPrimary
                Label("${p.target} · ${p.level}", on.copy(alpha = 0.8f))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column { AnimatedNumber(cards.size, MaterialTheme.typography.displaySmall, on); Text("words met", color = on.copy(alpha = 0.85f)) }
                    Column { AnimatedNumber(msgs, MaterialTheme.typography.displaySmall, on); Text("things said", color = on.copy(alpha = 0.85f)) }
                    Column { AnimatedNumber(sessions.size, MaterialTheme.typography.displaySmall, on); Text("scenes", color = on.copy(alpha = 0.85f)) }
                }
            }
            StatCard {
                Label("Deck maturity")
                Text("$mature known · $learning learning · $fresh new", style = MaterialTheme.typography.titleMedium)
                LinearWavyProgressIndicator(progress = { if (cards.isEmpty()) 0f else mature.toFloat() / cards.size }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Text("A word counts as known once its review interval passes 3 weeks.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            StatCard { Label("New words · last 7 days"); BarChart(perDay, goal = 5, modifier = Modifier.padding(top = 8.dp), base = cs.primaryContainer, hit = cs.primary) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
