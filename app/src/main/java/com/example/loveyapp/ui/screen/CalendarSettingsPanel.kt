package com.example.loveyapp.ui.screen

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loveyapp.R
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.prefs.MoodEntryStore
import com.example.loveyapp.data.prefs.MoodIconStorage
import com.example.loveyapp.data.prefs.MoodType
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * 全屏日历设置面板：
 * - 左侧为分类（通用 / 心情 → 表情、入口），右侧为可滚动内容
 * - 面板背景可调不透明度，设置入口布局时能看到后面日历实时变化
 */
@Composable
internal fun CalendarSettingsPanel(
    sensitivity: Int,
    onSensitivityChange: (Int) -> Unit,
    panelTransparency: Int,
    onPanelTransparencyChange: (Int) -> Unit,
    moodTypes: List<MoodType>,
    onAddCustomMood: (label: String, iconUri: Uri?) -> Unit,
    onRenameMood: (moodId: String, newLabel: String) -> Unit,
    onDeleteMoodType: (String) -> Unit,
    moodRecordCount: (String) -> Int,
    entryDragEnabled: Boolean,
    onEntryDragEnabledChange: (Boolean) -> Unit,
    entrySizeDp: Int,
    onEntrySizeChange: (Int) -> Unit,
    entryMarginDp: Int,
    onEntryMarginChange: (Int) -> Unit,
    legacyEntryVisible: Boolean,
    onLegacyEntryChange: (Boolean) -> Unit,
    onEntryIconChanged: () -> Unit,
    onRestoreLayout: (MoodEntryStore.SavedLayout) -> Unit,
    onResetLayout: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onDismiss() }
    // 0 = 通用，1 = 心情表情，2 = 心情入口
    var section by remember { mutableStateOf(0) }
    // 「心情」子分类默认收起，点击「心情」后才展开
    var moodExpanded by remember { mutableStateOf(false) }
    var moodIconVersion by remember { mutableStateOf(0) }
    var entryIconVersion by remember { mutableStateOf(0) }
    var layoutsVersion by remember { mutableStateOf(0) }
    val savedLayouts = remember(layoutsVersion) { MoodEntryStore.getSavedLayouts(context) }
    var restoredHint by remember { mutableStateOf<String?>(null) }
    var showSaveLayoutDialog by remember { mutableStateOf(false) }
    var showAddMoodDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<MoodType?>(null) }
    var deleteTarget by remember { mutableStateOf<MoodType?>(null) }
    var iconTargetMood by remember { mutableStateOf<String?>(null) }

    // 更换某个心情的图标
    val pickMoodIcon = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val mood = iconTargetMood
        if (uri != null && mood != null && MoodIconStorage.saveCustomIcon(context, mood, uri)) {
            moodIconVersion++
        }
        iconTargetMood = null
    }

    // 更换圆形入口图片
    val pickEntryImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && MoodEntryStore.saveIcon(context, uri)) {
            entryIconVersion++
            onEntryIconChanged()
        }
    }

    LaunchedEffect(restoredHint) {
        if (restoredHint != null) {
            delay(2200)
            restoredHint = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = (100 - panelTransparency) / 100f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "设置",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("关闭", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Row(modifier = Modifier.fillMaxSize()) {
                    // 左侧分类
                    Column(
                        modifier = Modifier
                            .width(104.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        SettingsNavItem(
                            label = "通用",
                            selected = section == 0,
                            onClick = { section = 0 }
                        )
                        SettingsNavItem(
                            label = if (moodExpanded) "心情 ▾" else "心情 ▸",
                            selected = section == 1 || section == 2,
                            onClick = {
                                if (moodExpanded) {
                                    // 再次点击收起子分类，并回到「通用」
                                    moodExpanded = false
                                    section = 0
                                } else {
                                    moodExpanded = true
                                    section = 1
                                }
                            }
                        )
                        if (moodExpanded) {
                            SettingsNavItem(
                                label = "表情",
                                selected = section == 1,
                                indent = true,
                                onClick = { section = 1 }
                            )
                            SettingsNavItem(
                                label = "入口",
                                selected = section == 2,
                                indent = true,
                                onClick = { section = 2 }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    )
                    // 右侧内容
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        when (section) {
                            0 -> GeneralSection(
                                sensitivity = sensitivity,
                                onSensitivityChange = onSensitivityChange,
                                transparency = panelTransparency,
                                onTransparencyChange = onPanelTransparencyChange
                            )

                            1 -> MoodSection(
                                moodTypes = moodTypes,
                                iconVersion = moodIconVersion,
                                onPickIcon = { moodId -> iconTargetMood = moodId; pickMoodIcon.launch("image/*") },
                                onRename = { renameTarget = it },
                                onDelete = { deleteTarget = it },
                                onAdd = { showAddMoodDialog = true }
                            )

                            else -> EntrySection(
                                dragEnabled = entryDragEnabled,
                                onDragEnabledChange = onEntryDragEnabledChange,
                                sizeDp = entrySizeDp,
                                onSizeChange = onEntrySizeChange,
                                marginDp = entryMarginDp,
                                onMarginChange = onEntryMarginChange,
                                legacyVisible = legacyEntryVisible,
                                onLegacyChange = { onLegacyEntryChange(it) },
                                entryIconVersion = entryIconVersion,
                                onPickEntryImage = { pickEntryImage.launch("image/*") },
                                onResetEntryImage = {
                                    MoodEntryStore.resetIcon(context)
                                    entryIconVersion++
                                    onEntryIconChanged()
                                },
                                savedLayouts = savedLayouts,
                                restoredHint = restoredHint,
                                onRestoreLayout = { layout ->
                                    onRestoreLayout(layout)
                                    restoredHint = "已恢复布局「${layout.name}」"
                                },
                                onDeleteLayout = { layout ->
                                    MoodEntryStore.deleteSavedLayout(context, layout.id)
                                    layoutsVersion++
                                },
                                onSaveLayout = { showSaveLayoutDialog = true },
                                onResetLayout = onResetLayout
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSaveLayoutDialog) {
        var layoutName by remember { mutableStateOf("布局 ${savedLayouts.size + 1}") }
        AlertDialog(
            onDismissRequest = { showSaveLayoutDialog = false },
            title = { Text("保存当前布局", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = layoutName,
                        onValueChange = { if (it.length <= 12) layoutName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("布局名称（最多 12 个字）", fontSize = 12.sp) }
                    )
                    Text(
                        text = "会记录当前按钮大小、贴边距离、位置与旧版横条开关",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = layoutName.isNotBlank(),
                    onClick = {
                        MoodEntryStore.saveLayoutSnapshot(context, layoutName.trim())
                        layoutsVersion++
                        restoredHint = "已保存布局「${layoutName.trim()}」"
                        showSaveLayoutDialog = false
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveLayoutDialog = false }) { Text("取消") }
            }
        )
    }

    if (showAddMoodDialog) {
        AddMoodTypeDialog(
            onDismiss = { showAddMoodDialog = false },
            onConfirm = { label, uri ->
                onAddCustomMood(label, uri)
                showAddMoodDialog = false
            }
        )
    }

    renameTarget?.let { mood ->
        var name by remember(mood.id) { mutableStateOf(mood.label) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("修改心情名称", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 6) name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("新的名字（最多 6 个字）", fontSize = 12.sp) }
                    )
                    Text(
                        text = "改名后日历中已有的「${mood.label}」记录也会显示为新名字",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = name.isNotBlank() && name.trim() != mood.label,
                    onClick = {
                        onRenameMood(mood.id, name.trim())
                        renameTarget = null
                    }
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("取消") }
            }
        )
    }

    deleteTarget?.let { mood ->
        val count = moodRecordCount(mood.id)
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除心情「${mood.label}」", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = if (count > 0) {
                        "日历中已有 $count 条「${mood.label}」记录，删除后这些记录仍会保留，" +
                            "但不能再新增该心情。确定删除吗？"
                    } else {
                        "删除后将不能新增「${mood.label}」心情。确定删除吗？"
                    },
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteMoodType(mood.id)
                    deleteTarget = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SettingsNavItem(
    label: String,
    selected: Boolean,
    indent: Boolean = false,
    onClick: () -> Unit
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        Color.Transparent
    }
    // 选中态只改底色、不改文字颜色，保证透明背景时文字依然清晰
    val textColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(
                start = if (indent) 20.dp else 10.dp,
                end = 8.dp,
                top = 10.dp,
                bottom = 10.dp
            )
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    hint: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        modifier = Modifier.fillMaxWidth()
    )
    if (hint != null) {
        Text(
            text = hint,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun GeneralSection(
    sensitivity: Int,
    onSensitivityChange: (Int) -> Unit,
    transparency: Int,
    onTransparencyChange: (Int) -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var input by remember(sensitivity) { mutableStateOf(sensitivity.toString()) }

    SectionTitle(
        title = "左右滑动灵敏度",
        subtitle = "1 = 需要滑动较长距离，100 = 轻轻一划即可翻页"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (editing) {
            OutlinedTextField(
                value = input,
                onValueChange = { text ->
                    if (text.length <= 3 && text.all { it.isDigit() }) input = text
                },
                modifier = Modifier.width(86.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextButton(onClick = {
                input.toIntOrNull()?.let { onSensitivityChange(it) }
                editing = false
            }) { Text("确定") }
        } else {
            Text(
                text = sensitivity.toString(),
                modifier = Modifier
                    .clickable {
                        editing = true
                        input = sensitivity.toString()
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    Slider(
        value = sensitivity.toFloat(),
        onValueChange = { onSensitivityChange(it.roundToInt()) },
        valueRange = 1f..100f,
        modifier = Modifier.fillMaxWidth()
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

    SectionTitle(
        title = "设置页透明度",
        subtitle = "100% 为完全透明（只显示文字），0% 为完全不透明"
    )
    SliderRow(
        title = "透明度",
        valueText = "$transparency%",
        value = transparency.toFloat(),
        range = 0f..100f,
        onValueChange = { onTransparencyChange(it.roundToInt()) }
    )
}

@Composable
private fun MoodSection(
    moodTypes: List<MoodType>,
    iconVersion: Int,
    onPickIcon: (String) -> Unit,
    onRename: (MoodType) -> Unit,
    onDelete: (MoodType) -> Unit,
    onAdd: () -> Unit
) {
    SectionTitle(
        title = "心情表情",
        subtitle = "点表情图标可更换图片；名称可修改，历史记录会同步显示新名字"
    )
    moodTypes.forEach { mood ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = moodIconPainter(mood.id, iconVersion),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onPickIcon(mood.id) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = mood.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (mood.builtin) "内置心情" else "自定义心情",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "改名",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onRename(mood) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = "删除",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clickable { onDelete(mood) }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
    TextButton(
        onClick = onAdd,
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Text("+ 添加自定义心情", fontSize = 13.sp)
    }
}

@Composable
private fun EntrySection(
    dragEnabled: Boolean,
    onDragEnabledChange: (Boolean) -> Unit,
    sizeDp: Int,
    onSizeChange: (Int) -> Unit,
    marginDp: Int,
    onMarginChange: (Int) -> Unit,
    legacyVisible: Boolean,
    onLegacyChange: (Boolean) -> Unit,
    entryIconVersion: Int,
    onPickEntryImage: () -> Unit,
    onResetEntryImage: () -> Unit,
    savedLayouts: List<MoodEntryStore.SavedLayout>,
    restoredHint: String?,
    onRestoreLayout: (MoodEntryStore.SavedLayout) -> Unit,
    onDeleteLayout: (MoodEntryStore.SavedLayout) -> Unit,
    onSaveLayout: () -> Unit,
    onResetLayout: () -> Unit
) {
    val context = LocalContext.current

    SectionTitle(
        title = "心情一览入口",
        subtitle = "左上角圆形按钮，点击进入心情一览；调整时可通过上方透明度看到日历实时变化"
    )
    SwitchRow(
        title = "位置可拖动",
        subtitle = "开启后可在日历页拖动圆形按钮，自动记住位置",
        checked = dragEnabled,
        onCheckedChange = onDragEnabledChange
    )
    SliderRow(
        title = "按钮大小",
        valueText = "${sizeDp}dp",
        value = sizeDp.toFloat(),
        range = MoodEntryStore.MIN_BUTTON_SIZE_DP.toFloat()..
            MoodEntryStore.MAX_BUTTON_SIZE_DP.toFloat(),
        onValueChange = { onSizeChange(it.roundToInt()) }
    )
    SliderRow(
        title = "贴边距离",
        valueText = "${marginDp}dp",
        value = marginDp.toFloat(),
        range = 0f..MoodEntryStore.MAX_EDGE_MARGIN_DP.toFloat(),
        onValueChange = { onMarginChange(it.roundToInt()) },
        hint = "按钮与屏幕边缘保持的最小距离，避免拖动时贴边不好点"
    )
    SwitchRow(
        title = "旧版横条入口",
        subtitle = "在图例下方显示「心情一览 · 共 N 条记录」横条，可与圆形按钮同时使用",
        checked = legacyVisible,
        onCheckedChange = onLegacyChange
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = entryIconPainter(entryIconVersion),
            contentDescription = "心情一览入口预览",
            modifier = Modifier.size(sizeDp.dp)
        )
        TextButton(onClick = onPickEntryImage) {
            Text("更换图片", fontSize = 12.sp)
        }
        if (MoodEntryStore.hasCustomIcon(context)) {
            TextButton(onClick = onResetEntryImage) {
                Text("恢复默认图片", fontSize = 12.sp)
            }
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

    SectionTitle(
        title = "已保存的布局（${savedLayouts.size}）",
        subtitle = "保存后可在这里随时恢复，不同风格可保存多份"
    )
    if (savedLayouts.isEmpty()) {
        Text(
            text = "还没有保存过布局，调整后点下方「保存当前布局」",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
    savedLayouts.forEach { layout ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = layout.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "大小 ${layout.sizeDp}dp · 贴边 ${layout.marginDp}dp · " +
                        "横条${if (layout.legacyVisible) "开" else "关"} · " +
                        formatLayoutTime(layout.savedAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "恢复",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onRestoreLayout(layout) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = "删除",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clickable { onDeleteLayout(layout) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onSaveLayout) {
            Text("保存当前布局", fontSize = 12.sp)
        }
        TextButton(onClick = onResetLayout) {
            Text("恢复默认布局", fontSize = 12.sp)
        }
    }
    if (restoredHint != null) {
        Text(
            text = restoredHint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatLayoutTime(ms: Long): String =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))

private fun moodDefaultRes(moodId: String): Int = when (moodId) {
    CalendarMark.MOOD_HAPPY -> R.drawable.ic_mood_happy
    CalendarMark.MOOD_NORMAL -> R.drawable.ic_mood_normal
    CalendarMark.MOOD_SAD -> R.drawable.ic_mood_sad
    else -> R.drawable.ic_mood_custom
}

@Composable
private fun moodIconPainter(mood: String, version: Int): Painter {
    val context = LocalContext.current
    val customFile = MoodIconStorage.getCustomIconFile(context, mood)
    return if (customFile.exists()) {
        val bitmap = remember(customFile.absolutePath, customFile.lastModified(), version) {
            runCatching { BitmapFactory.decodeFile(customFile.absolutePath)?.asImageBitmap() }.getOrNull()
        }
        if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) }
        else painterResource(id = moodDefaultRes(mood))
    } else {
        painterResource(id = moodDefaultRes(mood))
    }
}

@Composable
private fun entryIconPainter(version: Int): Painter {
    val context = LocalContext.current
    val file = MoodEntryStore.getIconFile(context)
    return if (file.exists()) {
        val bitmap = remember(file.absolutePath, file.lastModified(), version) {
            runCatching { BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() }.getOrNull()
        }
        if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) }
        else painterResource(id = R.drawable.ic_mood_happy)
    } else {
        painterResource(id = R.drawable.ic_mood_happy)
    }
}
