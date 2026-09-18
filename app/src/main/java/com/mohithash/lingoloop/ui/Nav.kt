@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.lingoloop.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.lingoloop.ui.screens.ChatScreen
import com.mohithash.lingoloop.ui.screens.DeckScreen
import com.mohithash.lingoloop.ui.screens.OnboardingScreen
import com.mohithash.lingoloop.ui.screens.PracticeScreen
import com.mohithash.lingoloop.ui.screens.ProgressScreen
import com.mohithash.lingoloop.ui.screens.ReviewScreen
import com.mohithash.lingoloop.ui.screens.SettingsScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    PRACTICE("practice", "Practice", Icons.Outlined.Forum, Icons.Filled.Forum),
    DECK("deck", "Words", Icons.Outlined.Style, Icons.Filled.Style),
    PROGRESS("progress", "Progress", Icons.Outlined.TrendingUp, Icons.Filled.TrendingUp),
}

@Composable
fun Nav(vm: AppViewModel) {
    val p by vm.profile.collectAsState()
    if (!p.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val current = back?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }
    Scaffold(bottomBar = {
        if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Tab.entries.forEach { tab ->
                val sel = current?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(selected = sel, onClick = { nav.navigate(tab.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(if (sel) tab.selected else tab.icon, tab.label) }, label = { Text(tab.label) })
            }
        }
    }) { pad ->
        NavHost(nav, Tab.PRACTICE.route, Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.PRACTICE.route) { PracticeScreen(vm, onChat = { nav.navigate("chat") }, onSettings = { nav.navigate("settings") }) }
            composable(Tab.DECK.route) { DeckScreen(vm, onReview = { nav.navigate("review") }) }
            composable(Tab.PROGRESS.route) { ProgressScreen(vm, onSettings = { nav.navigate("settings") }) }
            composable("chat") { ChatScreen(vm, onBack = { nav.popBackStack() }) }
            composable("review") { ReviewScreen(vm, onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
