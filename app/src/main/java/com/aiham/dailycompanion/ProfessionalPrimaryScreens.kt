package com.aiham.dailycompanion

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun ProfessionalHomeScreen(state: ProfessionalState, onNavigate: (ProScreen) -> Unit) {
    val source = state.profilePhotoUri?.let { PersonSource.ContentUri(it) } ?: PersonSource.Resource(R.drawable.user_sample)
    val progress = state.todayProgress
    val goal = state.topActiveGoal
    val featuredPhoto = state.photos.sortedWith(compareByDescending<ProPhoto> { it.favorite }.thenByDescending { it.createdAt }).firstOrNull()
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(58.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(2.dp),
                    contentAlignment = Alignment.BottomCenter
                ) { PersonCutout(source, Modifier.fillMaxSize()) }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("مرحباً، ${state.profileName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(formatArabicDate(LocalDate.now()), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 12.sp)
                }
                IconButton(onClick = { onNavigate(ProScreen.Settings) }) {
                    Icon(Icons.Rounded.Settings, contentDescription = "الإعدادات", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().height(230.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(Modifier.fillMaxSize().padding(start = 18.dp, end = 6.dp, top = 12.dp)) {
                    Column(
                        Modifier.weight(1.08f).fillMaxHeight().padding(vertical = 14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = .11f), shape = RoundedCornerShape(12.dp)) {
                                Text("لوحة اليوم", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                            }
                            Spacer(Modifier.height(11.dp))
                            Text("أنجز يومك\nبهدوء ووضوح", fontSize = 24.sp, lineHeight = 31.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(58.dp),
                                    strokeWidth = 7.dp,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f)
                                )
                                Text("${(progress * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("تقدم اليوم", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("أهداف + عادات + يوميات", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f), fontSize = 10.sp)
                            }
                        }
                    }
                    Box(Modifier.weight(.86f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                        PersonCutout(source, Modifier.fillMaxSize(), ContentScale.Fit)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProMetricCard("${state.goals.count { it.progress >= 1f }}", "أهداف مكتملة", "🎯", Modifier.weight(1f))
                ProMetricCard("${(state.todayHabitCompletion * 100).roundToInt()}%", "عادات اليوم", "✅", Modifier.weight(1f))
                ProMetricCard("${state.journals.size}", "يوم محفوظ", "📔", Modifier.weight(1f))
            }
        }

        if (goal != null) {
            item {
                ProSectionTitle("الأولوية الآن", "كل الأهداف") { onNavigate(ProScreen.Goals) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(ProScreen.Goals) },
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) { Text(goal.emoji, fontSize = 25.sp) }
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(goal.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(goal.detail.ifBlank { "حدد خطوة صغيرة وأنجزها الآن" }, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .56f), fontSize = 11.sp, maxLines = 2)
                            }
                            Text("${(goal.progress * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                        }
                        LinearProgressIndicator(
                            progress = { goal.progress },
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .07f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text(goal.category, fontSize = 10.sp) })
                            if (goal.deadline.isNotBlank()) AssistChip(onClick = {}, label = { Text(goal.deadline, fontSize = 10.sp) }, leadingIcon = { Icon(Icons.Rounded.Timer, null, modifier = Modifier.size(15.dp)) })
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                state.toggleGoalComplete(goal.id)
                            }) { Text("${if (goal.progress >= 1f) "إعادة فتح" else "تم ✓"}") }
                        }
                    }
                }
            }
        }

        item {
            ProSectionTitle("عادات اليوم", "التفاصيل") { onNavigate(ProScreen.Habits) }
            Card(
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (state.habits.isEmpty()) {
                        Text("لا توجد عادات بعد", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
                    } else {
                        state.habits.take(4).forEachIndexed { index, habit ->
                            val done = state.isHabitDone(habit.id)
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    state.toggleHabitDate(habit.id)
                                }.padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(habit.emoji, fontSize = 22.sp)
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(habit.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("سلسلة ${state.habitStreak(habit)} يوم", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f), fontSize = 10.sp)
                                }
                                Box(
                                    Modifier.size(34.dp).background(if (done) AppGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = .07f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) { if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                            }
                            if (index < state.habits.take(4).lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .05f))
                        }
                    }
                }
            }
        }

        if (featuredPhoto != null) {
            item {
                ProSectionTitle("من صورك", "فتح المعرض") { onNavigate(ProScreen.Photos) }
                Card(
                    modifier = Modifier.fillMaxWidth().height(210.dp).clickable { onNavigate(ProScreen.Photos) },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .55f))
                ) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Column(Modifier.weight(1f).padding(vertical = 12.dp), verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(9.dp))
                            Text(featuredPhoto.caption.ifBlank { "صورة تعني لك شيئاً" }, fontSize = 19.sp, fontWeight = FontWeight.Bold, lineHeight = 27.sp)
                            Spacer(Modifier.height(7.dp))
                            Text("اجعل صورك جزءاً من يومك، لا مجرد ملفات في المعرض.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f), fontSize = 11.sp, lineHeight = 18.sp)
                        }
                        Box(Modifier.weight(.8f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                            PersonCutout(PersonSource.ContentUri(featuredPhoto.uri), Modifier.fillMaxSize(), ContentScale.Fit)
                        }
                    }
                }
            }
        }

        state.pinnedNote?.let { note ->
            item {
                ProSectionTitle("ملاحظة قريبة", "الملاحظات") { onNavigate(ProScreen.Notes) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(ProScreen.Notes) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        Text(note.emoji, fontSize = 26.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(note.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(note.body, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .64f), fontSize = 12.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        state.featuredIdea?.let { idea ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(ProScreen.Ideas) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .48f))
                ) {
                    Column(Modifier.padding(17.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(idea.emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(8.dp))
                            Text("فكرة ملهمة", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(idea.title, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                        if (idea.body.isNotBlank()) Text(idea.body, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .62f), fontSize = 12.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun ProMetricCard(value: String, label: String, emoji: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f), textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

private enum class GoalView { All, Active, Done }

@Composable
fun ProfessionalGoalsScreen(state: ProfessionalState) {
    var query by rememberSaveable { mutableStateOf("") }
    var view by rememberSaveable { mutableStateOf(GoalView.All) }
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProGoal?>(null) }
    var deleting by remember { mutableStateOf<ProGoal?>(null) }
    val haptics = LocalHapticFeedback.current

    val filtered = state.goals.filter { goal ->
        val matchesText = query.isBlank() || goal.title.contains(query, true) || goal.detail.contains(query, true) || goal.category.contains(query, true)
        val matchesView = when (view) {
            GoalView.All -> true
            GoalView.Active -> goal.progress < 1f
            GoalView.Done -> goal.progress >= 1f
        }
        matchesText && matchesView
    }

    Column(Modifier.fillMaxSize()) {
        ProScreenHeader("أهدافي", "حوّل الهدف الكبير إلى تقدم يمكن رؤيته", "🎯") {
            FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, "إضافة هدف") }
        }
        ProSearchField(query, { query = it }, "ابحث بالهدف أو التصنيف")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FilterChip(selected = view == GoalView.All, onClick = { view = GoalView.All }, label = { Text("الكل") }) }
            item { FilterChip(selected = view == GoalView.Active, onClick = { view = GoalView.Active }, label = { Text("قيد التنفيذ") }) }
            item { FilterChip(selected = view == GoalView.Done, onClick = { view = GoalView.Done }, label = { Text("مكتمل") }) }
        }

        if (filtered.isEmpty()) {
            ProEmptyState("🎯", "لا توجد أهداف هنا", "غيّر الفلتر أو أضف هدفاً واضحاً تستطيع قياسه.", "إضافة هدف") { creating = true }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProSummaryStrip(
                        "${state.goals.size}" to "إجمالي",
                        "${state.goals.count { it.progress >= 1f }}" to "مكتمل",
                        "${(state.activeGoalAverage * 100).roundToInt()}%" to "متوسط النشط"
                    )
                }
                items(filtered, key = { it.id }) { goal ->
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(50.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text(goal.emoji, fontSize = 24.sp) }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(goal.category, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        if (goal.deadline.isNotBlank()) Text("• ${goal.deadline}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f), fontSize = 10.sp)
                                    }
                                }
                                IconButton(onClick = { editing = goal }) { Icon(Icons.Rounded.Edit, "تعديل", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f)) }
                                IconButton(onClick = { deleting = goal }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error) }
                            }
                            if (goal.detail.isNotBlank()) Text(goal.detail, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f), fontSize = 12.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${(goal.progress * 100).roundToInt()}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(48.dp))
                                Slider(value = goal.progress, onValueChange = { state.setGoalProgress(goal.id, it) }, modifier = Modifier.weight(1f))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (goal.progress >= 1f) AppGreen.copy(alpha = .14f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .65f),
                                    shape = RoundedCornerShape(11.dp)
                                ) {
                                    Text(
                                        if (goal.progress >= 1f) "مكتمل ✓" else "قيد التنفيذ",
                                        color = if (goal.progress >= 1f) AppGreen else MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    state.toggleGoalComplete(goal.id)
                                }) {
                                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (goal.progress >= 1f) "إعادة فتح" else "إكمال")
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(10.dp)) }
            }
        }
    }

    if (creating) ProGoalEditorDialog(null, { creating = false }) { title, emoji, detail, progress, category, deadline ->
        state.addGoal(title, emoji, detail, progress, category, deadline); creating = false
    }
    editing?.let { goal ->
        ProGoalEditorDialog(goal, { editing = null }) { title, emoji, detail, progress, category, deadline ->
            state.updateGoal(goal.id, title, emoji, detail, progress, category, deadline); editing = null
        }
    }
    deleting?.let { goal ->
        ProConfirmDialog("حذف الهدف؟", "سيُحذف \"${goal.title}\" من جهازك. لا يمكن التراجع بعد إغلاق النافذة.", "حذف", onDismiss = { deleting = null }) {
            state.deleteGoal(goal.id); deleting = null
        }
    }
}

@Composable
fun ProfessionalHabitsScreen(state: ProfessionalState) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ProHabit?>(null) }
    var deleting by remember { mutableStateOf<ProHabit?>(null) }
    var resetConfirm by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val days = state.last7Dates
    val bestCurrent = state.habits.maxOfOrNull { state.habitStreak(it) } ?: 0

    Column(Modifier.fillMaxSize()) {
        ProScreenHeader("العادات", "سجل يومك فعلياً، مع سلسلة تمتد بين الأسابيع", "✅") {
            Row {
                IconButton(onClick = { resetConfirm = true }) { Icon(Icons.Rounded.RestartAlt, "مسح آخر 7 أيام") }
                FilledIconButton(onClick = { creating = true }) { Icon(Icons.Rounded.Add, "إضافة عادة") }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProSummaryStrip(
                    "${(state.todayHabitCompletion * 100).roundToInt()}%" to "اليوم",
                    "$bestCurrent" to "أفضل سلسلة حالية",
                    "${(state.weeklyHabitCompletion * 100).roundToInt()}%" to "آخر 7 أيام"
                )
            }
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .46f))) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("ar")).take(2), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
                                Text("${day.dayOfMonth}", fontWeight = if (day == LocalDate.now()) FontWeight.ExtraBold else FontWeight.SemiBold, color = if (day == LocalDate.now()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (state.habits.isEmpty()) {
                item { ProEmptyState("✅", "لا توجد عادات", "أضف عادة بسيطة وابدأ تسجيلها من اليوم.", "إضافة عادة") { creating = true } }
            } else {
                items(state.habits, key = { it.id }) { habit ->
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(15.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text(habit.emoji, fontSize = 24.sp) }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(habit.name, fontWeight = FontWeight.Bold)
                                    Text("سلسلة ${state.habitStreak(habit)} • الأفضل ${state.habitBestStreak(habit)}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .54f), fontSize = 10.sp)
                                }
                                IconButton(onClick = { editing = habit }) { Icon(Icons.Rounded.Edit, "تعديل", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)) }
                                IconButton(onClick = { deleting = habit }) { Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error) }
                            }
                            Spacer(Modifier.height(13.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                days.forEach { day ->
                                    val done = day.toString() in habit.doneDates
                                    Box(
                                        Modifier
                                            .size(36.dp)
                                            .background(if (done) AppGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = .065f), CircleShape)
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                state.toggleHabitDate(habit.id, day)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (done) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        else Text("${day.dayOfMonth}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .46f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }

    if (creating) ProHabitEditorDialog(null, { creating = false }) { name, emoji -> state.addHabit(name, emoji); creating = false }
    editing?.let { habit -> ProHabitEditorDialog(habit, { editing = null }) { name, emoji -> state.updateHabit(habit.id, name, emoji); editing = null } }
    deleting?.let { habit -> ProConfirmDialog("حذف العادة؟", "سيُحذف سجل \"${habit.name}\" المحفوظ معها.", "حذف", onDismiss = { deleting = null }) { state.deleteHabit(habit.id); deleting = null } }
    if (resetConfirm) ProConfirmDialog("مسح آخر 7 أيام؟", "ستبقى العادات نفسها، لكن علامات الإنجاز في آخر سبعة أيام ستُمسح.", "مسح", onDismiss = { resetConfirm = false }) {
        state.resetCurrentWeek(); resetConfirm = false
    }
}

@Composable
fun ProfessionalPhotosScreen(state: ProfessionalState) {
    val context = LocalContext.current
    var selectedId by rememberSaveable { mutableStateOf(state.photos.firstOrNull()?.id) }
    var deleting by remember { mutableStateOf<ProPhoto?>(null) }
    var captioning by remember { mutableStateOf<ProPhoto?>(null) }
    var captionText by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            state.addPhotoUri(uri.toString())
            selectedId = state.photos.firstOrNull()?.id
        }
    }

    val selected = state.photos.firstOrNull { it.id == selectedId } ?: state.photos.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("📷  صوري", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("الشخصية نفسها تصبح جزءاً من تصميم التطبيق", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 12.sp)
                }
                FilledIconButton(onClick = { launcher.launch(arrayOf("image/*")) }) { Icon(Icons.Rounded.AddAPhoto, "إضافة صورة") }
            }
        }

        if (selected == null) {
            item { ProEmptyState("📸", "أضف أول صورة", "اختر صورة واضحة. سيُعرض الشخص بدون الخلفية كعنصر بارز داخل التطبيق.", "اختيار صورة") { launcher.launch(arrayOf("image/*")) } }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .64f))
                ) {
                    Box(Modifier.fillMaxSize()) {
                        PersonCutout(PersonSource.ContentUri(selected.uri), Modifier.fillMaxSize().padding(top = 16.dp, start = 10.dp, end = 10.dp), ContentScale.Fit)
                        Row(
                            Modifier.align(Alignment.TopEnd).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = .90f), shape = CircleShape) {
                                IconButton(onClick = { state.togglePhotoFavorite(selected.id) }) {
                                    Icon(if (selected.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, "مفضلة", tint = if (selected.favorite) AppRed else MaterialTheme.colorScheme.primary)
                                }
                            }
                            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = .90f), shape = CircleShape) {
                                IconButton(onClick = { captionText = selected.caption; captioning = selected }) { Icon(Icons.Rounded.EditNote, "تعليق") }
                            }
                        }
                        Column(
                            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(MaterialTheme.colorScheme.surface.copy(alpha = .92f)).padding(14.dp)
                        ) {
                            Text(selected.caption.ifBlank { "لحظة خاصة" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(7.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { state.setProfilePhoto(selected) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) {
                                    Icon(Icons.Rounded.Image, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text(if (state.profilePhotoUri == selected.uri) "الصورة الرئيسية ✓" else "اجعلها الرئيسية", fontSize = 11.sp)
                                }
                                OutlinedButton(onClick = { deleting = selected }, shape = RoundedCornerShape(15.dp)) {
                                    Icon(Icons.Rounded.DeleteOutline, "حذف", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("معرض الشخصيات", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${state.photos.size} صورة", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .52f), fontSize = 11.sp)
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 4.dp)) {
                    items(state.photos, key = { it.id }) { photo ->
                        val isSelected = photo.id == selected.id
                        Card(
                            modifier = Modifier.width(138.dp).height(190.dp).clickable { selectedId = photo.id },
                            shape = RoundedCornerShape(23.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        ) {
                            Box(Modifier.fillMaxSize()) {
                                PersonCutout(PersonSource.ContentUri(photo.uri), Modifier.fillMaxSize().padding(7.dp), ContentScale.Fit)
                                if (photo.favorite) Surface(modifier = Modifier.align(Alignment.TopStart).padding(7.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = .9f), shape = CircleShape) {
                                    Icon(Icons.Rounded.Favorite, null, tint = AppRed, modifier = Modifier.padding(6.dp).size(15.dp))
                                }
                                if (state.profilePhotoUri == photo.uri) Surface(modifier = Modifier.align(Alignment.BottomCenter).padding(7.dp), color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(9.dp)) {
                                    Text("الرئيسية", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("المعالجة تتم على الجهاز. الصورة الأصلية لا تُحذف عندما تحذفها من معرض التطبيق.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 11.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }

    deleting?.let { photo ->
        ProConfirmDialog("إزالة الصورة؟", "ستُزال من مساعدي اليومي فقط، ولن تُحذف الصورة الأصلية من هاتفك.", "إزالة", onDismiss = { deleting = null }) {
            state.removePhoto(photo.id); selectedId = state.photos.firstOrNull()?.id; deleting = null
        }
    }

    captioning?.let { photo ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { captioning = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("تعليق الصورة", fontWeight = FontWeight.Bold) },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("ما الذي تعنيه هذه الصورة؟") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = { Button(onClick = { state.updatePhotoCaption(photo.id, captionText); captioning = null }) { Text("حفظ") } },
            dismissButton = { TextButton(onClick = { captioning = null }) { Text("إلغاء") } }
        )
    }
}
