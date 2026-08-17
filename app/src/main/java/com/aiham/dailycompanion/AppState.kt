package com.aiham.dailycompanion

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GoalItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val emoji: String,
    val progress: Float,
    val detail: String
)

data class IdeaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "💡"
)

data class NoteItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val emoji: String = "📝"
)

data class HabitItem(
    val id: String,
    val name: String,
    val emoji: String,
    val doneMask: Int
)

class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("daily_companion", Context.MODE_PRIVATE)

    private var _profileName by mutableStateOf(prefs.getString("profile_name", "محمد حافظ") ?: "محمد حافظ")
    val profileName: String get() = _profileName

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

    private fun loadGoals() {
        val raw = prefs.getString("goals", null)
        if (raw.isNullOrBlank()) {
            goals.addAll(defaultGoals())
            saveGoals()
            return
        }
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { i ->
                val o = array.getJSONObject(i)
                goals += GoalItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    emoji = o.optString("emoji", "🎯"),
                    progress = o.optDouble("progress", 0.0).toFloat(),
                    detail = o.optString("detail", "جاري التقدم")
                )
            }
        }.onFailure {
            goals.clear(); goals.addAll(defaultGoals()); saveGoals()
        }
    }

    private fun loadIdeas() {
        val raw = prefs.getString("ideas", null)
        if (raw.isNullOrBlank()) {
            ideas.addAll(defaultIdeas())
            saveIdeas()
            return
        }
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { i ->
                val o = array.getJSONObject(i)
                ideas += IdeaItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    body = o.getString("body"),
                    emoji = o.optString("emoji", "💡")
                )
            }
        }.onFailure {
            ideas.clear(); ideas.addAll(defaultIdeas()); saveIdeas()
        }
    }

    private fun loadNotes() {
        val raw = prefs.getString("notes", null)
        if (raw.isNullOrBlank()) {
            notes.addAll(defaultNotes())
            saveNotes()
            return
        }
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { i ->
                val o = array.getJSONObject(i)
                notes += NoteItem(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    body = o.getString("body"),
                    emoji = o.optString("emoji", "📝")
                )
            }
        }.onFailure {
            notes.clear(); notes.addAll(defaultNotes()); saveNotes()
        }
    }

    private fun loadHabits() {
        val raw = prefs.getString("habits", null)
        if (raw.isNullOrBlank()) {
            habits.addAll(defaultHabits())
            saveHabits()
            return
        }
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { i ->
                val o = array.getJSONObject(i)
                habits += HabitItem(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    emoji = o.optString("emoji", "✓"),
                    doneMask = o.optInt("mask", 0)
                )
            }
        }.onFailure {
            habits.clear(); habits.addAll(defaultHabits()); saveHabits()
        }
    }

    private fun loadPhotos() {
        val raw = prefs.getString("photo_uris", null) ?: return
        runCatching {
            val array = JSONArray(raw)
            repeat(array.length()) { i -> photoUris += array.getString(i) }
        }
    }

    fun setProfileName(value: String) {
        val cleaned = value.trim().ifBlank { "محمد حافظ" }
        _profileName = cleaned
        prefs.edit().putString("profile_name", cleaned).apply()
    }

    fun addGoal(title: String) {
        val clean = title.trim()
        if (clean.isBlank()) return
        goals.add(0, GoalItem(title = clean, emoji = "🎯", progress = 0.12f, detail = "هدف جديد"))
        saveGoals()
    }

    fun advanceGoal(id: String) {
        val i = goals.indexOfFirst { it.id == id }
        if (i < 0) return
        val old = goals[i]
        goals[i] = old.copy(progress = (old.progress + 0.1f).coerceAtMost(1f), detail = "اضغط لزيادة التقدم")
        saveGoals()
    }

    fun addIdea(title: String, body: String) {
        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        if (cleanTitle.isBlank() && cleanBody.isBlank()) return
        ideas.add(0, IdeaItem(title = cleanTitle.ifBlank { "فكرة جديدة" }, body = cleanBody.ifBlank { "دوّن الفكرة قبل أن تضيع." }))
        saveIdeas()
    }

    fun addNote(title: String, body: String) {
        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        if (cleanTitle.isBlank() && cleanBody.isBlank()) return
        notes.add(0, NoteItem(title = cleanTitle.ifBlank { "ملاحظة سريعة" }, body = cleanBody.ifBlank { "تم حفظ ملاحظتك." }))
        saveNotes()
    }

    fun toggleHabit(habitId: String, day: Int) {
        if (day !in 0..6) return
        val i = habits.indexOfFirst { it.id == habitId }
        if (i < 0) return
        val old = habits[i]
        val bit = 1 shl day
        habits[i] = old.copy(doneMask = old.doneMask xor bit)
        saveHabits()
    }

    fun addPhotoUri(uri: String) {
        if (uri.isBlank() || photoUris.contains(uri)) return
        photoUris.add(0, uri)
        savePhotos()
    }

    fun removePhotoUri(uri: String) {
        photoUris.remove(uri)
        savePhotos()
    }

    private fun saveGoals() {
        val array = JSONArray()
        goals.forEach {
            array.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("emoji", it.emoji)
                put("progress", it.progress.toDouble()); put("detail", it.detail)
            })
        }
        prefs.edit().putString("goals", array.toString()).apply()
    }

    private fun saveIdeas() {
        val array = JSONArray()
        ideas.forEach {
            array.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("body", it.body); put("emoji", it.emoji)
            })
        }
        prefs.edit().putString("ideas", array.toString()).apply()
    }

    private fun saveNotes() {
        val array = JSONArray()
        notes.forEach {
            array.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("body", it.body); put("emoji", it.emoji)
            })
        }
        prefs.edit().putString("notes", array.toString()).apply()
    }

    private fun saveHabits() {
        val array = JSONArray()
        habits.forEach {
            array.put(JSONObject().apply {
                put("id", it.id); put("name", it.name); put("emoji", it.emoji); put("mask", it.doneMask)
            })
        }
        prefs.edit().putString("habits", array.toString()).apply()
    }

    private fun savePhotos() {
        val array = JSONArray()
        photoUris.forEach(array::put)
        prefs.edit().putString("photo_uris", array.toString()).apply()
    }

    companion object {
        private fun defaultGoals() = listOf(
            GoalItem(title = "التمرين 5 أيام في الأسبوع", emoji = "🏋️", progress = .80f, detail = "4 من 5 أيام"),
            GoalItem(title = "قراءة 20 صفحة يومياً", emoji = "📖", progress = .60f, detail = "12 من 20 صفحة"),
            GoalItem(title = "شرب 2 لتر من الماء", emoji = "💧", progress = .70f, detail = "1.4 من 2 لتر"),
            GoalItem(title = "تعلم مهارة جديدة", emoji = "🎓", progress = .40f, detail = "جاري التقدم")
        )

        private fun defaultIdeas() = listOf(
            IdeaItem(title = "ابدأ الآن", body = "ولو بخطوة صغيرة. التغيير يبدأ من قرار صغير.", emoji = "🚀"),
            IdeaItem(title = "الاستمرار يصنع الفرق", body = "لا تتوقف؛ كل يوم يقرّبك من هدفك.", emoji = "🌱"),
            IdeaItem(title = "كل يوم فرصة جديدة", body = "أنت أقوى مما تتخيل، فقط استمر في المحاولة.", emoji = "🌤️"),
            IdeaItem(title = "اصنع مستقبلك بفكرة اليوم", body = "أفكارك الصغيرة اليوم قد تصبح إنجازات الغد.", emoji = "🧩")
        )

        private fun defaultNotes() = listOf(
            NoteItem(title = "ملاحظة سريعة", body = "لا تنسَ الاجتماع غداً الساعة 10 صباحاً.", emoji = "📋"),
            NoteItem(title = "فكرة اليوم", body = "التركيز على ما أستطيع التحكم به.", emoji = "💡"),
            NoteItem(title = "تذكير شخصي", body = "اشرب ماء ولا تنسَ الصلاة.", emoji = "❤️")
        )

        private fun defaultHabits() = listOf(
            HabitItem("water", "شرب الماء", "💧", 0b0111111),
            HabitItem("sport", "الرياضة", "🏃", 0b0011111),
            HabitItem("read", "قراءة", "📖", 0b0011011),
            HabitItem("reflect", "تأمل", "🧘", 0b0001111)
        )
    }
}
