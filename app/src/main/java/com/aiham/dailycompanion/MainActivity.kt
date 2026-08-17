package com.aiham.dailycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationScheduler.createChannel(this)
        val prefs = getSharedPreferences("daily_companion", MODE_PRIVATE)
        if (prefs.getBoolean("reminder_enabled", false)) {
            NotificationScheduler.scheduleDaily(
                this,
                prefs.getInt("reminder_hour", 20),
                prefs.getInt("reminder_minute", 0)
            )
        }

        setContent {
            val state = remember { ProfessionalState(applicationContext) }
            DailyCompanionTheme(darkTheme = state.darkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ProfessionalAppRoot(state)
                }
            }
        }
    }
}

/** Kept for the legacy 2.x screen source file so old code remains buildable during migration. */
enum class AppScreen(val label: String) {
    Home("الرئيسية"), Goals("أهدافي"), Ideas("أفكاري"), Notes("ملاحظاتي"), Habits("العادات"),
    Photos("صوري"), Journal("يومي"), Settings("الإعدادات"), More("المزيد")
}

enum class ProScreen(val label: String) {
    Home("الرئيسية"),
    Goals("أهدافي"),
    Ideas("أفكاري"),
    Notes("ملاحظاتي"),
    Habits("العادات"),
    Photos("صوري"),
    Journal("يومي"),
    Settings("الإعدادات"),
    More("المزيد")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfessionalAppRoot(state: ProfessionalState) {
    var screen by rememberSaveable { mutableStateOf(ProScreen.Home) }
    var addSheet by rememberSaveable { mutableStateOf(false) }
    var quickType by remember { mutableStateOf<ProQuickType?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ProfessionalBottomBar(
                current = screen,
                onHome = { screen = ProScreen.Home },
                onGoals = { screen = ProScreen.Goals },
                onAdd = { addSheet = true },
                onPhotos = { screen = ProScreen.Photos },
                onMore = { screen = ProScreen.More }
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(inner),
            color = MaterialTheme.colorScheme.background
        ) {
            when (screen) {
                ProScreen.Home -> ProfessionalHomeScreen(state) { screen = it }
                ProScreen.Goals -> ProfessionalGoalsScreen(state)
                ProScreen.Ideas -> ProfessionalIdeasScreen(state)
                ProScreen.Notes -> ProfessionalNotesScreen(state)
                ProScreen.Habits -> ProfessionalHabitsScreen(state)
                ProScreen.Photos -> ProfessionalPhotosScreen(state)
                ProScreen.Journal -> ProfessionalJournalScreen(state)
                ProScreen.Settings -> ProfessionalSettingsScreen(state) { screen = it }
                ProScreen.More -> ProfessionalMoreScreen(state) { screen = it }
            }
        }
    }

    if (addSheet) {
        ModalBottomSheet(onDismissRequest = { addSheet = false }, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text("إضافة سريعة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text("سجّل ما خطر لك الآن بدون البحث داخل التطبيق.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f), fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                ProQuickType.entries.forEach { type ->
                    ProfessionalQuickRow(type.emoji, type.title) {
                        addSheet = false
                        quickType = type
                    }
                }
                ProfessionalQuickRow("📷", "إضافة صورة أو لحظة") {
                    addSheet = false
                    screen = ProScreen.Photos
                }
                ProfessionalQuickRow("📔", "تسجيل يومي") {
                    addSheet = false
                    screen = ProScreen.Journal
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    quickType?.let { type ->
        ProQuickCreateDialog(
            type = type,
            onDismiss = { quickType = null },
            onSave = { title, body, emoji ->
                when (type) {
                    ProQuickType.Goal -> state.addGoal(title, emoji, body)
                    ProQuickType.Idea -> state.addIdea(title, body, emoji)
                    ProQuickType.Note -> state.addNote(title, body, emoji)
                    ProQuickType.Habit -> state.addHabit(title, emoji)
                }
                quickType = null
            }
        )
    }
}

@Composable
private fun ProfessionalBottomBar(
    current: ProScreen,
    onHome: () -> Unit,
    onGoals: () -> Unit,
    onAdd: () -> Unit,
    onPhotos: () -> Unit,
    onMore: () -> Unit
) {
    Surface(shadowElevation = 18.dp, color = MaterialTheme.colorScheme.surface) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            ProBottomItem("الرئيسية", Icons.Rounded.Home, current == ProScreen.Home, onHome)
            ProBottomItem("أهدافي", Icons.Rounded.TrackChanges, current == ProScreen.Goals, onGoals)
            NavigationBarItem(
                selected = false,
                onClick = onAdd,
                icon = {
                    Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = "إضافة", tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                },
                label = { Text("إضافة", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
            ProBottomItem("صوري", Icons.Rounded.PhotoLibrary, current == ProScreen.Photos, onPhotos)
            ProBottomItem("المزيد", Icons.Rounded.MoreHoriz, current == ProScreen.More, onMore)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ProBottomItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .45f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .45f)
        )
    )
}

@Composable
private fun ProfessionalQuickRow(emoji: String, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .48f))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = .82f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(11.dp))
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}
