package com.aiham.dailycompanion

import android.Manifest
import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ProfessionalIdeasScreen(state: ProfessionalState) {
    var query by rememberSaveable { mutableStateOf("") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProIdea?>(null) }
    var deleting by remember { mutableStateOf<ProIdea?>(null) }

    val filtered = state.ideas
        .filter { !favoritesOnly || it.favorite }
        .filter { query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) || it.category.contains(query, true) }
        .sortedWith(compareByDescending<ProIdea> { it.favorite }.thenByDescending { it.createdAt })

    Column(Modifier.fillMaxSize()) {
        ProScreenHeader("أفكاري", "مكتبة شخصية للأفكار التي تريد العودة إليها", "💡") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, "إضافة فكرة") }
        }
        ProSearchField(query, { query = it }, "ابحث في الأفكار والتصنيفات")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = !favoritesOnly, onClick = { favoritesOnly = false }, label = { Text("كل الأفكار") }) }
            item { FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = true }, label = { Text("المفضلة") }, leadingIcon = { Icon(Icons.Rounded.Favorite, null, modifier = Modifier.size(16.dp)) }) }
        }

        if (filtered.isEmpty()) {
            ProEmptyState("💡", "لا توجد أفكار هنا", "أضف فكرة سريعة أو غيّر خيارات البحث.", "إضافة فكرة") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProSummaryStrip(
                        "${state.ideas.size}" to "أفكار",
                        "${state.ideas.count { it.favorite }}" to "مفضلة",
                        "${state.ideas.map { it.category }.distinct().size}" to "تصنيفات"
                    )
                }
                items(filtered, key = { it.id }) { idea ->
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(idea.emoji, fontSize = 24.sp) }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(idea.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(idea.category, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(onClick = { state.toggleIdeaFavorite(idea.id) }) {
                                    Icon(if (idea.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "مفضلة", tint = if (idea.favorite) AppRed else MaterialTheme.colorScheme.onSurface.copy(alpha = .48f))
                                }
                            }
                            if (idea.body.isNotBlank()) Text(idea.body, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f), fontSize = 13.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editing = idea }) { Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("تعديل") }
                                TextButton(onClick = { deleting = idea }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("حذف") }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (creating) ProIdeaEditorDialog(null, { creating = false }) { title, body, emoji, category -> state.addIdea(title, body, emoji, category); creating = false }
    editing?.let { idea -> ProIdeaEditorDialog(idea, { editing = null }) { title, body, emoji, category -> state.updateIdea(idea.id, title, body, emoji, category); editing = null } }
    deleting?.let { idea -> ProConfirmDialog("حذف الفكرة؟", "سيتم حذف \"${idea.title}\" من مكتبتك.", "حذف", onDismiss = { deleting = null }) { state.deleteIdea(idea.id); deleting = null } }
}

@Composable
fun ProfessionalNotesScreen(state: ProfessionalState) {
    var query by rememberSaveable { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProNote?>(null) }
    var deleting by remember { mutableStateOf<ProNote?>(null) }

    val filtered = state.notes
        .filter { query.isBlank() || it.title.contains(query, true) || it.body.contains(query, true) }
        .sortedWith(compareByDescending<ProNote> { it.pinned }.thenByDescending { it.updatedAt })

    Column(Modifier.fillMaxSize()) {
        ProScreenHeader("ملاحظاتي", "التقط المعلومة قبل أن تضيع", "📝") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, "إضافة ملاحظة") }
        }
        ProSearchField(query, { query = it }, "ابحث في الملاحظات")

        if (filtered.isEmpty()) {
            ProEmptyState("📝", "لا توجد ملاحظات", "ابدأ بملاحظة سريعة، وثبّت المهم منها في الأعلى.", "ملاحظة جديدة") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    val container = noteCardColor(note.colorKey)
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = container)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(note.emoji, fontSize = 25.sp)
                                Spacer(Modifier.width(9.dp))
                                Text(note.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                IconButton(onClick = { state.toggleNotePinned(note.id) }) {
                                    Icon(Icons.Rounded.PushPin, "تثبيت", tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = .42f))
                                }
                            }
                            if (note.body.isNotBlank()) Text(note.body, fontSize = 13.sp, lineHeight = 21.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .70f), modifier = Modifier.padding(vertical = 8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editing = note }) { Text("تعديل") }
                                TextButton(onClick = { deleting = note }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("حذف") }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }

    if (creating) ProNoteEditorDialog(null, { creating = false }) { title, body, emoji, color -> state.addNote(title, body, emoji, color); creating = false }
    editing?.let { note -> ProNoteEditorDialog(note, { editing = null }) { title, body, emoji, color -> state.updateNote(note.id, title, body, emoji, color); editing = null } }
    deleting?.let { note -> ProConfirmDialog("حذف الملاحظة؟", "سيتم حذف \"${note.title}\" من جهازك.", "حذف", onDismiss = { deleting = null }) { state.deleteNote(note.id); deleting = null } }
}

@Composable
private fun noteCardColor(key: Int): Color = when (key) {
    1 -> if (MaterialTheme.colorScheme.background.luminance() < .5f) Color(0xFF493C25) else Color(0xFFFFF2D2)
    2 -> if (MaterialTheme.colorScheme.background.luminance() < .5f) Color(0xFF243F34) else Color(0xFFE6F7ED)
    3 -> if (MaterialTheme.colorScheme.background.luminance() < .5f) Color(0xFF253B4C) else Color(0xFFE9F4FF)
    4 -> if (MaterialTheme.colorScheme.background.luminance() < .5f) Color(0xFF4A2E38) else Color(0xFFFFEAF0)
    else -> MaterialTheme.colorScheme.surface
}

@Composable
fun ProfessionalJournalScreen(state: ProfessionalState) {
    val today = LocalDate.now()
    val savedToday = state.journalFor(today)
    var mood by remember(savedToday?.updatedAt) { mutableStateOf(savedToday?.mood ?: "🙂") }
    var gratitude by remember(savedToday?.updatedAt) { mutableStateOf(savedToday?.gratitude ?: "") }
    var focus by remember(savedToday?.updatedAt) { mutableStateOf(savedToday?.focus ?: "") }
    var note by remember(savedToday?.updatedAt) { mutableStateOf(savedToday?.note ?: "") }
    var savedFlag by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<ProJournalEntry?>(null) }
    val moods = listOf("😄", "🙂", "😐", "😔", "😤")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { ProScreenHeader("يومي", "سجل اليوم وارجع إلى أيامك السابقة متى شئت", "📔") }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("اليوم", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(formatArabicDate(today), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        if (savedToday != null) Surface(color = AppGreen.copy(alpha = .13f), shape = RoundedCornerShape(11.dp)) {
                            Text("محفوظ ✓", color = AppGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp))
                        }
                    }
                    Text("كيف كان شعورك؟", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        moods.forEach { item ->
                            Box(
                                Modifier.size(49.dp).background(if (mood == item) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = .055f), CircleShape).clickable { mood = item; savedFlag = false },
                                contentAlignment = Alignment.Center
                            ) { Text(item, fontSize = 25.sp) }
                        }
                    }
                    OutlinedTextField(gratitude, { gratitude = it; savedFlag = false }, label = { Text("شيء ممتن له") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(18.dp))
                    OutlinedTextField(focus, { focus = it; savedFlag = false }, label = { Text("أهم تركيز اليوم") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(18.dp))
                    OutlinedTextField(note, { note = it; savedFlag = false }, label = { Text("ماذا حدث اليوم؟") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(18.dp))
                    Button(
                        onClick = { state.saveJournal(today, mood, gratitude, focus, note); savedFlag = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(17.dp)
                    ) {
                        Icon(Icons.Rounded.Bookmark, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(if (savedFlag) "تم الحفظ" else "حفظ اليوم")
                    }
                }
            }
        }

        item { ProSectionTitle("أرشيف الأيام") }
        if (state.journals.isEmpty()) {
            item { Text("عند حفظ يومك لأول مرة سيظهر هنا.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), modifier = Modifier.fillMaxWidth().padding(20.dp)) }
        } else {
            items(state.journals, key = { it.date }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { preview = entry },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (entry.date == today.toString()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f) else MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) { Text(entry.mood, fontSize = 24.sp) }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            val parsed = runCatching { LocalDate.parse(entry.date) }.getOrNull()
                            Text(parsed?.let { formatArabicDate(it) } ?: entry.date, fontWeight = FontWeight.Bold)
                            val summary = entry.focus.ifBlank { entry.note.ifBlank { entry.gratitude } }
                            Text(summary.ifBlank { "يوم محفوظ" }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("‹", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .35f))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    preview?.let { entry ->
        AlertDialog(
            onDismissRequest = { preview = null },
            shape = RoundedCornerShape(28.dp),
            title = {
                Column {
                    Text("${entry.mood}  يوم محفوظ", fontWeight = FontWeight.Bold)
                    Text(entry.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    JournalPreviewPart("ممتن له", entry.gratitude)
                    JournalPreviewPart("التركيز", entry.focus)
                    JournalPreviewPart("ما حدث", entry.note)
                }
            },
            confirmButton = { TextButton(onClick = { preview = null }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun JournalPreviewPart(title: String, value: String) {
    if (value.isNotBlank()) {
        Column {
            Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun ProfessionalSettingsScreen(state: ProfessionalState, onNavigate: (ProScreen) -> Unit) {
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ProScreenHeader("الإعدادات", "تحكم في التجربة والخصوصية والتذكيرات", "⚙️") }
        item {
            ProSettingCard("الملف الشخصي", Icons.Rounded.Edit) {
                OutlinedTextField(name, { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(17.dp))
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { state.setProfileName(name) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Text("حفظ الاسم") }
                    OutlinedButton(onClick = { onNavigate(ProScreen.Photos) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) { Icon(Icons.Rounded.PhotoLibrary, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("الصورة") }
                }
            }
        }
        item {
            ProSettingCard("المظهر", if (state.darkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("الوضع الداكن", fontWeight = FontWeight.SemiBold)
                        Text("ألوان محسنة وتباين أوضح في الإضاءة المنخفضة", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), fontSize = 11.sp)
                    }
                    Switch(checked = state.darkMode, onCheckedChange = { state.setDarkMode(it) })
                }
            }
        }
        item {
            ProSettingCard("التذكير اليومي", Icons.Rounded.Schedule) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("مراجعة الأهداف والعادات", fontWeight = FontWeight.SemiBold)
                        Text(formatProTime(state.reminderHour, state.reminderMinute), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)
                    ) { Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("الوقت") }
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else NotificationScheduler.showReminder(context)
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)
                    ) { Icon(Icons.Rounded.Notifications, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("اختبار") }
                }
            }
        }
        item {
            ProSettingCard("بياناتي", Icons.Rounded.Share) {
                Text("كل المحتوى الأساسي محفوظ محلياً على جهازك.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 11.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { shareTextPro(context, state.exportText()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Rounded.Share, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("مشاركة ملخص بياناتي")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { resetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("إعادة ضبط التطبيق") }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f))) {
                Column(Modifier.padding(17.dp)) {
                    Text("مساعدي اليومي 3.0", fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(4.dp))
                    Text("إصدار احترافي مع أرشيف يوميات، سجل عادات بالتواريخ، معرض شخصيات متقدم ولوحة يومية موحدة.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f), fontSize = 11.sp, lineHeight = 18.sp)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    if (resetConfirm) ProConfirmDialog("إعادة ضبط التطبيق؟", "سيتم حذف بيانات الأهداف والعادات والصور واليوميات المحفوظة داخل التطبيق وإعادة المحتوى التجريبي.", "إعادة ضبط", onDismiss = { resetConfirm = false }) {
        NotificationScheduler.cancel(context)
        state.resetAll(); name = state.profileName; resetConfirm = false
    }
}

@Composable
private fun ProSettingCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(9.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(13.dp))
            content()
        }
    }
}

@Composable
fun ProfessionalMoreScreen(state: ProfessionalState, onNavigate: (ProScreen) -> Unit) {
    val source = state.profilePhotoUri?.let { PersonSource.ContentUri(it) } ?: PersonSource.Resource(R.drawable.user_sample)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .58f))) {
                Row(Modifier.fillMaxWidth().height(145.dp).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(105.dp), contentAlignment = Alignment.BottomCenter) { PersonCutout(source, Modifier.fillMaxSize(), ContentScale.Fit) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(state.profileName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("مساحتك اليومية الشخصية", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .57f), fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = .7f), shape = RoundedCornerShape(10.dp)) {
                            Text("تقدم اليوم ${(state.todayProgress * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                        }
                    }
                }
            }
        }
        item { ProMenuCard(Icons.Rounded.Lightbulb, "أفكاري", "${state.ideas.size} فكرة • ${state.ideas.count { it.favorite }} مفضلة") { onNavigate(ProScreen.Ideas) } }
        item { ProMenuCard(Icons.Rounded.StickyNote2, "ملاحظاتي", "${state.notes.size} ملاحظة • ${state.notes.count { it.pinned }} مثبتة") { onNavigate(ProScreen.Notes) } }
        item { ProMenuCard(Icons.Rounded.EventNote, "يومي", "${state.journals.size} يوم محفوظ في الأرشيف") { onNavigate(ProScreen.Journal) } }
        item { ProMenuCard(Icons.Rounded.TrackChanges, "العادات", "التزام آخر 7 أيام ${(state.weeklyHabitCompletion * 100).roundToInt()}%") { onNavigate(ProScreen.Habits) } }
        item { ProMenuCard(Icons.Rounded.PhotoLibrary, "صوري", "${state.photos.size} صورة في معرض الشخصيات") { onNavigate(ProScreen.Photos) } }
        item { ProMenuCard(Icons.Rounded.Settings, "الإعدادات", "المظهر، الملف الشخصي، التذكيرات والبيانات") { onNavigate(ProScreen.Settings) } }
        item { Spacer(Modifier.height(8.dp)) }
    }
}
