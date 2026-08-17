package com.aiham.dailycompanion

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/**
 * Data layer for the professional 3.0 experience.
 *
 * It deliberately keeps using the original SharedPreferences file so an update from 2.x can
 * migrate the user's existing goals, ideas, notes, habits, photos and journal instead of starting
 * from an empty application.
 */
data class ProGoal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val emoji: String = "🎯",
    val detail: String = "",
    val progress: Float = 0f,
    val category: String = "شخصي",
    val deadline: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ProIdea(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "💡",
    val category: String = "إلهام",
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ProNote(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "📝",
    val pinned: Boolean = false,
    val colorKey: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProHabit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "✅",
    val doneDates: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ProPhoto(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val caption: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ProJournalEntry(
    val date: String,
    val mood: String = "🙂",
    val gratitude: String = "",
    val focus: String = "",
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

class ProfessionalState(context: Context) {
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

    val goals = mutableStateListOf<ProGoal>()
    val ideas = mutableStateListOf<ProIdea>()
    val notes = mutableStateListOf<ProNote>()
    val habits = mutableStateListOf<ProHabit>()
    val photos = mutableStateListOf<ProPhoto>()
    val journals = mutableStateListOf<ProJournalEntry>()

    init {
        loadGoals()
        loadIdeas()
        loadNotes()
        loadHabits()
        loadPhotos()
        loadJournals()
    }

    fun setProfileName(value: String) {
        _profileName = value.trim().ifBlank { "محمد حافظ" }
        prefs.edit().putString("profile_name", _profileName).apply()
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

    fun addGoal(
        title: String,
        emoji: String = "🎯",
        detail: String = "",
        progress: Float = 0f,
        category: String = "شخصي",
        deadline: String = ""
    ) {
        if (title.isBlank()) return
        goals.add(
            0,
            ProGoal(
                title = title.trim(),
                emoji = emoji.ifBlank { "🎯" },
                detail = detail.trim(),
                progress = progress.coerceIn(0f, 1f),
                category = category.trim().ifBlank { "شخصي" },
                deadline = deadline.trim()
            )
        )
        saveGoals()
    }

    fun updateGoal(
        id: String,
        title: String,
        emoji: String,
        detail: String,
        progress: Float,
        category: String,
        deadline: String
    ) {
        val i = goals.indexOfFirst { it.id == id }
        if (i < 0 || title.isBlank()) return
        goals[i] = goals[i].copy(
            title = title.trim(),
            emoji = emoji.ifBlank { "🎯" },
            detail = detail.trim(),
            progress = progress.coerceIn(0f, 1f),
            category = category.trim().ifBlank { "شخصي" },
            deadline = deadline.trim()
        )
        saveGoals()
    }

    fun setGoalProgress(id: String, progress: Float) {
        val i = goals.indexOfFirst { it.id == id }
        if (i < 0) return
        goals[i] = goals[i].copy(progress = progress.coerceIn(0f, 1f))
        saveGoals()
    }

    fun toggleGoalComplete(id: String) {
        val i = goals.indexOfFirst { it.id == id }
        if (i < 0) return
        goals[i] = goals[i].copy(progress = if (goals[i].progress >= 1f) .9f else 1f)
        saveGoals()
    }

    fun deleteGoal(id: String) {
        goals.removeAll { it.id == id }
        saveGoals()
    }

    fun addIdea(title: String, body: String, emoji: String = "💡", category: String = "إلهام") {
        if (title.isBlank() && body.isBlank()) return
        ideas.add(
            0,
            ProIdea(
                title = title.trim().ifBlank { "فكرة جديدة" },
                body = body.trim(),
                emoji = emoji.ifBlank { "💡" },
                category = category.trim().ifBlank { "إلهام" }
            )
        )
        saveIdeas()
    }

    fun updateIdea(id: String, title: String, body: String, emoji: String, category: String) {
        val i = ideas.indexOfFirst { it.id == id }
        if (i < 0) return
        ideas[i] = ideas[i].copy(
            title = title.trim().ifBlank { "فكرة جديدة" },
            body = body.trim(),
            emoji = emoji.ifBlank { "💡" },
            category = category.trim().ifBlank { "إلهام" }
        )
        saveIdeas()
    }

    fun toggleIdeaFavorite(id: String) {
        val i = ideas.indexOfFirst { it.id == id }
        if (i < 0) return
        ideas[i] = ideas[i].copy(favorite = !ideas[i].favorite)
        saveIdeas()
    }

    fun deleteIdea(id: String) {
        ideas.removeAll { it.id == id }
        saveIdeas()
    }

    fun addNote(title: String, body: String, emoji: String = "📝", colorKey: Int = 0) {
        if (title.isBlank() && body.isBlank()) return
        notes.add(
            0,
            ProNote(
                title = title.trim().ifBlank { "ملاحظة" },
                body = body.trim(),
                emoji = emoji.ifBlank { "📝" },
                colorKey = colorKey.coerceIn(0, 4)
            )
        )
        saveNotes()
    }

    fun updateNote(id: String, title: String, body: String, emoji: String, colorKey: Int) {
        val i = notes.indexOfFirst { it.id == id }
        if (i < 0) return
        notes[i] = notes[i].copy(
            title = title.trim().ifBlank { "ملاحظة" },
            body = body.trim(),
            emoji = emoji.ifBlank { "📝" },
            colorKey = colorKey.coerceIn(0, 4),
            updatedAt = System.currentTimeMillis()
        )
        saveNotes()
    }

    fun toggleNotePinned(id: String) {
        val i = notes.indexOfFirst { it.id == id }
        if (i < 0) return
        notes[i] = notes[i].copy(pinned = !notes[i].pinned, updatedAt = System.currentTimeMillis())
        saveNotes()
    }

    fun deleteNote(id: String) {
        notes.removeAll { it.id == id }
        saveNotes()
    }

    fun addHabit(name: String, emoji: String = "✅") {
        if (name.isBlank()) return
        habits.add(0, ProHabit(name = name.trim(), emoji = emoji.ifBlank { "✅" }))
        saveHabits()
    }

    fun updateHabit(id: String, name: String, emoji: String) {
        val i = habits.indexOfFirst { it.id == id }
        if (i < 0 || name.isBlank()) return
        habits[i] = habits[i].copy(name = name.trim(), emoji = emoji.ifBlank { "✅" })
        saveHabits()
    }

    fun toggleHabitDate(id: String, date: LocalDate = LocalDate.now()) {
        val i = habits.indexOfFirst { it.id == id }
        if (i < 0) return
        val key = date.toString()
        val old = habits[i].doneDates
        val updated = if (key in old) old - key else old + key
        habits[i] = habits[i].copy(doneDates = updated)
        saveHabits()
    }

    fun isHabitDone(id: String, date: LocalDate = LocalDate.now()): Boolean =
        habits.firstOrNull { it.id == id }?.doneDates?.contains(date.toString()) == true

    fun deleteHabit(id: String) {
        habits.removeAll { it.id == id }
        saveHabits()
    }

    fun resetCurrentWeek() {
        val week = last7Dates.map { it.toString() }.toSet()
        habits.indices.forEach { index ->
            habits[index] = habits[index].copy(doneDates = habits[index].doneDates - week)
        }
        saveHabits()
    }

    fun habitStreak(habit: ProHabit): Int {
        var day = LocalDate.now()
        if (day.toString() !in habit.doneDates) day = day.minusDays(1)
        var streak = 0
        while (day.toString() in habit.doneDates) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun habitBestStreak(habit: ProHabit): Int {
        val dates = habit.doneDates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.sorted()
        if (dates.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until dates.size) {
            if (dates[i - 1].plusDays(1) == dates[i]) {
                current++
                if (current > best) best = current
            } else if (dates[i - 1] != dates[i]) {
                current = 1
            }
        }
        return best
    }

    fun addPhotoUri(uri: String) {
        if (uri.isBlank()) return
        val existing = photos.indexOfFirst { it.uri == uri }
        if (existing >= 0) {
            val photo = photos.removeAt(existing)
            photos.add(0, photo)
        } else {
            photos.add(0, ProPhoto(uri = uri))
        }
        if (_profilePhotoUri == null) setProfilePhotoUri(uri)
        savePhotos()
    }

    fun updatePhotoCaption(id: String, caption: String) {
        val i = photos.indexOfFirst { it.id == id }
        if (i < 0) return
        photos[i] = photos[i].copy(caption = caption.trim())
        savePhotos()
    }

    fun togglePhotoFavorite(id: String) {
        val i = photos.indexOfFirst { it.id == id }
        if (i < 0) return
        photos[i] = photos[i].copy(favorite = !photos[i].favorite)
        savePhotos()
    }

    fun removePhoto(id: String) {
        val photo = photos.firstOrNull { it.id == id } ?: return
        photos.removeAll { it.id == id }
        if (_profilePhotoUri == photo.uri) setProfilePhotoUri(photos.firstOrNull()?.uri)
        savePhotos()
    }

    fun setProfilePhoto(photo: ProPhoto) = setProfilePhotoUri(photo.uri)

    fun saveJournal(
        date: LocalDate = LocalDate.now(),
        mood: String,
        gratitude: String,
        focus: String,
        note: String
    ) {
        val key = date.toString()
        val entry = ProJournalEntry(
            date = key,
            mood = mood,
            gratitude = gratitude.trim(),
            focus = focus.trim(),
            note = note.trim()
        )
        val i = journals.indexOfFirst { it.date == key }
        if (i >= 0) journals[i] = entry else journals.add(0, entry)
        journals.sortByDescending { it.date }
        saveJournals()
    }

    fun journalFor(date: LocalDate): ProJournalEntry? = journals.firstOrNull { it.date == date.toString() }

    val last7Dates: List<LocalDate>
        get() = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }

    val todayHabitCompletion: Float
        get() {
            if (habits.isEmpty()) return 0f
            val today = LocalDate.now().toString()
            return habits.count { today in it.doneDates }.toFloat() / habits.size.toFloat()
        }

    val weeklyHabitCompletion: Float
        get() {
            if (habits.isEmpty()) return 0f
            val week = last7Dates.map { it.toString() }.toSet()
            val done = habits.sumOf { habit -> habit.doneDates.count { it in week } }
            return done.toFloat() / (habits.size * 7).toFloat()
        }

    val activeGoalAverage: Float
        get() {
            val active = goals.filter { it.progress < 1f }
            if (active.isEmpty()) return if (goals.isEmpty()) 0f else 1f
            return active.map { it.progress }.average().toFloat()
        }

    val todayProgress: Float
        get() {
            val journalScore = if (journalFor(LocalDate.now()) != null) 1f else 0f
            return (activeGoalAverage * .45f + todayHabitCompletion * .40f + journalScore * .15f).coerceIn(0f, 1f)
        }

    val topActiveGoal: ProGoal?
        get() = goals.filter { it.progress < 1f }.maxByOrNull { it.progress }

    val pinnedNote: ProNote?
        get() = notes.sortedWith(compareByDescending<ProNote> { it.pinned }.thenByDescending { it.updatedAt }).firstOrNull()

    val featuredIdea: ProIdea?
        get() = ideas.sortedWith(compareByDescending<ProIdea> { it.favorite }.thenByDescending { it.createdAt }).firstOrNull()

    fun exportText(): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
        return buildString {
            appendLine("مساعدي اليومي 3.0 — ${LocalDate.now().format(formatter)}")
            appendLine("الاسم: $profileName")
            appendLine("تقدم اليوم: ${(todayProgress * 100).toInt()}%")
            appendLine("الأهداف: ${goals.size} | مكتملة: ${goals.count { it.progress >= 1f }}")
            appendLine("العادات اليوم: ${(todayHabitCompletion * 100).toInt()}%")
            appendLine("الأفكار: ${ideas.size} | الملاحظات: ${notes.size} | الصور: ${photos.size}")
            appendLine("اليوميات المحفوظة: ${journals.size}")
            appendLine()
            appendLine("الأهداف:")
            goals.forEach { appendLine("• ${it.title} — ${(it.progress * 100).toInt()}%") }
        }
    }

    fun resetAll() {
        listOf("goals", "ideas", "notes", "habits", "photo_uris", "pro_photos", "journal", "pro_journals").forEach {
            prefs.edit().remove(it).apply()
        }
        _profileName = "محمد حافظ"
        _profilePhotoUri = null
        _darkMode = false
        _reminderEnabled = false
        _reminderHour = 20
        _reminderMinute = 0
        prefs.edit()
            .putString("profile_name", _profileName)
            .putBoolean("dark_mode", false)
            .putBoolean("reminder_enabled", false)
            .putInt("reminder_hour", 20)
            .putInt("reminder_minute", 0)
            .remove("profile_photo_uri")
            .apply()
        goals.clear(); goals.addAll(defaultGoals())
        ideas.clear(); ideas.addAll(defaultIdeas())
        notes.clear(); notes.addAll(defaultNotes())
        habits.clear(); habits.addAll(defaultHabits())
        photos.clear()
        journals.clear()
        saveAll()
    }

    private fun loadGoals() {
        val raw = prefs.getString("goals", null)
        if (raw.isNullOrBlank()) { goals.addAll(defaultGoals()); return }
        runCatching {
            val a = JSONArray(raw)
            repeat(a.length()) {
                val o = a.getJSONObject(it)
                goals.add(
                    ProGoal(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "هدف"),
                        emoji = o.optString("emoji", "🎯"),
                        detail = o.optString("detail", ""),
                        progress = o.optDouble("progress", 0.0).toFloat().coerceIn(0f, 1f),
                        category = o.optString("category", "شخصي"),
                        deadline = o.optString("deadline", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }.onFailure { goals.clear(); goals.addAll(defaultGoals()) }
    }

    private fun loadIdeas() {
        val raw = prefs.getString("ideas", null)
        if (raw.isNullOrBlank()) { ideas.addAll(defaultIdeas()); return }
        runCatching {
            val a = JSONArray(raw)
            repeat(a.length()) {
                val o = a.getJSONObject(it)
                ideas.add(
                    ProIdea(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "فكرة"),
                        body = o.optString("body", ""),
                        emoji = o.optString("emoji", "💡"),
                        category = o.optString("category", "إلهام"),
                        favorite = o.optBoolean("favorite", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }.onFailure { ideas.clear(); ideas.addAll(defaultIdeas()) }
    }

    private fun loadNotes() {
        val raw = prefs.getString("notes", null)
        if (raw.isNullOrBlank()) { notes.addAll(defaultNotes()); return }
        runCatching {
            val a = JSONArray(raw)
            repeat(a.length()) {
                val o = a.getJSONObject(it)
                notes.add(
                    ProNote(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        title = o.optString("title", "ملاحظة"),
                        body = o.optString("body", ""),
                        emoji = o.optString("emoji", "📝"),
                        pinned = o.optBoolean("pinned", false),
                        colorKey = o.optInt("colorKey", 0),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }.onFailure { notes.clear(); notes.addAll(defaultNotes()) }
    }

    private fun loadHabits() {
        val raw = prefs.getString("habits", null)
        if (raw.isNullOrBlank()) { habits.addAll(defaultHabits()); return }
        runCatching {
            val a = JSONArray(raw)
            repeat(a.length()) {
                val o = a.getJSONObject(it)
                val dates = mutableSetOf<String>()
                val datesArray = o.optJSONArray("dates")
                if (datesArray != null) {
                    repeat(datesArray.length()) { index -> dates.add(datesArray.getString(index)) }
                } else {
                    // Migration from the old 7-bit tracker. Preserve the visible completion pattern
                    // by projecting its seven bits onto the latest seven calendar dates.
                    val mask = o.optInt("mask", 0)
                    last7Dates.forEachIndexed { index, date ->
                        if ((mask and (1 shl index)) != 0) dates.add(date.toString())
                    }
                }
                habits.add(
                    ProHabit(
                        id = o.optString("id", UUID.randomUUID().toString()),
                        name = o.optString("name", "عادة"),
                        emoji = o.optString("emoji", "✅"),
                        doneDates = dates,
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }.onFailure { habits.clear(); habits.addAll(defaultHabits()) }
    }

    private fun loadPhotos() {
        val rich = prefs.getString("pro_photos", null)
        if (!rich.isNullOrBlank()) {
            runCatching {
                val a = JSONArray(rich)
                repeat(a.length()) {
                    val o = a.getJSONObject(it)
                    photos.add(
                        ProPhoto(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            uri = o.getString("uri"),
                            caption = o.optString("caption", ""),
                            favorite = o.optBoolean("favorite", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            return
        }
        val legacy = prefs.getString("photo_uris", null) ?: return
        runCatching {
            val a = JSONArray(legacy)
            repeat(a.length()) { photos.add(ProPhoto(uri = a.getString(it))) }
            savePhotos()
        }
    }

    private fun loadJournals() {
        val rich = prefs.getString("pro_journals", null)
        if (!rich.isNullOrBlank()) {
            runCatching {
                val a = JSONArray(rich)
                repeat(a.length()) {
                    val o = a.getJSONObject(it)
                    journals.add(
                        ProJournalEntry(
                            date = o.optString("date", LocalDate.now().toString()),
                            mood = o.optString("mood", "🙂"),
                            gratitude = o.optString("gratitude", ""),
                            focus = o.optString("focus", ""),
                            note = o.optString("note", ""),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                journals.sortByDescending { it.date }
            }
            return
        }
        val legacy = prefs.getString("journal", null) ?: return
        runCatching {
            val o = JSONObject(legacy)
            journals.add(
                ProJournalEntry(
                    date = o.optString("date", LocalDate.now().toString()),
                    mood = o.optString("mood", "🙂"),
                    gratitude = o.optString("gratitude", ""),
                    focus = o.optString("focus", "")
                )
            )
            saveJournals()
        }
    }

    private fun saveGoals() {
        val a = JSONArray()
        goals.forEach { g -> a.put(JSONObject().apply {
            put("id", g.id); put("title", g.title); put("emoji", g.emoji); put("detail", g.detail)
            put("progress", g.progress.toDouble()); put("category", g.category); put("deadline", g.deadline); put("createdAt", g.createdAt)
        }) }
        prefs.edit().putString("goals", a.toString()).apply()
    }

    private fun saveIdeas() {
        val a = JSONArray()
        ideas.forEach { x -> a.put(JSONObject().apply {
            put("id", x.id); put("title", x.title); put("body", x.body); put("emoji", x.emoji)
            put("category", x.category); put("favorite", x.favorite); put("createdAt", x.createdAt)
        }) }
        prefs.edit().putString("ideas", a.toString()).apply()
    }

    private fun saveNotes() {
        val a = JSONArray()
        notes.forEach { x -> a.put(JSONObject().apply {
            put("id", x.id); put("title", x.title); put("body", x.body); put("emoji", x.emoji)
            put("pinned", x.pinned); put("colorKey", x.colorKey); put("updatedAt", x.updatedAt)
        }) }
        prefs.edit().putString("notes", a.toString()).apply()
    }

    private fun saveHabits() {
        val a = JSONArray()
        habits.forEach { h -> a.put(JSONObject().apply {
            put("id", h.id); put("name", h.name); put("emoji", h.emoji); put("createdAt", h.createdAt)
            put("dates", JSONArray().apply { h.doneDates.sorted().forEach { put(it) } })
        }) }
        prefs.edit().putString("habits", a.toString()).apply()
    }

    private fun savePhotos() {
        val a = JSONArray()
        photos.forEach { p -> a.put(JSONObject().apply {
            put("id", p.id); put("uri", p.uri); put("caption", p.caption); put("favorite", p.favorite); put("createdAt", p.createdAt)
        }) }
        val legacy = JSONArray().apply { photos.forEach { put(it.uri) } }
        prefs.edit().putString("pro_photos", a.toString()).putString("photo_uris", legacy.toString()).apply()
    }

    private fun saveJournals() {
        val a = JSONArray()
        journals.sortedByDescending { it.date }.forEach { j -> a.put(JSONObject().apply {
            put("date", j.date); put("mood", j.mood); put("gratitude", j.gratitude); put("focus", j.focus)
            put("note", j.note); put("updatedAt", j.updatedAt)
        }) }
        prefs.edit().putString("pro_journals", a.toString()).apply()
    }

    private fun saveAll() {
        saveGoals(); saveIdeas(); saveNotes(); saveHabits(); savePhotos(); saveJournals()
    }

    companion object {
        private fun defaultGoals() = listOf(
            ProGoal(title = "التمرين 5 أيام في الأسبوع", emoji = "🏋️", progress = .80f, detail = "أكمل جلسة اليوم", category = "صحة"),
            ProGoal(title = "قراءة 20 صفحة يومياً", emoji = "📖", progress = .60f, detail = "12 من 20 صفحة", category = "تعلّم"),
            ProGoal(title = "شرب 2 لتر من الماء", emoji = "💧", progress = .70f, detail = "1.4 من 2 لتر", category = "صحة"),
            ProGoal(title = "تطوير مهارة جديدة", emoji = "🎓", progress = .40f, detail = "جلسة تركيز لمدة 30 دقيقة", category = "تطوير")
        )

        private fun defaultIdeas() = listOf(
            ProIdea(title = "ابدأ الآن", body = "ولو بخطوة صغيرة. التغيير يبدأ من قرار صغير.", emoji = "🚀", favorite = true),
            ProIdea(title = "الاستمرار يصنع الفرق", body = "لا تتوقف؛ كل يوم يقرّبك من هدفك.", emoji = "🌱"),
            ProIdea(title = "كل يوم فرصة جديدة", body = "اختر شيئاً واحداً مهماً وأنجزه بإتقان.", emoji = "🌤️")
        )

        private fun defaultNotes() = listOf(
            ProNote(title = "أولوية اليوم", body = "أبدأ بالمهمة الأعلى أثراً قبل فتح المشتتات.", emoji = "📌", pinned = true, colorKey = 1),
            ProNote(title = "فكرة سريعة", body = "خصص وقتاً قصيراً للمراجعة في نهاية اليوم.", emoji = "💡", colorKey = 2)
        )

        private fun defaultHabits(): List<ProHabit> {
            val recent = (1L..4L).map { LocalDate.now().minusDays(it).toString() }.toSet()
            return listOf(
                ProHabit(id = "water-pro", name = "شرب الماء", emoji = "💧", doneDates = recent + LocalDate.now()),
                ProHabit(id = "sport-pro", name = "الرياضة", emoji = "🏃", doneDates = recent.take(3).toSet()),
                ProHabit(id = "read-pro", name = "القراءة", emoji = "📖", doneDates = recent.take(2).toSet()),
                ProHabit(id = "reflect-pro", name = "مراجعة اليوم", emoji = "🧘", doneDates = recent.take(3).toSet())
            )
        }
    }
}
