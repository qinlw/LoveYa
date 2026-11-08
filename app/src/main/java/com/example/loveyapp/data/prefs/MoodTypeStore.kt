package com.example.loveyapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.loveyapp.data.local.entity.CalendarMark
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/** 心情类型：内置三种固定心情 + 用户自定义心情 */
data class MoodType(
    val id: String,
    val label: String,
    val builtin: Boolean
)

/**
 * 心情类型存储：内置三种心情固定，用户自定义心情持久化在 SharedPreferences 中。
 * 自定义心情的表情图片交给 MoodIconStorage 保存，文件名以心情 id 区分。
 */
@Singleton
class MoodTypeStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mood_types", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM = "custom_moods"
        private const val KEY_BUILTIN_LABELS = "builtin_labels"
        private const val KEY_HIDDEN_MOODS = "hidden_moods"

        /** 内置心情（可改名、可删除，删除后不再出现在选择列表） */
        val BUILTIN: List<MoodType> = listOf(
            MoodType(CalendarMark.MOOD_HAPPY, "开心", true),
            MoodType(CalendarMark.MOOD_NORMAL, "正常", true),
            MoodType(CalendarMark.MOOD_SAD, "不开心", true)
        )
    }

    /** 用户自定义心情（按添加顺序） */
    fun getCustomMoods(): List<MoodType> {
        val raw = prefs.getString(KEY_CUSTOM, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val id = obj.optString("id")
                    val label = obj.optString("label")
                    if (id.isNotBlank() && label.isNotBlank()) {
                        add(MoodType(id, label, builtin = false))
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    /** 全部心情：内置在前（使用改名后的名称、排除已删除），自定义在后 */
    fun getMoodTypes(): List<MoodType> {
        val labels = getBuiltinLabels()
        val hidden = prefs.getStringSet(KEY_HIDDEN_MOODS, emptySet()) ?: emptySet()
        val builtins = BUILTIN
            .filterNot { it.id in hidden }
            .map { it.copy(label = labels[it.id] ?: it.label) }
        return builtins + getCustomMoods().filterNot { it.id in hidden }
    }

    /** 已删除（隐藏）的心情，供「心情一览」中查找与恢复 */
    fun getHiddenMoods(): List<MoodType> {
        val hidden = prefs.getStringSet(KEY_HIDDEN_MOODS, emptySet()) ?: emptySet()
        val labels = getBuiltinLabels()
        val builtins = BUILTIN
            .filter { it.id in hidden }
            .map { it.copy(label = labels[it.id] ?: it.label) }
        val customs = getCustomMoods().filter { it.id in hidden }
        return builtins + customs
    }

    /** 恢复已删除的心情，使其重新出现在心情选择列表 */
    fun restoreMood(id: String) {
        val hidden = prefs.getStringSet(KEY_HIDDEN_MOODS, emptySet()) ?: emptySet()
        prefs.edit().putStringSet(KEY_HIDDEN_MOODS, hidden - id).apply()
    }

    /** 已删除心情的原名称：用于历史记录展示，避免删除后看不懂记录含义 */
    fun archivedLabelOf(id: String): String? {
        BUILTIN.find { it.id == id }?.let { builtin ->
            return getBuiltinLabels()[id] ?: builtin.label
        }
        return getCustomMoods().find { it.id == id }?.label
    }

    /** 新增自定义心情，可选同时保存一张自定义表情图 */
    fun addCustomMood(label: String, iconUri: Uri?): MoodType {
        val mood = MoodType("CUSTOM_" + System.currentTimeMillis(), label.trim(), builtin = false)
        saveCustomMoods(getCustomMoods() + mood)
        if (iconUri != null) {
            MoodIconStorage.saveCustomIcon(context, mood.id, iconUri)
        }
        return mood
    }

    /** 修改心情名称（内置心情记录改名覆盖，自定义心情更新列表） */
    fun renameMood(id: String, newLabel: String): Boolean {
        val label = newLabel.trim()
        if (label.isBlank()) return false
        if (BUILTIN.any { it.id == id }) {
            saveBuiltinLabels(getBuiltinLabels() + (id to label))
        } else {
            saveCustomMoods(getCustomMoods().map { if (it.id == id) it.copy(label = label) else it })
        }
        return true
    }

    /**
     * 删除心情（内置与自定义都支持）。
     * 只从可选列表隐藏，不改动历史记录与名称映射；
     * 已记录的日历数据仍会保留展示，记录里也能看到原来的心情名称。
     */
    fun deleteMood(id: String) {
        val hidden = prefs.getStringSet(KEY_HIDDEN_MOODS, emptySet()) ?: emptySet()
        prefs.edit().putStringSet(KEY_HIDDEN_MOODS, hidden + id).apply()
    }

    private fun getBuiltinLabels(): Map<String, String> {
        val raw = prefs.getString(KEY_BUILTIN_LABELS, null) ?: return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            buildMap {
                obj.keys().forEach { key ->
                    val value = obj.optString(key)
                    if (value.isNotBlank()) put(key, value)
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun saveBuiltinLabels(labels: Map<String, String>) {
        val obj = JSONObject()
        labels.forEach { (id, label) -> obj.put(id, label) }
        prefs.edit().putString(KEY_BUILTIN_LABELS, obj.toString()).apply()
    }

    private fun saveCustomMoods(moods: List<MoodType>) {
        val array = JSONArray()
        moods.forEach { mood ->
            array.put(JSONObject().apply {
                put("id", mood.id)
                put("label", mood.label)
            })
        }
        prefs.edit().putString(KEY_CUSTOM, array.toString()).apply()
    }
}
