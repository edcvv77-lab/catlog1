package com.aiham.dailycompanion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyCompanionTheme {
                CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val state = remember { AppState(applicationContext) }
                    DailyCompanionApp(state)
                }
            }
        }
    }
}

private enum class AppScreen(val label: String) {
    Home("الرئيسية"), Goals("أهدافي"), Ideas("أفكار"), Notes("ملاحظاتي"),
    Habits("متتبع السلوك"), Photos("صوري"), Ticket("تذكرة"), More("المزيد")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyCompanionApp(state: AppState) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
    var addSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBackground,
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
        Surface(Modifier.fillMaxSize().padding(inner), color = AppBackground) {
            when (screen) {
                AppScreen.Home -> HomeScreen(state) { screen = it }
                AppScreen.Goals -> GoalsScreen(state)
                AppScreen.Ideas -> IdeasScreen(state)
                AppScreen.Notes -> NotesScreen(state)
                AppScreen.Habits -> HabitsScreen(state)
                AppScreen.Photos -> PhotosScreen(state)
                AppScreen.Ticket -> TicketScreen()
                AppScreen.More -> MoreScreen(state) { screen = it }
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
                AddSheetRow("هدف جديد", "🎯") { addSheet = false; screen = AppScreen.Goals }
                AddSheetRow("فكرة جديدة", "💡") { addSheet = false; screen = AppScreen.Ideas }
                AddSheetRow("ملاحظة لحظية", "📝") { addSheet = false; screen = AppScreen.Notes }
                AddSheetRow("صورة شخصية", "📷") { addSheet = false; screen = AppScreen.Photos }
                Spacer(Modifier.height(18.dp))
            }
        }
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
    Surface(shadowElevation = 14.dp, color = Color.White) {
        NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
            BottomNavItem("الرئيسية", Icons.Rounded.Home, current == AppScreen.Home, onHome)
            BottomNavItem("أهدافي", Icons.Rounded.TrackChanges, current == AppScreen.Goals, onGoals)
            NavigationBarItem(
                selected = false,
                onClick = onAdd,
                icon = {
                    Box(Modifier.size(48.dp).background(AppPurple, CircleShape), contentAlignment = Alignment.Center) {
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
            selectedIconColor = AppPurple,
            selectedTextColor = AppPurple,
            indicatorColor = AppLavender,
            unselectedIconColor = AppMuted,
            unselectedTextColor = AppMuted
        )
    )
}

@Composable
private fun AddSheetRow(title: String, emoji: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFF6F5FB)).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 25.sp)
        Spacer(Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.Add, contentDescription = null, tint = AppPurple)
    }
}

@Composable
private fun ScreenHeader(title: String, emoji: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("$emoji  $title", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = AppMuted, fontSize = 13.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Rounded.MoreHoriz, contentDescription = null, tint = AppMuted) }
    }
}

@Composable
private fun HomeScreen(state: AppState, go: (AppScreen) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HomeGreeting(state.profileName) }
        item { HeroCard() }
        item {
            SectionTitle("أهداف اليوم", "عرض الكل") { go(AppScreen.Goals) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.goals.take(4).forEachIndexed { index, goal ->
                        GoalMini(goal, index)
                        if (index < state.goals.take(4).lastIndex) HorizontalDivider(color = Color(0xFFF0F0F4))
                    }
                }
            }
        }
        item {
            SectionTitle("صوري وأفكاري", "استكشف") { go(AppScreen.Photos) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoPeekCard(Modifier.weight(1f)) { go(AppScreen.Photos) }
                InspirationPeekCard(Modifier.weight(1f)) { go(AppScreen.Ideas) }
            }
        }
        item { SectionTitle("وصول سريع"); QuickActions(go) }
        item {
            SectionTitle("متتبع السلوك", "هذا الأسبوع") { go(AppScreen.Habits) }
            HabitPreview(state) { go(AppScreen.Habits) }
        }
        item { Spacer(Modifier.height(6.dp)) }
    }
}

@Composable
private fun HomeGreeting(name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(54.dp).background(AppLavender, CircleShape).padding(3.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            PersonCutout(PersonSource.Resource(R.drawable.user_sample), Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("مرحباً، $name 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("جاهز لتحقيق يوم رائع؟", color = AppMuted, fontSize = 13.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Rounded.NotificationsNone, contentDescription = "التنبيهات", tint = AppPurple) }
    }
}

@Composable
private fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = AppLavender)
    ) {
        Row(Modifier.fillMaxSize().padding(start = 18.dp, end = 10.dp, top = 14.dp)) {
            Column(Modifier.weight(1f).padding(vertical = 16.dp), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AppPurple)
                    Spacer(Modifier.width(6.dp)); Text("فكرة اليوم", color = AppPurple, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text("لا تنتظر الفرصة،\nبل اصنعها بنفسك.", fontSize = 23.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
                Spacer(Modifier.height(10.dp))
                Text("خطوة صغيرة اليوم تصنع فرقاً كبيراً غداً.", color = AppMuted, fontSize = 12.sp)
            }
            Box(Modifier.weight(.9f).fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                PersonCutout(PersonSource.Resource(R.drawable.user_sample), Modifier.fillMaxSize(), ContentScale.Fit)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action, color = AppPurple, fontSize = 12.sp) }
    }
}

@Composable
private fun GoalMini(goal: GoalItem, index: Int) {
    val tint = listOf(Color(0xFF6B5CE7), Color(0xFFF1AA2B), Color(0xFF4A9BF1), Color(0xFF35B979))[index % 4]
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).background(tint.copy(alpha = .12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Text(goal.emoji, fontSize = 25.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(goal.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier.weight(1f).height(7.dp).clip(CircleShape),
                    color = tint,
                    trackColor = Color(0xFFF0F0F5),
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(8.dp)); Text("${(goal.progress * 100).roundToInt()}%", color = AppMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PhotoPeekCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Sky)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.padding(14.dp)) {
                Text("📷  صوري", fontWeight = FontWeight.Bold)
                Text("الشخصية فقط بدون خلفية", color = AppMuted, fontSize = 11.sp)
            }
            PersonCutout(PersonSource.Resource(R.drawable.user_sample), Modifier.fillMaxSize().padding(top = 36.dp), ContentScale.Fit)
        }
    }
}

@Composable
private fun InspirationPeekCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Mint)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("💡  فكرة ملهمة", fontWeight = FontWeight.Bold)
            Text("كل يوم بداية جديدة\nلفكرة عظيمة.", fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 27.sp)
            Text("اعرض المزيد ←", color = AppPurple, fontSize = 12.sp)
        }
    }
}

@Composable
private fun QuickActions(go: (AppScreen) -> Unit) {
    val entries = listOf(
        Triple("فكرة جديدة", "💡", AppScreen.Ideas), Triple("ملاحظة سريعة", "📝", AppScreen.Notes),
        Triple("تذكرة", "🎟️", AppScreen.Ticket), Triple("متتبع السلوك", "📈", AppScreen.Habits),
        Triple("صوري", "📷", AppScreen.Photos), Triple("المزيد", "•••", AppScreen.More)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.chunked(3).forEach { rowEntries ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowEntries.forEach { (label, emoji, destination) ->
                    Card(
                        modifier = Modifier.weight(1f).height(92.dp).clickable { go(destination) },
                        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(Modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(emoji, fontSize = 24.sp); Spacer(Modifier.height(6.dp))
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitPreview(state: AppState, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.habits.take(4).forEach { habit ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(habit.emoji, fontSize = 20.sp); Spacer(Modifier.width(8.dp))
                    Text(habit.name, fontSize = 13.sp, modifier = Modifier.width(82.dp), fontWeight = FontWeight.Medium)
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(7) { day ->
                            val done = habit.doneMask and (1 shl day) != 0
                            Box(Modifier.size(23.dp).background(if (done) Color(0xFF43C884) else Color(0xFFF1F1F5), CircleShape), contentAlignment = Alignment.Center) {
                                if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsScreen(state: AppState) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("أهدافي", "🎯", "اضغط على أي هدف لزيادة تقدمه")
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.goals, key = { it.id }) { goal -> GoalCard(goal) { state.advanceGoal(goal.id) } }
            item { Spacer(Modifier.height(76.dp)) }
        }
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Button(
                onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = AppPurple)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("إضافة هدف جديد", fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showAdd) {
        OneFieldDialog("هدف جديد", "مثال: المشي 30 دقيقة يومياً", onDismiss = { showAdd = false }) {
            state.addGoal(it); showAdd = false
        }
    }
}

@Composable
private fun GoalCard(goal: GoalItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(70.dp).background(AppLavender, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) { Text(goal.emoji, fontSize = 32.sp) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, fontWeight = FontWeight.Bold); Text(goal.detail, color = AppMuted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { goal.progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = AppPurple, trackColor = Color(0xFFF0EFF7)
                )
                Spacer(Modifier.height(5.dp)); Text("${(goal.progress * 100).roundToInt()}% مكتمل", color = AppPurple, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun IdeasScreen(state: AppState) {
    var add by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("أفكار ملهمة", "💡", "احتفظ بما يشعل حماسك")
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.ideas, key = { it.id }) { idea -> InspirationCard(idea) }
            item { Spacer(Modifier.height(70.dp)) }
        }
        Button(onClick = { add = true }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("أضف فكرة")
        }
    }
    if (add) {
        TwoFieldDialog("فكرة جديدة", "عنوان الفكرة", "اكتب الفكرة أو الإلهام هنا...", { add = false }) { title, body ->
            state.addIdea(title, body); add = false
        }
    }
}

@Composable
private fun InspirationCard(idea: IdeaItem) {
    val backgrounds = listOf(AppLavender, Mint, Butter, Sky, Peach, Rose)
    val idx = (idea.id.hashCode() and Int.MAX_VALUE) % backgrounds.size
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = backgrounds[idx])) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(idea.emoji, fontSize = 34.sp); Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(idea.title, fontWeight = FontWeight.Bold, fontSize = 18.sp); Spacer(Modifier.height(5.dp))
                Text(idea.body, color = AppMuted, lineHeight = 21.sp, fontSize = 13.sp)
            }
            Icon(Icons.Rounded.BookmarkBorder, contentDescription = null, tint = AppPurple)
        }
    }
}

@Composable
private fun NotesScreen(state: AppState) {
    var add by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("الملاحظات اللحظية", "📝", "دوّنها قبل أن تضيع")
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.notes, key = { it.id }) { note ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(16.dp)) {
                        Text(note.emoji, fontSize = 28.sp); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(note.title, fontWeight = FontWeight.Bold); Spacer(Modifier.height(5.dp))
                            Text(note.body, color = AppMuted, fontSize = 13.sp, lineHeight = 21.sp)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(70.dp)) }
        }
        Button(onClick = { add = true }, modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp), shape = RoundedCornerShape(18.dp)) {
            Icon(Icons.Rounded.StickyNote2, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("ملاحظة جديدة")
        }
    }
    if (add) {
        TwoFieldDialog("ملاحظة جديدة", "عنوان اختياري", "اكتب ملاحظتك...", { add = false }) { title, body ->
            state.addNote(title, body); add = false
        }
    }
}

@Composable
private fun HabitsScreen(state: AppState) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("متتبع السلوك", "📈", "الأحد — السبت")
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Spacer(Modifier.width(100.dp))
                    listOf("ح", "ن", "ث", "ر", "خ", "ج", "س").forEach {
                        Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = AppMuted, fontSize = 11.sp)
                    }
                }
            }
            items(state.habits, key = { it.id }) { habit ->
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${habit.emoji} ${habit.name}", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        repeat(7) { day ->
                            val done = habit.doneMask and (1 shl day) != 0
                            Box(Modifier.weight(1f).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier.size(30.dp).background(if (done) Color(0xFF45C785) else Color(0xFFF2F2F6), CircleShape).clickable { state.toggleHabit(habit.id, day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (done) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = AppLavender)) {
                    Row(Modifier.padding(20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                        Stat("87%", "معدل الالتزام"); Stat("12", "أفضل سلسلة"); Stat("28", "إجمالي الأيام")
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppPurple)
        Text(label, color = AppMuted, fontSize = 10.sp)
    }
}

@Composable
private fun PhotosScreen(state: AppState) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            state.addPhotoUri(uri.toString())
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("صوري", "📷", "التطبيق يزيل الخلفية تلقائياً ويُبقي الشخصية بارزة")
        Card(
            modifier = Modifier.fillMaxWidth().height(210.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = AppLavender)
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("صورتك جزء من الواجهة", fontWeight = FontWeight.Bold, fontSize = 20.sp); Spacer(Modifier.height(7.dp))
                    Text("بدون خلفية، بدون إطار تقليدي، وبأسلوب بارز مثل الأيقونات.", color = AppMuted, fontSize = 12.sp, lineHeight = 19.sp)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { picker.launch(arrayOf("image/*")) }, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("إضافة صورة")
                    }
                }
                Box(Modifier.weight(.8f).fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    PersonCutout(PersonSource.Resource(R.drawable.user_sample), Modifier.fillMaxSize(), ContentScale.Fit)
                }
            }
        }

        if (state.photoUris.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = AppPurple, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(12.dp)); Text("أضف صورك الشخصية", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("ستظهر هنا بعد فصل الخلفية تلقائياً.", color = AppMuted, fontSize = 13.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                gridItems(state.photoUris, key = { it }) { uri -> PhotoCutoutTile(uri, onDelete = { state.removePhotoUri(uri) }) }
            }
        }
    }
}

@Composable
private fun PhotoCutoutTile(uri: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(.82f), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            PersonCutout(PersonSource.ContentUri(uri), Modifier.fillMaxSize().padding(8.dp), ContentScale.Fit)
            FilledIconButton(
                onClick = onDelete, modifier = Modifier.align(Alignment.TopStart).padding(7.dp).size(30.dp),
                colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(alpha = .9f))
            ) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFB64A54), modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun TicketScreen() {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("تذكرة", "🎟️", "رسالة تحفظها معك")
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp), shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = AppLavender), elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = AppPurple, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(18.dp)); Text("تذكّر دائماً!", color = AppPurple, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Spacer(Modifier.height(24.dp))
                Text("كل يوم فرصة جديدة\nلتكون نسخة أفضل\nمن نفسك.", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 39.sp)
                Spacer(Modifier.height(24.dp)); HorizontalDivider(color = AppPurple.copy(alpha = .15f)); Spacer(Modifier.height(18.dp))
                Barcode(Modifier.fillMaxWidth().height(56.dp)); Spacer(Modifier.height(8.dp))
                Text("DAILY • 2026 • YOU", letterSpacing = 2.sp, color = AppMuted, fontSize = 9.sp)
            }
        }
        Spacer(Modifier.height(24.dp)); Text("ملاحظات", fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.padding(horizontal = 24.dp))
        Card(Modifier.fillMaxWidth().padding(24.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Text("اكتب لنفسك سبباً واحداً للاستمرار اليوم.", modifier = Modifier.padding(18.dp), color = AppMuted)
        }
    }
}

@Composable
private fun Barcode(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val bars = listOf(1,2,1,3,1,1,2,3,1,2,1,1,3,2,1,3,1,2,2,1,3,1,1,2,1,3,2,1,2,1,3,1,2)
        var x = 0f
        val unit = size.width / (bars.sum() + bars.size * .8f)
        bars.forEachIndexed { index, w ->
            val width = w * unit
            if (index % 2 == 0 || index % 3 == 0) {
                drawRect(Color(0xFF1D1D23), topLeft = Offset(x, 0f), size = androidx.compose.ui.geometry.Size(width, size.height))
            }
            x += width + unit * .8f
        }
    }
}

@Composable
private fun MoreScreen(state: AppState, go: (AppScreen) -> Unit) {
    var editName by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("المزيد", "•••")
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 16.dp), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppLavender)
        ) {
            Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(82.dp), contentAlignment = Alignment.BottomCenter) {
                    PersonCutout(PersonSource.Resource(R.drawable.user_sample), Modifier.fillMaxSize())
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.profileName, fontWeight = FontWeight.Bold, fontSize = 20.sp); Text("مستخدم نشط ✨", color = AppMuted, fontSize = 12.sp)
                }
                IconButton(onClick = { editName = true }) { Icon(Icons.Rounded.Edit, contentDescription = "تعديل الاسم", tint = AppPurple) }
            }
        }
        Spacer(Modifier.height(16.dp))
        MoreRow("أهدافي", "🎯") { go(AppScreen.Goals) }; MoreRow("أفكاري", "💡") { go(AppScreen.Ideas) }
        MoreRow("ملاحظاتي", "📝") { go(AppScreen.Notes) }; MoreRow("متتبع السلوك", "📈") { go(AppScreen.Habits) }
        MoreRow("صوري", "📷") { go(AppScreen.Photos) }; MoreRow("التنبيهات", "🔔") { }; MoreRow("الإعدادات", "⚙️") { }
        Spacer(Modifier.height(22.dp)); Text("الإصدار 1.0.0", color = AppMuted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(Modifier.height(24.dp))
    }
    if (editName) {
        OneFieldDialog("تعديل الاسم", state.profileName, onDismiss = { editName = false }) { state.setProfileName(it); editName = false }
    }
}

@Composable
private fun MoreRow(label: String, emoji: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 22.sp); Spacer(Modifier.width(12.dp)); Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("‹", color = AppMuted, fontSize = 23.sp)
        }
    }
}

@Composable
private fun OneFieldDialog(title: String, hint: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = value, onValueChange = { value = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(hint) },
                shape = RoundedCornerShape(16.dp), singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(value.ifBlank { hint }) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun TwoFieldDialog(
    title: String,
    titleHint: String,
    bodyHint: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(first, { first = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(titleHint) }, singleLine = true, shape = RoundedCornerShape(16.dp))
                OutlinedTextField(second, { second = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text(bodyHint) }, minLines = 3, shape = RoundedCornerShape(16.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(first, second) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
