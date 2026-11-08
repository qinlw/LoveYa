package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.prefs.MoodType
import com.example.loveyapp.data.prefs.MoodTypeStore
import com.example.loveyapp.data.repository.CalendarMarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 心情一览页：汇总所有心情记录，支持按心情筛选，
 * 并提供已删除心情的查找与恢复。
 */
@HiltViewModel
class MoodViewModel @Inject constructor(
    private val calendarMarkRepository: CalendarMarkRepository,
    private val moodTypeStore: MoodTypeStore
) : ViewModel() {

    var moodMarks by mutableStateOf<List<CalendarMark>>(emptyList())
        private set
    var moodTypes by mutableStateOf<List<MoodType>>(moodTypeStore.getMoodTypes())
        private set
    var hiddenMoods by mutableStateOf<List<MoodType>>(moodTypeStore.getHiddenMoods())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            moodMarks = calendarMarkRepository.getAllMarks()
                .filter { it.type == CalendarMark.TYPE_MOOD }
            refreshTypes()
        }
    }

    fun refreshTypes() {
        moodTypes = moodTypeStore.getMoodTypes()
        hiddenMoods = moodTypeStore.getHiddenMoods()
    }

    /** 恢复已删除的心情 */
    fun restoreMood(id: String) {
        moodTypeStore.restoreMood(id)
        refreshTypes()
    }

    /** 心情名称：已删除的心情返回存档名称 */
    fun labelOf(moodId: String?): String? {
        if (moodId == null) return null
        return moodTypes.find { it.id == moodId }?.label ?: moodTypeStore.archivedLabelOf(moodId)
    }

    /** 某心情的记录条数 */
    fun countOf(moodId: String): Int = moodMarks.count { it.name == moodId }

    /** 判断心情是否已被删除（用于筛选与展示标记） */
    fun isDeleted(moodId: String): Boolean = hiddenMoods.any { it.id == moodId }
}
