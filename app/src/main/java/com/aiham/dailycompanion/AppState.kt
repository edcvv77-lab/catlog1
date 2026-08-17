package com.aiham.dailycompanion

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class GoalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val emoji: String = "🎯",
    val progress: Float = 0f,
    val detail: String = ""
)

data class IdeaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "💡",
    val favorite: Boolean = false
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "📝",
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class HabitItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "✓",
    val doneMask: Int = 0
)

data class JournalEntry(
    val date: String,
    val mood: String,
    val gratitude: String,
    val focus: String
)

class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("daily_companion", Context.MODE_PRIVATE)

    private var _profileName by mutableStateOf(prefs.getString("profile_name", "محمد حافظ") ?: "محمد حافظ")
    val profileName: String get() = _profileName

    private var _profilePhotoUri by mutableStateOf(prefs.getString("profile_photo_uri", null))
    val profilePhotoUri: String? get() = _profilePhotoUri

    private var _darkMode by mutableStateOf(prefs.getBoolean("dark_mode", false))
    val darkMode: Boolean get() = _darkMode

    private var _reminderEnabled by mutableStateOf(prefs.getBoolean("reminder_enabled", false))
    val reminderEnabled: Boolean get() = _reminderEnabled

    private var _reminderHour by mutableStateOf(prefs.getInt("reminder_hour", 20))
    val reminderHour: Int get() = _reminderHour

    private var _reminderMinute by mutableStateOf(prefs.getInt("reminder_minute", 0))
    val reminderMinute: Int get() = _reminderMinute

    private var _journal by mutableStateOf(loadJournal())
    val journal: JournalEntry get() = _journal

    val goals = mutableStateListOf<GoalItem>()
    val ideas = mutableStateListOf<IdeaItem>()
    val notes = mutableStateListOf<NoteItem>()
    val habits = mutableStateListOf<HabitItem>()
    val photoUris = mutableStateListOf<String>()

    init {
        loadGoals()
        loadIdeas()
        loadNotes()
        loadHabits()
        loadPhotos()
    }

    fun setProfileName(value: String) {
        val cleaned = value.trim().ifBlank { "محمد حافظ" }
        _profileName = cleaned
        prefs.edit().putString("profile_name", cleaned).apply()
    }

    fun setProfilePhotoUri(uri: String?) {
        _profilePhotoUri = uri
        prefs.edit().apply {
            if (uri.isNullOrBlank()) remove("profile_photo_uri") else putString("profile_photo_uri", uri)
        }.apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _darkMode = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun setReminder(enabled: Boolean, hour: Int = _reminderHour, minute: Int = _reminderMinute) {
        _reminderEnabled = enabled
        _reminderHour = hour.coerceIn(0, 23)
        _reminderMinute = minute.coerceIn(0, 59)
        prefs.edit()
            .putBoolean("reminder_enabled", _reminderEnabled)
            .putInt("reminder_hour", _reminderHour)
            .putInt("reminder_minute", _reminderMinute)
            .apply()
    }

    fun addGoal(title: String, emoji: String = "🎯", detail: String = "", progress: Float = 0f) {
        val clean = title.trim()
        if (clean.isBlank()) return
        goals.add(0, GoalItem(title = clean, emoji = emoji.ifBlank { "🎯" }, progress = progress.coerceIn(0f, 1f), detail = detail.trim()))
        saveGoals()
    }

    fun updateGoal(id: String, title: String, emoji: String, detail: String, progress: Float) {
        val index = goals.indexOfFirst { it.id == id }
        if (index < 0 || title.trim().isBlank()) return
        goals[index] = goals[index].copy(
            title = title.trim(),
            emoji = emoji.ifBlank { "🎯" },
            detail = detail.trim(),
            progress = progress.coerceIn(0f, 1f)
        )
        saveGoals()
    }

    fun setGoalProgress(id: String, progress: Float) {
        val index = goals.indexOfFirst { it.id == id }
        if (index < 0) return
        goals[index] = goals[index].copy(progress = progress.coerceIn(0f, 1f))
        saveGoals()
    }

    fun deleteGoal(id: String) {
        goals.removeAll { it.id == id }
        saveGoals()
    }

    fun addIdea(title: String, body: String, emoji: String = "💡") {
        if (title.trim().isBlank() && body.trim().isBlank()) return
        ideas.add(0, IdeaItem(title = title.trim().ifBlank { "فكرة جديدة" }, body = body.trim(), emoji = emoji.ifBlank { "💡" }))
        saveIdeas()
    }

    fun updateIdea(id: String, title: String, body: String, emoji: String) {
        val index = ideas.indexOfFirst { it.id == id }
        if (index < 0) return
        val old = ideas[index]
        ideas[index] = old.copy(
            title = title.trim().ifBlank { "فكرة جديدة" },
            body = body.trim(),
            emoji = emoji.ifBlank { "💡" }
        )
        saveIdeas()
    }

    fun toggleIdeaFavorite(id: String) {
        val index = ideas.indexOfFirst { it.id == id }
        if (index < 0) return
        ideas[index] = ideas[index].copy(favorite = !ideas[index].favorite)
        saveIdeas()
    }

    fun deleteIdea(id: String) {
        ideas.removeAll { it.id == id }
        saveIdeas()
    }

    fun addNote(title: String, body: String, emoji: String = "📝") {
        if (title.trim().isBlank() && body.trim().isBlank()) return
        notes.add(0, NoteItem(title = title.trim().ifBlank { "ملاحظة" }, body = body.trim(), emoji = emoji.ifBlank { "📝" }))
        saveNotes()
    }

    fun updateNote(id: String, title: String, body: String, emoji: String) {
        val index = notes.indexOfFirst { it.id == id }
        if (index < 0) return
        notes[index] = notes[index].copy(
            title = title.trim().ifBlank { "ملاحظة" },
            body = body.trim(),
            emoji = emoji.ifBlank { "📝" },
            updatedAt = System.currentTimeMillis()
        )
        saveNotes()
    }

    fun toggleNotePinned(id: String) {
        val index = notes.indexOfFirst { it.id == id }
        if (index < 0) return
        notes[index] = notes[index].copy(pinned = !notes[index].pinned, updatedAt = System.currentTimeMillis())
        saveNotes()
    }

    fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        saveNotes()
    }

    fun addHabit(name: String, emoji: String = "✓") {
        val clean = name.trim()
        if (clean.isBlank()) return
        habits.add(HabitItem(name = clean, emoji = emoji.ifBlank { "✓" }))
        saveHabits()
    }

    fun updateHabit(id: String, name: String, emoji: String) {
        val index = habits.indexOfFirst { it.id == id }
        if (index < 0 || name.trim().isBlank()) return
        habits[index] = habits[index].copy(name = name.trim(), emoji = emoji.ifBlank { "✓" })
        saveHabits()
    }

    fun toggleHabit(habitId: String, day: Int) {
        if (day !in 0..6) return
        val index = habits.indexOfFirst { it.id == habitId }
        if (index < 0) return
        val bit = 1 shl day
        habits[index] = habits[index].copy(doneMask = habits[index].doneMask xor bit)
        saveHabits()
    }

    fun deleteHabit(id: String) {
        habits.removeAll { it.id == id }
        saveHabits()
    }

    fun resetHabitsWeek() {
        for (i in habits.indices) habits[i] = habits[i].copy(doneMask = 0)
        saveHabits()
    }

    fun addPhotoUri(uri: String) {
        if (uri.isBlank()) return
        photoUris.remove(uri)
        photoUris.add(0, uri)
        savePhotos()
    }

    fun removePhotoUri(uri: String) {
        photoUris.remove(uri)
        if (_profilePhotoUri == uri) setProfilePhotoUri(null)
        savePhotos()
    }

    fun saveJournal(mood: String, gratitude: String, focus: String) {
        _journal = JournalEntry(LocalDate.now().toString(), mood, gratitude.trim(), focus.trim())
        prefs.edit().putString("journal", JSONObject().apply {
            put("date", _journal.date)
            put("mood", _journal.mood)
            put("gratitude", _journal.gratitude)
            put("focus", _journal.focus)
        }.toString()).apply()
    }

    fun exportText(): String {
        val completedGoals = goals.count { it.progress >= 1f }
        val habitDone = habits.sumOf { Integer.bitCount(it.doneMask and 0x7F) }
        return buildString {
            appendLine("مساعدي اليومي — ملخص $profileName")
            appendLine("الأهداف: ${goals.size} (مكتمل: $completedGoals)")
            appendLine("الأفكار: ${ideas.size}")
            appendLine("الملاحظات: ${notes.size}")
            appendLine("إنجازات العادات هذا الأسبوع: $habitDone")
            appendLine()
            appendLine("أهدافي:")
            goals.forEach { appendLine("• ${it.title} — ${(it.progress * 100).toInt()}%") }
        }
    }

    fun resetAll() {
        prefs.edit().clear().apply()
        _profileName = "محمد حافظ"
        _profilePhotoUri = null
        _darkMode = false
        _reminderEnabled = false
        _reminderHour = 20
        _reminderMinute = 0
        _journal = JournalEntry(LocalDate.now().toString(), "🙂", "", "")
        goals.clear(); goals.addAll(defaultGoals())
        ideas.clear(); ideas.addAll(defaultIdeas())
        notes.clear(); notes.addAll(defaultNotes())
        habits.clear(); habits.addAll(defaultHabits())
        photoUris.clear()
        saveGoals(); saveIdeas(); saveNotes(); saveHabits(); savePhotos()
    }

    val weeklyHabitCompletion: Float
        get() {
            if (habits.isEmpty()) return 0f
            val total = habits.size * 7
            val done = habits.sumOf { Integer.bitCount(it.doneMask and 0x7F) }
            return done.toFloat() / total.toFloat()
        }

    private fun loadGoals() = loadArray("goals", goals, defaultGoals()) { o ->
        GoalItem(
            id = o.optString("id", UUID.randomUUID().toString()),
            title = o.optString("title", "هدف"),
            emoji = o.optString("emoji", "🎯"),
            progress = o.optDouble("progress", 0.0).toFloat().coerceIn(0f, 1f),
            detail = o.optString("detail", "")
        )
    }

    private fun loadIdeas() = loadArray("ideas", ideas, defaultIdeas()) { o ->
        IdeaItem(
            id = o.optString("id", UUID.randomUUID().toString()),
            title = o.optString("title", "فكرة"),
            body = o.optString("body", ""),
            emoji = o.optString("emoji", "💡"),
            favorite = o.optBoolean("favorite", false)
        )
    }

    private fun loadNotes() = loadArray("notes", notes, defaultNotes()) { o ->
        NoteItem(
            id = o.optString("id", UUID.randomUUID().toString()),
            title = o.optString("title", "ملاحظة"),
            body = o.optString("body", ""),
            emoji = o.optString("emoji", "📝"),
            pinned = o.optBoolean("pinned", false),
            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun loadHabits() = loadArray("habits", habits, defaultHabits()) { o ->
        HabitItem(
            id = o.optString("id", UUID.randomUUID().toString()),
            name = o.optString("name", "عادة"),
            emoji = o.optString("emoji", "✓"),
            doneMask = o.optInt("mask", 0)
        )
    }

    private fun <T> loadArray(key: String, target: MutableList<T>, defaults: List<T>, mapper: (JSONObject) -> T) {
        val raw = prefs.getString(key, null)
        if (raw.isNullOrBlank()) {
            target.addAll(defaults)
            return
        }
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { target.add(mapper(array.getJSONObject(it))) }
        }.onFailure {
            target.clear(); target.addAll(defaults)
        }
    }

    private fun loadPhotos() {
        val raw = prefs.getString("photo_uris", null) ?: return
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { photoUris.add(array.getString(it)) }
        }
    }

    private fun loadJournal(): JournalEntry {
        val raw = prefs.getString("journal", null)
        if (raw.isNullOrBlank()) return JournalEntry(LocalDate.now().toString(), "🙂", "", "")
        return runCatching {
            val o = JSONObject(raw)
            JournalEntry(
                date = o.optString("date", LocalDate.now().toString()),
                mood = o.optString("mood", "🙂"),
                gratitude = o.optString("gratitude", ""),
                focus = o.optString("focus", "")
            )
        }.getOrElse { JournalEntry(LocalDate.now().toString(), "🙂", "", "") }
    }

    private fun saveGoals() = saveArray("goals", goals.map { item -> JSONObject().apply {
        put("id", item.id); put("title", item.title); put("emoji", item.emoji); put("progress", item.progress.toDouble()); put("detail", item.detail)
    } })

    private fun saveIdeas() = saveArray("ideas", ideas.map { item -> JSONObject().apply {
        put("id", item.id); put("title", item.title); put("body", item.body); put("emoji", item.emoji); put("favorite", item.favorite)
    } })

    private fun saveNotes() = saveArray("notes", notes.map { item -> JSONObject().apply {
        put("id", item.id); put("title", item.title); put("body", item.body); put("emoji", item.emoji); put("pinned", item.pinned); put("updatedAt", item.updatedAt)
    } })

    private fun saveHabits() = saveArray("habits", habits.map { item -> JSONObject().apply {
        put("id", item.id); put("name", item.name); put("emoji", item.emoji); put("mask", item.doneMask)
    } })

    private fun savePhotos() {
        val array = JSONArray(); photoUris.forEach { array.put(it) }
        prefs.edit().putString("photo_uris", array.toString()).apply()
    }

    private fun saveArray(key: String, objects: List<JSONObject>) {
        val array = JSONArray(); objects.forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).apply()
    }

    companion object {
        private fun defaultGoals() = listOf(
            GoalItem(title = "التمرين 5 أيام في الأسبوع", emoji = "🏋️", progress = .80f, detail = "4 من 5 أيام"),
            GoalItem(title = "قراءة 20 صفحة يومياً", emoji = "📖", progress = .60f, detail = "12 من 20 صفحة"),
            GoalItem(title = "شرب 2 لتر من الماء", emoji = "💧", progress = .70f, detail = "1.4 من 2 لتر"),
            GoalItem(title = "تعلم مهارة جديدة", emoji = "🎓", progress = .40f, detail = "جاري التقدم")
        )

        private fun defaultIdeas() = listOf(
            IdeaItem(title = "ابدأ الآن", body = "ولو بخطوة صغيرة. التغيير يبدأ من قرار صغير.", emoji = "🚀", favorite = true),
            IdeaItem(title = "الاستمرار يصنع الفرق", body = "لا تتوقف؛ كل يوم يقرّبك من هدفك.", emoji = "🌱"),
            IdeaItem(title = "كل يوم فرصة جديدة", body = "أنت أقوى مما تتخيل، فقط استمر في المحاولة.", emoji = "🌤️"),
            IdeaItem(title = "اصنع مستقبلك بفكرة اليوم", body = "أفكارك الصغيرة اليوم قد تصبح إنجازات الغد.", emoji = "🧩")
        )

        private fun defaultNotes() = listOf(
            NoteItem(title = "ملاحظة سريعة", body = "لا تنسَ الاجتماع غداً الساعة 10 صباحاً.", emoji = "📋", pinned = true),
            NoteItem(title = "فكرة اليوم", body = "التركيز على ما أستطيع التحكم به.", emoji = "💡"),
            NoteItem(title = "تذكير شخصي", body = "اشرب ماء ولا تنسَ الصلاة.", emoji = "❤️")
        )

        private fun defaultHabits() = listOf(
            HabitItem(id = "water", name = "شرب الماء", emoji = "💧", doneMask = 0b0111111),
            HabitItem(id = "sport", name = "الرياضة", emoji = "🏃", doneMask = 0b0011111),
            HabitItem(id = "read", name = "قراءة", emoji = "📖", doneMask = 0b0011011),
            HabitItem(id = "reflect", name = "تأمل", emoji = "🧘", doneMask = 0b0001111)
        )
    }
}
