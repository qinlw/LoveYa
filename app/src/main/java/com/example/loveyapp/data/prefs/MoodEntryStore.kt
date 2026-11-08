package com.example.loveyapp.data.prefs

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * 日历页「心情一览」入口的配置：
 * 圆形入口的自定义图片、位置、大小、贴边距离、是否可拖动，
 * 以及是否同时显示旧版横条入口。
 * 图片保存在应用私有目录，不依赖相册 Uri 权限。
 */
object MoodEntryStore {
    private const val PREFS_NAME = "mood_entry"
    private const val KEY_DRAG_ENABLED = "drag_enabled"
    private const val KEY_OFFSET_X = "offset_x"
    private const val KEY_OFFSET_Y = "offset_y"
    private const val KEY_BUTTON_SIZE = "button_size"
    private const val KEY_EDGE_MARGIN = "edge_margin"
    private const val KEY_SHOW_LEGACY = "show_legacy_entry"
    private const val KEY_SAVED_LAYOUTS = "saved_layouts"
    private const val KEY_SETTINGS_TRANSPARENCY = "settings_transparency"
    private const val ICON_FILE_NAME = "mood_entry_icon.png"

    const val DEFAULT_BUTTON_SIZE_DP = 56
    const val DEFAULT_EDGE_MARGIN_DP = 12
    const val MIN_BUTTON_SIZE_DP = 40
    const val MAX_BUTTON_SIZE_DP = 88
    const val MAX_EDGE_MARGIN_DP = 48

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDragEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DRAG_ENABLED, false)

    fun setDragEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DRAG_ENABLED, enabled).apply()
    }

    /** 圆形按钮直径（dp），默认 56 */
    fun getButtonSizeDp(context: Context): Int =
        prefs(context).getInt(KEY_BUTTON_SIZE, DEFAULT_BUTTON_SIZE_DP)

    fun setButtonSizeDp(context: Context, sizeDp: Int) {
        prefs(context).edit()
            .putInt(KEY_BUTTON_SIZE, sizeDp.coerceIn(MIN_BUTTON_SIZE_DP, MAX_BUTTON_SIZE_DP))
            .apply()
    }

    /** 按钮与屏幕边缘的最小距离（dp）：拖动时不会被贴到边缘，默认 12 */
    fun getEdgeMarginDp(context: Context): Int =
        prefs(context).getInt(KEY_EDGE_MARGIN, DEFAULT_EDGE_MARGIN_DP)

    fun setEdgeMarginDp(context: Context, marginDp: Int) {
        prefs(context).edit()
            .putInt(KEY_EDGE_MARGIN, marginDp.coerceIn(0, MAX_EDGE_MARGIN_DP))
            .apply()
    }

    /** 是否同时显示旧版「心情一览 · 共 N 条记录」横条入口，默认关闭 */
    fun isLegacyEntryVisible(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHOW_LEGACY, false)

    fun setLegacyEntryVisible(context: Context, visible: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_LEGACY, visible).apply()
    }

    /** 保存的 X 坐标（px）；-1 表示尚未拖动过，使用默认位置 */
    fun getOffsetX(context: Context): Float = prefs(context).getFloat(KEY_OFFSET_X, -1f)

    fun getOffsetY(context: Context): Float = prefs(context).getFloat(KEY_OFFSET_Y, -1f)

    fun saveOffset(context: Context, x: Float, y: Float) {
        prefs(context).edit()
            .putFloat(KEY_OFFSET_X, x)
            .putFloat(KEY_OFFSET_Y, y)
            .apply()
    }

    /** 恢复默认布局：位置、大小、贴边距离、旧版入口；保留自定义图片与拖动开关 */
    fun resetLayout(context: Context, keepOffset: Boolean = false) {
        val editor = prefs(context).edit()
            .putInt(KEY_BUTTON_SIZE, DEFAULT_BUTTON_SIZE_DP)
            .putInt(KEY_EDGE_MARGIN, DEFAULT_EDGE_MARGIN_DP)
            .putBoolean(KEY_SHOW_LEGACY, false)
        if (!keepOffset) {
            editor.remove(KEY_OFFSET_X).remove(KEY_OFFSET_Y)
        }
        editor.apply()
    }

    /** 设置页透明度（0~100）：100 表示完全透明（只显示文字），0 表示完全不透明；默认 8 */
    fun getSettingsTransparency(context: Context): Int =
        prefs(context).getInt(KEY_SETTINGS_TRANSPARENCY, 8).coerceIn(0, 100)

    fun setSettingsTransparency(context: Context, percent: Int) {
        prefs(context).edit()
            .putInt(KEY_SETTINGS_TRANSPARENCY, percent.coerceIn(0, 100))
            .apply()
    }

    /** 已保存的布局快照 */
    data class SavedLayout(
        val id: String,
        val name: String,
        val sizeDp: Int,
        val marginDp: Int,
        val legacyVisible: Boolean,
        val offsetX: Float,
        val offsetY: Float,
        val savedAt: Long
    )

    /** 所有已保存的布局，按保存时间倒序（最新的在前） */
    fun getSavedLayouts(context: Context): List<SavedLayout> {
        val raw = prefs(context).getString(KEY_SAVED_LAYOUTS, null) ?: return emptyList()
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()
        val result = mutableListOf<SavedLayout>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            result.add(
                SavedLayout(
                    id = obj.optString("id"),
                    name = obj.optString("name"),
                    sizeDp = obj.optInt("size", DEFAULT_BUTTON_SIZE_DP),
                    marginDp = obj.optInt("margin", DEFAULT_EDGE_MARGIN_DP),
                    legacyVisible = obj.optBoolean("legacy", false),
                    offsetX = obj.optDouble("x", -1.0).toFloat(),
                    offsetY = obj.optDouble("y", -1.0).toFloat(),
                    savedAt = obj.optLong("savedAt", 0L)
                )
            )
        }
        return result.sortedByDescending { it.savedAt }
    }

    private fun writeSavedLayouts(context: Context, layouts: List<SavedLayout>) {
        val array = JSONArray()
        layouts.forEach { layout ->
            array.put(
                JSONObject().apply {
                    put("id", layout.id)
                    put("name", layout.name)
                    put("size", layout.sizeDp)
                    put("margin", layout.marginDp)
                    put("legacy", layout.legacyVisible)
                    put("x", layout.offsetX.toDouble())
                    put("y", layout.offsetY.toDouble())
                    put("savedAt", layout.savedAt)
                }
            )
        }
        prefs(context).edit().putString(KEY_SAVED_LAYOUTS, array.toString()).apply()
    }

    /** 把当前布局保存为一个带名字的快照 */
    fun saveLayoutSnapshot(context: Context, name: String): SavedLayout {
        val layout = SavedLayout(
            id = UUID.randomUUID().toString(),
            name = name,
            sizeDp = getButtonSizeDp(context),
            marginDp = getEdgeMarginDp(context),
            legacyVisible = isLegacyEntryVisible(context),
            offsetX = getOffsetX(context),
            offsetY = getOffsetY(context),
            savedAt = System.currentTimeMillis()
        )
        writeSavedLayouts(context, getSavedLayouts(context) + layout)
        return layout
    }

    fun deleteSavedLayout(context: Context, id: String) {
        writeSavedLayouts(context, getSavedLayouts(context).filterNot { it.id == id })
    }

    /** 应用某个已保存布局（位置未记录时回到默认位置） */
    fun applyLayout(context: Context, layout: SavedLayout) {
        setButtonSizeDp(context, layout.sizeDp)
        setEdgeMarginDp(context, layout.marginDp)
        setLegacyEntryVisible(context, layout.legacyVisible)
        if (layout.offsetX >= 0f && layout.offsetY >= 0f) {
            saveOffset(context, layout.offsetX, layout.offsetY)
        } else {
            prefs(context).edit().remove(KEY_OFFSET_X).remove(KEY_OFFSET_Y).apply()
        }
    }

    fun getIconFile(context: Context): File = File(context.filesDir, ICON_FILE_NAME)

    fun hasCustomIcon(context: Context): Boolean = getIconFile(context).exists()

    fun saveIcon(context: Context, uri: Uri): Boolean {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                getIconFile(context).outputStream().use { output -> input.copyTo(output) }
            }
            true
        }.getOrDefault(false)
    }

    fun resetIcon(context: Context) {
        runCatching { getIconFile(context).delete() }
    }
}
