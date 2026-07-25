package com.example.loveyapp.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.runtime.Composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FadeInAnimation(
    targetState: Boolean,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = targetState,
        enter = fadeIn(tween(duration)),
        exit = fadeOut(tween(duration)),
        content = { content() }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SlideInUpAnimation(
    targetState: Boolean,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = targetState,
        enter = slideInVertically(tween(duration), { it }) + fadeIn(tween(duration)),
        exit = slideOutVertically(tween(duration), { it }) + fadeOut(tween(duration)),
        content = { content() }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> PageTransitionAnimation(
    targetState: T,
    duration: Int = 300,
    contentKey: (T) -> Any = { it as Any },
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            slideInHorizontally(tween(duration), { it }) + fadeIn(tween(duration)) with
                    slideOutHorizontally(tween(duration), { -it }) + fadeOut(tween(duration))
        },
        contentKey = contentKey,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ListItemAnimation(
    index: Int,
    isVisible: Boolean,
    duration: Int = 300,
    delay: Int = 50,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(tween(duration, index * delay), { 20 }) + fadeIn(tween(duration, index * delay)),
        exit = slideOutVertically(tween(duration), { -20 }) + fadeOut(tween(duration)),
        content = { content() }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> CrossfadeAnimation(
    targetState: T,
    duration: Int = 300,
    contentKey: (T) -> Any = { it as Any },
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            fadeIn(tween(duration)) with fadeOut(tween(duration))
        },
        contentKey = contentKey,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SlideInLeftAnimation(
    targetState: Boolean,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = targetState,
        enter = slideInHorizontally(tween(duration), { -it }) + fadeIn(tween(duration)),
        exit = slideOutHorizontally(tween(duration), { -it }) + fadeOut(tween(duration)),
        content = { content() }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SlideInRightAnimation(
    targetState: Boolean,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = targetState,
        enter = slideInHorizontally(tween(duration), { it }) + fadeIn(tween(duration)),
        exit = slideOutHorizontally(tween(duration), { it }) + fadeOut(tween(duration)),
        content = { content() }
    )
}