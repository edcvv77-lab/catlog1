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
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
            val state = remember { AppState(applicationContext) }
            DailyCompanionTheme(darkTheme = state.darkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(state)
                }
            }
        }
    }
}

enum class AppScreen(val label: String) {
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

private enum class QuickType(val title: String, val emoji: String) {
    Goal("هدف جديد", "🎯"),
    Idea("فكرة جديدة", "💡"),
    Note("ملاحظة جديدة", "📝"),
    Habit("عادة جديدة", "✅")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(state: AppState) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    var addSheet by rememberSaveable { mutableStateOf(false) }
    var quickType by remember { mutableStateOf<QuickType?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                current = screen,
                onHome = { screen = AppScreen.Home },
                onGoals = { screen = AppScreen.Goals },
                onAdd = { addSheet = true },
                onPhotos = { screen = AppScreen.Photos },
                onMore = { screen = AppScreen.More }
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(inner),
            color = MaterialTheme.colorScheme.background
        ) {
            when (screen) {
                AppScreen.Home -> HomeScreen(state, onNavigate = { screen = it })
                AppScreen.Goals -> GoalsScreen(state)
                AppScreen.Ideas -> IdeasScreen(state)
                AppScreen.Notes -> NotesScreen(state)
                AppScreen.Habits -> HabitsScreen(state)
                AppScreen.Photos -> PhotosScreen(state)
                AppScreen.Journal -> JournalScreen(state)
                AppScreen.Settings -> SettingsScreen(state, onNavigate = { screen = it })
                AppScreen.More -> MoreScreen(state, onNavigate = { screen = it })
            }
        }
    }

    if (addSheet) {
        ModalBottomSheet(onDismissRequest = { addSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("إضافة سريعة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("أضف ما تحتاجه بدون مغادرة سياقك الحالي.", color = AppMuted, fontSize = 13.sp)
                QuickType.entries.forEach { type ->
                    QuickAddRow(type.title, type.emoji) {
                        addSheet = false
                        quickType = type
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    quickType?.let { type ->
        QuickCreateDialog(
            type = type,
            onDismiss = { quickType = null },
            onSave = { title, body, emoji ->
                when (type) {
                    QuickType.Goal -> state.addGoal(title, emoji, body)
                    QuickType.Idea -> state.addIdea(title, body, emoji)
                    QuickType.Note -> state.addNote(title, body, emoji)
                    QuickType.Habit -> state.addHabit(title, emoji)
                }
                quickType = null
            }
        )
    }
}

@Composable
private fun AppBottomBar(
    current: AppScreen,
    onHome: () -> Unit,
    onGoals: () -> Unit,
    onAdd: () -> Unit,
    onPhotos: () -> Unit,
    onMore: () -> Unit
) {
    Surface(shadowElevation = 14.dp, color = MaterialTheme.colorScheme.surface) {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
            BottomNavItem("الرئيسية", Icons.Rounded.Home, current == AppScreen.Home, onHome)
            BottomNavItem("أهدافي", Icons.Rounded.TrackChanges, current == AppScreen.Goals, onGoals)
            NavigationBarItem(
                selected = false,
                onClick = onAdd,
                icon = {
                    Box(
                        Modifier.size(50.dp).background(AppPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "إضافة", tint = Color.White)
                    }
                },
                label = { Text("إضافة", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
            BottomNavItem("صوري", Icons.Rounded.PhotoLibrary, current == AppScreen.Photos, onPhotos)
            BottomNavItem("المزيد", Icons.Rounded.MoreHoriz, current == AppScreen.More, onMore)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomNavItem(
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
            unselectedIconColor = AppMuted,
            unselectedTextColor = AppMuted
        )
    )
}

@Composable
private fun QuickAddRow(title: String, emoji: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 25.sp)
        Spacer(Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun QuickCreateDialog(
    type: QuickType,
    onDismiss: () -> Unit,
    onSave: (title: String, body: String, emoji: String) -> Unit
) {
    var title by remember(type) { mutableStateOf("") }
    var body by remember(type) { mutableStateOf("") }
    var emoji by remember(type) { mutableStateOf(type.emoji) }
    val needsBody = type != QuickType.Habit

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(type.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(3) },
                        label = { Text("رمز") },
                        modifier = Modifier.width(92.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (type == QuickType.Habit) "اسم العادة" else "العنوان") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                if (needsBody) {
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text(if (type == QuickType.Goal) "تفاصيل الهدف" else "التفاصيل") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, body, emoji) }, enabled = title.isNotBlank() || (needsBody && body.isNotBlank())) {
                Text("حفظ")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
