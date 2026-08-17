package com.aiham.dailycompanion

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

enum class ProQuickType(val title: String, val emoji: String) {
    Goal("هدف جديد", "🎯"),
    Idea("فكرة جديدة", "💡"),
    Note("ملاحظة جديدة", "📝"),
    Habit("عادة جديدة", "✅")
}

@Composable
internal fun ProScreenHeader(
    title: String,
    subtitle: String,
    emoji: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("$emoji  $title", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .60f), fontSize = 12.sp)
        }
        action?.invoke()
    }
}

@Composable
internal fun ProSectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action, fontSize = 12.sp) }
    }
}

@Composable
internal fun ProSearchField(value: String, onValueChange: (String) -> Unit, hint: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text(hint) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
internal fun ProSummaryStrip(vararg values: Pair<String, String>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .75f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            values.forEach { (value, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(value, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(2.dp))
                    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
internal fun ProEmptyState(emoji: String, title: String, body: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 42.sp) }
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            body,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 9.dp),
            lineHeight = 20.sp
        )
        Button(onClick = onAction, shape = RoundedCornerShape(16.dp)) { Text(action) }
    }
}

@Composable
internal fun ProMenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f), fontSize = 11.sp)
            }
            Text("‹", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .35f))
        }
    }
}

@Composable
internal fun ProGoalEditorDialog(
    item: ProGoal?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Float, String, String) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "🎯") }
    var detail by remember(item?.id) { mutableStateOf(item?.detail ?: "") }
    var progress by remember(item?.id) { mutableStateOf(item?.progress ?: 0f) }
    var category by remember(item?.id) { mutableStateOf(item?.category ?: "شخصي") }
    var deadline by remember(item?.id) { mutableStateOf(item?.deadline ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (item == null) "إضافة هدف" else "تعديل الهدف", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(84.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("عنوان الهدف") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(detail, { detail = it }, label = { Text("تفاصيل أو خطوة تالية") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(category, { category = it }, label = { Text("التصنيف") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(deadline, { deadline = it }, label = { Text("الموعد") }, placeholder = { Text("اختياري") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Text("التقدم ${(progress * 100).roundToInt()}%", fontWeight = FontWeight.SemiBold)
                Slider(value = progress, onValueChange = { progress = it })
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, emoji, detail, progress, category, deadline) }, enabled = title.isNotBlank()) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun ProIdeaEditorDialog(
    item: ProIdea?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var body by remember(item?.id) { mutableStateOf(item?.body ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "💡") }
    var category by remember(item?.id) { mutableStateOf(item?.category ?: "إلهام") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (item == null) "فكرة جديدة" else "تعديل الفكرة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(84.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(category, { category = it }, label = { Text("التصنيف") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(body, { body = it }, label = { Text("اكتب الفكرة") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            }
        },
        confirmButton = { Button(onClick = { onSave(title, body, emoji, category) }, enabled = title.isNotBlank() || body.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun ProNoteEditorDialog(
    item: ProNote?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var title by remember(item?.id) { mutableStateOf(item?.title ?: "") }
    var body by remember(item?.id) { mutableStateOf(item?.body ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "📝") }
    var colorKey by remember(item?.id) { mutableStateOf(item?.colorKey ?: 0) }
    val swatches = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        Color(0xFFFFE8B5), Color(0xFFDFF6E9), Color(0xFFE5F1FF), Color(0xFFFFE4EC)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (item == null) "ملاحظة جديدة" else "تعديل الملاحظة", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(84.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text("العنوان") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                OutlinedTextField(body, { body = it }, label = { Text("الملاحظة") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
                Text("لون البطاقة", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    swatches.forEachIndexed { index, color ->
                        Box(
                            Modifier
                                .size(if (colorKey == index) 34.dp else 30.dp)
                                .background(color, CircleShape)
                                .clickable { colorKey = index },
                            contentAlignment = Alignment.Center
                        ) { if (colorKey == index) Text("✓", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(title, body, emoji, colorKey) }, enabled = title.isNotBlank() || body.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun ProHabitEditorDialog(item: ProHabit?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember(item?.id) { mutableStateOf(item?.name ?: "") }
    var emoji by remember(item?.id) { mutableStateOf(item?.emoji ?: "✅") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (item == null) "عادة جديدة" else "تعديل العادة", fontWeight = FontWeight.Bold) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(84.dp), singleLine = true)
                OutlinedTextField(name, { name = it }, label = { Text("اسم العادة") }, modifier = Modifier.weight(1f), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onSave(name, emoji) }, enabled = name.isNotBlank()) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun ProQuickCreateDialog(
    type: ProQuickType,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember(type) { mutableStateOf("") }
    var body by remember(type) { mutableStateOf("") }
    var emoji by remember(type) { mutableStateOf(type.emoji) }
    val bodyEnabled = type != ProQuickType.Habit

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(type.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(emoji, { emoji = it.take(3) }, label = { Text("رمز") }, modifier = Modifier.width(84.dp), singleLine = true)
                    OutlinedTextField(title, { title = it }, label = { Text(if (type == ProQuickType.Habit) "اسم العادة" else "العنوان") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                if (bodyEnabled) OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(if (type == ProQuickType.Goal) "الخطوة التالية" else "التفاصيل") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = { Button(onClick = { onSave(title, body, emoji) }, enabled = title.isNotBlank() || (bodyEnabled && body.isNotBlank())) { Text("إضافة") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
internal fun ProConfirmDialog(
    title: String,
    body: String,
    confirm: String,
    destructive: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(body, lineHeight = 21.sp) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (destructive) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) { Text(confirm) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

internal fun shareTextPro(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة عبر"))
}

internal fun formatProTime(hour: Int, minute: Int): String {
    val isPm = hour >= 12
    val h = when (val normalized = hour % 12) { 0 -> 12; else -> normalized }
    return "%d:%02d %s".format(Locale.US, h, minute, if (isPm) "م" else "ص")
}

internal fun formatArabicDate(date: LocalDate): String = runCatching {
    date.format(DateTimeFormatter.ofPattern("EEEE، d MMM", Locale("ar")))
}.getOrElse { date.toString() }
