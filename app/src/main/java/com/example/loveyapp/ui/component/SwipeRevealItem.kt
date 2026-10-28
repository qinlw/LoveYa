package com.example.loveyapp.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class RevealValue { Closed, Revealed }

/**
 * QQ 风格左滑露出操作按钮的列表项容器。
 * - 向左滑动内容，露出右侧操作按钮
 * - 点击操作按钮：执行动作并自动收回
 * - 已展开时点击内容本身：先收回，不触发内容点击
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeRevealItem(
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionWidth: Dp = 88.dp,
    actionColor: Color? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val actionWidthPx = with(density) { actionWidth.toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = RevealValue.Closed,
            positionalThreshold = { totalDistance -> totalDistance * 0.3f },
            velocityThreshold = { with(density) { 120.dp.toPx() } },
            animationSpec = tween(durationMillis = 200)
        )
    }
    SideEffect {
        state.updateAnchors(
            DraggableAnchors {
                RevealValue.Closed at 0f
                RevealValue.Revealed at -actionWidthPx
            }
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // 背景操作区（右侧）：仅在内容确实向左滑动后才绘制，避免圆角处透出按钮文字
        val currentOffset = state.offset
        if (!currentOffset.isNaN() && currentOffset < -0.5f) {
            Box(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(actionWidth)
                        .fillMaxHeight()
                        .background(
                            color = actionColor ?: MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        )
                        .clickable {
                            scope.launch { state.animateTo(RevealValue.Closed) }
                            onAction()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actionText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // 前景内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val x = state.offset
                    IntOffset(if (x.isNaN()) 0 else x.roundToInt(), 0)
                }
                .anchoredDraggable(state, Orientation.Horizontal)
        ) {
            content()
            if (state.currentValue == RevealValue.Revealed) {
                // 展开时点击内容先收回，避免误触进入详情
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { scope.launch { state.animateTo(RevealValue.Closed) } }
                )
            }
        }
    }
}
