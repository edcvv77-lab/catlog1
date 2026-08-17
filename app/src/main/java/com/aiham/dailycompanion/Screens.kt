package com.aiham.dailycompanion

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(state: AppState, onNavigate: (AppScreen) -> Unit) {
    val source = state.profilePhotoUri?.let { PersonSource.ContentUri(it) } ?: PersonSource.Resource(R.drawable.user_sample)
    val quotes = remember {
        listOf(
            "لا تنتظر الفرصة، بل اصنعها بنفسك.",
            "الاستمرار الهادئ أقوى من البداية المتحمسة.",
            "خطوة واضحة اليوم أفضل من خطة مثالية مؤجلة.",
            "رتّب يومك حول ما يهمك فعلاً."
        )
    }
    val quote = quotes[LocalDate.now().dayOfYear % quotes.size]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(58.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(3.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    PersonCutout(source, Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("مرحباً، ${state.profileName} 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("كل شيء مهم ليومك في مكان واحد.", color = AppMuted, fontSize = 13.sp)
                }
                IconButton(onClick = { onNavigate(AppScreen.Settings) }) {
                    Icon(
                        if (state.reminderEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsNone,
                        contentDescription = "التذكيرات",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.fillMaxSize().padding(start = 18.dp, end = 10.dp, top = 14.dp)) {
                    Column(Modifier.weight(1.1f).padding(vertical = 16.dp), verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("فكرة اليوم", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(quote, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
                        Spacer(Modifier.height(10.dp))
                        Text("اضغط على أفكاري لتبني مكتبتك الشخصية من الإلهام.", color = AppMuted, fontSize = 12.sp)
                        TextButton(onClick = { onNavigate(AppScreen.Ideas) }) { Text("فتح الأفكار") }
                    }
                    Box(Modifier.weight(.8f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                        PersonCutout(source, Modifier.fillMaxSize(), ContentScale.Fit)
                    }
                }
            }
        }

        item {
            SectionTitle("تقدّم الأهداف", "إدارة الأهداف") { onNavigate(AppScreen.Goals) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (state.goals.isEmpty()) {
                        EmptyInline("لا توجد أهداف بعد", "أضف أول هدف من زر +")
                    } else {
                        state.goals.take(3).forEachIndexed { index, goal ->
                            GoalMini(goal, index) { onNavigate(AppScreen.Goals) }
                            if (index < state.goals.take(3).lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .07f))
                        }
                    }
                }
            }
        }

        item {
            SectionTitle("لوحتك الشخصية")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeaturePeekCard(
                    modifier = Modifier.weight(1f),
                    title = "صوري",
                    subtitle = "${state.photoUris.size} صورة محفوظة",
                    emoji = "📷",
                    container = AppSky,
                    onClick = { onNavigate(AppScreen.Photos) }
                )
                FeaturePeekCard(
                    modifier = Modifier.weight(1f),
                    title = "أفكاري",
                    subtitle = "${state.ideas.size} فكرة",
                    emoji = "💡",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { onNavigate(AppScreen.Ideas) }
                )
            }
        }

        item {
            SectionTitle("وصول سريع")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickTile("ملاحظاتي", Icons.Rounded.StickyNote2, Modifier.weight(1f)) { onNavigate(AppScreen.Notes) }
                QuickTile("العادات", Icons.Rounded.Check, Modifier.weight(1f)) { onNavigate(AppScreen.Habits) }
                QuickTile("يومي", Icons.Rounded.EventNote, Modifier.weight(1f)) { onNavigate(AppScreen.Journal) }
                QuickTile("الإعدادات", Icons.Rounded.Settings, Modifier.weight(1f)) { onNavigate(AppScreen.Settings) }
            }
        }

        item {
            SectionTitle("متتبع السلوك", "عرض التفاصيل") { onNavigate(AppScreen.Habits) }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.Habits) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("التزام هذا الأسبوع", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${(state.weeklyHabitCompletion * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { state.weeklyHabitCompletion },
                        modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                        color = AppGreen,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f)
                    )
                    state.habits.take(3).forEach { habit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(habit.emoji, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(habit.name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text("${Integer.bitCount(habit.doneMask and 0x7F)}/7", color = AppMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
fun GoalsScreen(state: AppState) {
    var editing by remember { mutableStateOf<GoalItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<GoalItem?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("أهدافي", "🎯", "عدّل التقدم والتفاصيل في أي وقت") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, contentDescription = "إضافة هدف") }
        }
        if (state.goals.isEmpty()) {
            EmptyState("🎯", "لا توجد أهداف", "ابدأ بهدف واحد واضح وقابل للقياس.", "إضافة هدف") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryStrip(
                        leftValue = "${state.goals.count { it.progress >= 1f }}",
                        leftLabel = "مكتمل",
                        middleValue = "${state.goals.size}",
                        middleLabel = "إجمالي",
                        rightValue = "${(((state.goals.map { it.progress }.average().takeIf { !it.isNaN() } ?: 0.0) * 100).toInt())}%",
                        rightLabel = "متوسط"
                    )
                }
                items(state.goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onProgress = { state.setGoalProgress(goal.id, it) },
                        onEdit = { editing = goal },
                        onDelete = { deleting = goal }
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }
    }

    if (creating) GoalEditorDialog(null, { creating = false }) { title, emoji, detail, progress ->
        state.addGoal(title, emoji, detail, progress); creating = false
    }
    editing?.let { item ->
        GoalEditorDialog(item, { editing = null }) { title, emoji, detail, progress ->
            state.updateGoal(item.id, title, emoji, detail, progress); editing = null
        }
    }
    deleting?.let { item ->
        ConfirmDialog("حذف الهدف؟", "سيتم حذف \"${item.title}\" نهائياً.", "حذف", onDismiss = { deleting = null }) {
            state.deleteGoal(item.id); deleting = null
        }
    }
}

@Composable
fun IdeasScreen(state: AppState) {
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<IdeaItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<IdeaItem?>(null) }
    val filtered = state.ideas
        .filter { query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) }
        .sortedWith(compareByDescending<IdeaItem> { it.favorite })

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("أفكاري", "💡", "احتفظ بما يلهمك وعلّم المفضّل") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, contentDescription = "إضافة فكرة") }
        }
        SearchField(query, { query = it }, "ابحث في الأفكار")
        if (filtered.isEmpty()) {
            EmptyState("💡", "لا توجد نتائج", "جرّب عبارة أخرى أو أضف فكرة جديدة.", "إضافة فكرة") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { idea ->
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                    Text(idea.emoji, fontSize = 23.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(idea.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                IconButton(onClick = { state.toggleIdeaFavorite(idea.id) }) {
                                    Icon(
                                        if (idea.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "المفضلة",
                                        tint = if (idea.favorite) AppRed else AppMuted
                                    )
                                }
                            }
                            if (idea.body.isNotBlank()) {
                                Spacer(Modifier.height(10.dp))
                                Text(idea.body, color = AppMuted, lineHeight = 23.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { editing = idea }) { Icon(Icons.Rounded.Edit, null); Spacer(Modifier.width(4.dp)); Text("تعديل") }
                                TextButton(onClick = { deleting = idea }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                    Icon(Icons.Rounded.DeleteOutline, null); Spacer(Modifier.width(4.dp)); Text("حذف")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating) IdeaEditorDialog(null, { creating = false }) { title, body, emoji -> state.addIdea(title, body, emoji); creating = false }
    editing?.let { item ->
        IdeaEditorDialog(item, { editing = null }) { title, body, emoji -> state.updateIdea(item.id, title, body, emoji); editing = null }
    }
    deleting?.let { item ->
        ConfirmDialog("حذف الفكرة؟", "لن يمكن استعادتها بعد الحذف.", "حذف", onDismiss = { deleting = null }) {
            state.deleteIdea(item.id); deleting = null
        }
    }
}

@Composable
fun NotesScreen(state: AppState) {
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<NoteItem?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<NoteItem?>(null) }
    val filtered = state.notes
        .filter { query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) }
        .sortedWith(compareByDescending<NoteItem> { it.pinned }.thenByDescending { it.updatedAt })

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("ملاحظاتي", "📝", "ملاحظات سريعة مع تثبيت وتعديل") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, contentDescription = "إضافة ملاحظة") }
        }
        SearchField(query, { query = it }, "ابحث في الملاحظات")
        if (filtered.isEmpty()) {
            EmptyState("📝", "لا توجد ملاحظات", "اكتب ما لا تريد أن تنساه.", "إضافة ملاحظة") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(note.emoji, fontSize = 23.sp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(note.title, fontWeight = FontWeight.Bold)
                                    if (note.pinned) Text("مثبّتة", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                }
                                IconButton(onClick = { state.toggleNotePinned(note.id) }) {
                                    Icon(if (note.pinned) Icons.Rounded.PushPin else Icons.Rounded.BookmarkBorder, contentDescription = "تثبيت", tint = if (note.pinned) MaterialTheme.colorScheme.primary else AppMuted)
                                }
                            }
                            if (note.body.isNotBlank()) {
                                Text(note.body, color = AppMuted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { editing = note }) { Icon(Icons.Rounded.Edit, "تعديل", tint = AppMuted) }
                                IconButton(onClick = { deleting = note }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating) NoteEditorDialog(null, { creating = false }) { title, body, emoji -> state.addNote(title, body, emoji); creating = false }
    editing?.let { item ->
        NoteEditorDialog(item, { editing = null }) { title, body, emoji -> state.updateNote(item.id, title, body, emoji); editing = null }
    }
    deleting?.let { item ->
        ConfirmDialog("حذف الملاحظة؟", "سيتم حذفها من الجهاز.", "حذف", onDismiss = { deleting = null }) {
            state.deleteNote(item.id); deleting = null
        }
    }
}

@Composable
fun HabitsScreen(state: AppState) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<HabitItem?>(null) }
    var deleting by remember { mutableStateOf<HabitItem?>(null) }
    var resetConfirm by remember { mutableStateOf(false) }
    val days = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("متتبع العادات", "✅", "اضغط على كل يوم لتسجيل الإنجاز") {
            Row {
                IconButton(onClick = { resetConfirm = true }) { Icon(Icons.Rounded.RestartAlt, "تصفير الأسبوع") }
                FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, "إضافة عادة") }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummaryStrip(
                    leftValue = "${state.habits.size}",
                    leftLabel = "عادات",
                    middleValue = "${state.habits.sumOf { Integer.bitCount(it.doneMask and 0x7F) }}",
                    middleLabel = "إنجاز",
                    rightValue = "${(state.weeklyHabitCompletion * 100).roundToInt()}%",
                    rightLabel = "التزام"
                )
            }
            if (state.habits.isEmpty()) {
                item { EmptyInline("لا توجد عادات", "أضف عادة قابلة للقياس ثم سجّل أيامك") }
            } else {
                items(state.habits, key = { it.id }) { habit ->
                    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(habit.emoji, fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(habit.name, fontWeight = FontWeight.Bold)
                                    Text("${Integer.bitCount(habit.doneMask and 0x7F)} من 7 أيام", color = AppMuted, fontSize = 12.sp)
                                }
                                IconButton(onClick = { editing = habit }) { Icon(Icons.Rounded.Edit, "تعديل", tint = AppMuted) }
                                IconButton(onClick = { deleting = habit }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error) }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                days.forEachIndexed { day, label ->
                                    val done = (habit.doneMask and (1 shl day)) != 0
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, color = AppMuted, fontSize = 11.sp)
                                        Spacer(Modifier.height(5.dp))
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .background(if (done) AppGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = .08f), CircleShape)
                                                .clickable { state.toggleHabit(habit.id, day) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (creating) HabitEditorDialog(null, { creating = false }) { name, emoji -> state.addHabit(name, emoji); creating = false }
    editing?.let { item -> HabitEditorDialog(item, { editing = null }) { name, emoji -> state.updateHabit(item.id, name, emoji); editing = null } }
    deleting?.let { item -> ConfirmDialog("حذف العادة؟", "سيتم حذف سجل هذا الأسبوع معها.", "حذف", { deleting = null }) { state.deleteHabit(item.id); deleting = null } }
    if (resetConfirm) ConfirmDialog("تصفير الأسبوع؟", "سيتم إلغاء جميع علامات الإنجاز الحالية فقط، ولن تُحذف العادات.", "تصفير", { resetConfirm = false }) {
        state.resetHabitsWeek(); resetConfirm = false
    }
}

@Composable
fun PhotosScreen(state: AppState) {
    val context = LocalContext.current
    var deleting by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            state.addPhotoUri(uri.toString())
            if (state.profilePhotoUri == null) state.setProfilePhotoUri(uri.toString())
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("صوري", "📷", "أضف صورك وسيتم إبراز الشخص بدون الخلفية") {
            FilledIconButton(onClick = { launcher.launch(arrayOf("image/*")) }) { Icon(Icons.Rounded.AddAPhoto, "إضافة صورة") }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("يمكنك تعيين أي صورة كصورة رئيسية للتطبيق. المعالجة تتم على جهازك.", fontSize = 12.sp, modifier = Modifier.weight(1f))
            }
        }
        if (state.photoUris.isEmpty()) {
            EmptyState("📸", "معرضك فارغ", "اختر صورة من هاتفك. سنحفظ إذن الوصول إليها محلياً.", "اختيار صورة") {
                launcher.launch(arrayOf("image/*"))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(state.photoUris, key = { it }) { uri ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier.fillMaxWidth().aspectRatio(.86f).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                PersonCutout(PersonSource.ContentUri(uri), Modifier.fillMaxSize().padding(6.dp), ContentScale.Fit)
                                if (state.profilePhotoUri == uri) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("الرئيسية", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                                }
                            }
                            Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                IconButton(onClick = { state.setProfilePhotoUri(uri) }) {
                                    Icon(Icons.Rounded.Image, "تعيين رئيسية", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { deleting = uri }) {
                                    Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleting?.let { uri ->
        ConfirmDialog("حذف الصورة؟", "سيتم حذفها من معرض التطبيق فقط، ولن تُحذف من هاتفك.", "حذف", { deleting = null }) {
            state.removePhotoUri(uri); deleting = null
        }
    }
}

@Composable
fun JournalScreen(state: AppState) {
    val context = LocalContext.current
    var mood by remember(state.journal.date) { mutableStateOf(state.journal.mood) }
    var gratitude by remember(state.journal.date) { mutableStateOf(state.journal.gratitude) }
    var focus by remember(state.journal.date) { mutableStateOf(state.journal.focus) }
    var saved by remember { mutableStateOf(false) }
    val moods = listOf("😄", "🙂", "😐", "😔", "😤")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ScreenHeaderInline("يومي", "📔", "وقفة قصيرة لتثبيت ما يهمك اليوم") }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("كيف كان مزاجك؟", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        moods.forEach { item ->
                            Box(
                                Modifier
                                    .size(50.dp)
                                    .background(if (mood == item) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = .06f), CircleShape)
                                    .clickable { mood = item },
                                contentAlignment = Alignment.Center
                            ) { Text(item, fontSize = 26.sp) }
                        }
                    }
                    OutlinedTextField(
                        value = gratitude,
                        onValueChange = { gratitude = it; saved = false },
                        label = { Text("شيء أنا ممتن له اليوم") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = focus,
                        onValueChange = { focus = it; saved = false },
                        label = { Text("أهم شيء سأركز عليه") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Button(
                        onClick = { state.saveJournal(mood, gratitude, focus); saved = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Rounded.Bookmark, null); Spacer(Modifier.width(6.dp)); Text(if (saved) "تم الحفظ" else "حفظ يومي") }
                    OutlinedButton(
                        onClick = {
                            val text = "يومي — ${LocalDate.now()}\nالمزاج: $mood\nممتن لـ: $gratitude\nتركيزي: $focus"
                            shareText(context, text)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("مشاركة الملخص") }
                }
            }
        }
        item {
            Text("يتم حفظ هذا القسم محلياً على جهازك ولا يحتاج حساباً أو اتصالاً بالإنترنت.", color = AppMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SettingsScreen(state: AppState, onNavigate: (AppScreen) -> Unit) {
    val context = LocalContext.current
    var name by remember(state.profileName) { mutableStateOf(state.profileName) }
    var resetConfirm by remember { mutableStateOf(false) }
    var pendingEnable by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && pendingEnable) {
            state.setReminder(true)
            NotificationScheduler.scheduleDaily(context, state.reminderHour, state.reminderMinute)
        }
        pendingEnable = false
    }

    fun enableReminder() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pendingEnable = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            state.setReminder(true)
            NotificationScheduler.scheduleDaily(context, state.reminderHour, state.reminderMinute)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeaderInline("الإعدادات", "⚙️", "خصص التجربة بما يناسبك") }
        item {
            SettingCard(title = "الملف الشخصي", icon = Icons.Rounded.Edit) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسمك") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { state.setProfileName(name) }, modifier = Modifier.weight(1f)) { Text("حفظ الاسم") }
                    OutlinedButton(onClick = { onNavigate(AppScreen.Photos) }, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.PhotoLibrary, null); Spacer(Modifier.width(4.dp)); Text("الصورة") }
                }
            }
        }
        item {
            SettingCard(title = "المظهر", icon = if (state.darkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("الوضع الداكن", fontWeight = FontWeight.SemiBold)
                        Text("تغيير ألوان التطبيق بالكامل", color = AppMuted, fontSize = 12.sp)
                    }
                    Switch(checked = state.darkMode, onCheckedChange = { state.setDarkMode(it) })
                }
            }
        }
        item {
            SettingCard(title = "التذكير اليومي", icon = Icons.Rounded.Schedule) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("تذكير بالأهداف والعادات", fontWeight = FontWeight.SemiBold)
                        Text(formatTime(state.reminderHour, state.reminderMinute), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) enableReminder() else {
                                state.setReminder(false)
                                NotificationScheduler.cancel(context)
                            }
                        }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(context, { _, hour, minute ->
                                state.setReminder(state.reminderEnabled, hour, minute)
                                if (state.reminderEnabled) NotificationScheduler.scheduleDaily(context, hour, minute)
                            }, state.reminderHour, state.reminderMinute, false).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Rounded.Schedule, null); Spacer(Modifier.width(4.dp)); Text("اختيار الوقت") }
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else NotificationScheduler.showReminder(context)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Icon(Icons.Rounded.Notifications, null); Spacer(Modifier.width(4.dp)); Text("اختبار") }
                }
            }
        }
        item {
            SettingCard(title = "بياناتي", icon = Icons.Rounded.Share) {
                OutlinedButton(onClick = { shareText(context, state.exportText()) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Share, null); Spacer(Modifier.width(6.dp)); Text("مشاركة ملخص بياناتي")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { resetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(6.dp)); Text("إعادة التطبيق للوضع الافتراضي") }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("مساعدي اليومي 2.0", fontWeight = FontWeight.Bold)
                    Text("نسخة محسّنة تعمل محلياً: أهداف، أفكار، ملاحظات، عادات، صور، يوميات وتذكيرات.", color = AppMuted, fontSize = 12.sp, lineHeight = 20.sp)
                }
            }
        }
    }

    if (resetConfirm) ConfirmDialog("إعادة ضبط التطبيق؟", "سيتم حذف جميع بياناتك المحلية وإرجاع المحتوى التجريبي الافتراضي.", "إعادة ضبط", { resetConfirm = false }) {
        NotificationScheduler.cancel(context)
        state.resetAll(); name = state.profileName; resetConfirm = false
    }
}

@Composable
fun MoreScreen(state: AppState, onNavigate: (AppScreen) -> Unit) {
    val source = state.profilePhotoUri?.let { PersonSource.ContentUri(it) } ?: PersonSource.Resource(R.drawable.user_sample)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Box(Modifier.size(62.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.BottomCenter) {
                    PersonCutout(source, Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.profileName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("مساحتك الشخصية اليومية", color = AppMuted, fontSize = 13.sp)
                }
                IconButton(onClick = { onNavigate(AppScreen.Settings) }) { Icon(Icons.Rounded.Settings, "الإعدادات") }
            }
        }
        item { MoreEntry(Icons.Rounded.Lightbulb, "أفكاري", "${state.ideas.size} فكرة محفوظة", AppScreen.Ideas, onNavigate) }
        item { MoreEntry(Icons.Rounded.StickyNote2, "ملاحظاتي", "${state.notes.size} ملاحظة", AppScreen.Notes, onNavigate) }
        item { MoreEntry(Icons.Rounded.Check, "متتبع العادات", "التزام ${(state.weeklyHabitCompletion * 100).roundToInt()}% هذا الأسبوع", AppScreen.Habits, onNavigate) }
        item { MoreEntry(Icons.Rounded.EventNote, "يومي", "مزاج اليوم ${state.journal.mood}", AppScreen.Journal, onNavigate) }
        item { MoreEntry(Icons.Rounded.Settings, "الإعدادات", "الملف الشخصي، المظهر والتذكيرات", AppScreen.Settings, onNavigate) }
    }
}

@Composable
private fun MoreEntry(icon: ImageVector, title: String, subtitle: String, screen: AppScreen, onNavigate: (AppScreen) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate(screen) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = AppMuted, fontSize = 12.sp)
            }
            Text("‹", color = AppMuted, fontSize = 26.sp)
        }
    }
}

@Composable
private fun GoalCard(goal: GoalItem, onProgress: (Float) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(goal.emoji, fontSize = 24.sp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.Bold)
                    if (goal.detail.isNotBlank()) Text(goal.detail, color = AppMuted, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, "تعديل", tint = AppMuted) }
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error) }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${(goal.progress * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(46.dp))
                Slider(value = goal.progress, onValueChange = onProgress, modifier = Modifier.weight(1f))
            }
            if (goal.progress >= 1f) {
                Surface(color = AppGreen.copy(alpha = .12f), shape = RoundedCornerShape(10.dp)) {
                    Text("مكتمل ✓", color = AppGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun GoalMini(goal: GoalItem, index: Int, onClick: () -> Unit) {
    val tint = listOf(AppPurple, AppAmber, Color(0xFF4A9BF1), AppGreen)[index % 4]
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(50.dp).background(tint.copy(alpha = .12f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(goal.emoji, fontSize = 23.sp) }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(goal.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = tint,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f)
            )
        }
        Spacer(Modifier.width(8.dp)); Text("${(goal.progress * 100).roundToInt()}%", color = AppMuted, fontSize = 11.sp)
    }
}

@Composable
private fun FeaturePeekCard(modifier: Modifier, title: String, subtitle: String, emoji: String, container: Color, onClick: () -> Unit) {
    Card(modifier = modifier.height(132.dp).clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = container)) {
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.height(6.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AppMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuickTile(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 13.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun ScreenHeader(title: String, emoji: String, subtitle: String, action: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("$emoji  $title", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AppMuted, fontSize = 12.sp)
        }
        action()
    }
}

@Composable
private fun ScreenHeaderInline(title: String, emoji: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$emoji  $title", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, color = AppMuted, fontSize = 12.sp)
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp) }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, hint: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text(hint) },
        leadingIcon = { Icon(Icons.Rounded.Search, null) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun SummaryStrip(leftValue: String, leftLabel: String, middleValue: String, middleLabel: String, rightValue: String, rightLabel: String) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            Stat(leftValue, leftLabel); Stat(middleValue, middleLabel); Stat(rightValue, rightLabel)
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, color = AppMuted, fontSize = 10.sp)
    }
}

@Composable
private fun EmptyState(emoji: String, title: String, body: String, action: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 54.sp)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body, color = AppMuted, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
            Button(onClick = onClick) { Text(action) }
        }
    }
}

@Composable
private fun EmptyInline(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(body, color = AppMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun GoalEditorDialog(item: GoalItem?, onDismiss: () -> Unit, onSave: (String, String, String, Float) -> Unit) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "🎯") }
    var detail by remember(item?.id) { mutableStateOf(item?.detail ?: "") }
    var progress by remember(item?.id) { mutableStateOf(item?.progress ?: 0f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "إضافة هدف" else "تعديل الهدف", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(88.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("عنوان الهدف") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(detail, { detail = it }, label = { Text("التفاصيل") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Text("التقدم: ${(progress * 100).roundToInt()}%", fontWeight = FontWeight.SemiBold)
                Slider(value = progress, onValueChange = { progress = it })
            }
        },
        confirmButton = { Button(onClick = { onSave(title, emoji, detail, progress) }, enabled = title.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun IdeaEditorDialog(item: IdeaItem?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var body by remember(item?.id) { mutableStateOf(item?.body ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "💡") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "إضافة فكرة" else "تعديل الفكرة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(88.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(body, { body = it }, label = { Text("الفكرة") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            }
        },
        confirmButton = { Button(onClick = { onSave(title, body, emoji) }, enabled = title.isNotBlank() || body.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun NoteEditorDialog(item: NoteItem?, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var body by remember(item?.id) { mutableStateOf(item?.body ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "📝") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "إضافة ملاحظة" else "تعديل الملاحظة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(88.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(body, { body = it }, label = { Text("النص") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
            }
        },
        confirmButton = { Button(onClick = { onSave(title, body, emoji) }, enabled = title.isNotBlank() || body.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun HabitEditorDialog(item: HabitItem?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember(item?.id) { mutableStateOf(item?.name ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "✅") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "إضافة عادة" else "تعديل العادة", fontWeight = FontWeight.Bold) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(88.dp), singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("اسم العادة") }, modifier = Modifier.weight(1f), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onSave(name, emoji) }, enabled = name.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun ConfirmDialog(title: String, body: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(body) },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة عبر"))
}

private fun formatTime(hour: Int, minute: Int): String {
    val isPm = hour >= 12
    val h = when (val normalized = hour % 12) { 0 -> 12; else -> normalized }
    return "%d:%02d %s".format(Locale.US, h, minute, if (isPm) "م" else "ص")
}
