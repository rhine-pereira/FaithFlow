package com.rhinepereira.faithflow.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

enum class TutorialStep {
    ADD_CATEGORY_BTN,
    ADD_NOTE_FAB,
    RICH_TEXT_EDITOR,
    ADD_THEME_FAB,
    DONE
}

class TutorialState(initialStep: TutorialStep = TutorialStep.ADD_CATEGORY_BTN) {
    var currentStep by mutableStateOf(initialStep)
    val targets = mutableStateMapOf<TutorialStep, Rect>()
    var isActive by mutableStateOf(initialStep != TutorialStep.DONE)

    fun registerTarget(step: TutorialStep, bounds: Rect) {
        targets[step] = bounds
    }

    fun next() {
        val values = TutorialStep.entries
        val nextIndex = values.indexOf(currentStep) + 1
        if (nextIndex < values.size) {
            currentStep = values[nextIndex]
            if (currentStep == TutorialStep.DONE) {
                isActive = false
            }
        } else {
            isActive = false
            currentStep = TutorialStep.DONE
        }
    }
    
    fun skip() {
        isActive = false
        currentStep = TutorialStep.DONE
    }
}

val LocalTutorialState = compositionLocalOf<TutorialState?> { null }

@Composable
fun Modifier.tutorialTarget(step: TutorialStep): Modifier {
    val state = LocalTutorialState.current
    return if (state?.isActive == true) {
        this.onGloballyPositioned { coordinates ->
            state.registerTarget(step, coordinates.boundsInRoot())
        }
    } else {
        this
    }
}

@Composable
fun TutorialOverlay(state: TutorialState, onFinished: () -> Unit) {
    if (!state.isActive || state.currentStep == TutorialStep.DONE) return

    val targetRect = state.targets[state.currentStep]
    val animAlpha by animateFloatAsState(targetValue = if (targetRect != null) 0.8f else 0f, label = "ScrimAlpha")

    // Draw full screen scrim with a punched out hole
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
    ) {
        drawRect(color = Color.Black.copy(alpha = animAlpha))
        if (targetRect != null) {
            // Expand the cutout slightly
            val padding = 8.dp.toPx()
            val drawRect = Rect(
                left = targetRect.left - padding,
                top = targetRect.top - padding,
                right = targetRect.right + padding,
                bottom = targetRect.bottom + padding
            )
            drawRoundRect(
                color = Color.Transparent,
                topLeft = drawRect.topLeft,
                size = drawRect.size,
                cornerRadius = CornerRadius(16.dp.toPx()),
                blendMode = BlendMode.Clear
            )
        }
    }

    // Draw the tooltip popup
    if (targetRect != null) {
        val density = LocalDensity.current
        val screenHeight = with(density) { MaterialTheme.typography.bodyLarge.fontSize.toDp().value * 50 } // Approx

        // Simple heuristic: if target is in bottom half, show tooltip above it, else below.
        val showAbove = targetRect.center.y > 1000f // We should ideally pass screen size, but relying on center relative to popups
        
        // As Popup handles its own window, we can just center it roughly near the target or use standard compose layout
        Box(modifier = Modifier.fillMaxSize()) {
             Column(
                 modifier = Modifier
                     .align(Alignment.Center)
                     .padding(32.dp)
                     .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                     .padding(24.dp),
                 horizontalAlignment = Alignment.CenterHorizontally
             ) {
                 val content = getTutorialContent(state.currentStep)
                 if (content.emoji.isNotEmpty()) {
                     Text(content.emoji, style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(bottom = 16.dp))
                 }
                 Text(
                     text = content.title,
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.onSurface
                 )
                 Spacer(modifier = Modifier.height(16.dp))
                 Text(
                     text = content.description,
                     style = MaterialTheme.typography.bodyLarge,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center
                 )
                 Spacer(modifier = Modifier.height(24.dp))
                 Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                     TextButton(onClick = { state.skip() }) {
                         Text("Skip Tutorial")
                     }
                     Button(onClick = { state.next() }) {
                         Text(if (state.currentStep == TutorialStep.ADD_THEME_FAB) "Done" else "Next")
                     }
                 }
             }
        }
    }
}

private data class TutorialContent(val title: String, val description: String, val emoji: String)

private fun getTutorialContent(step: TutorialStep): TutorialContent {
    return when (step) {
        TutorialStep.ADD_CATEGORY_BTN -> TutorialContent(
            "Organise with Categories",
            "Welcome! In Notes, you can create categories (like 'Sermon Notes' or 'Prayers') to keep things neat. Tap 'Add Category' to start.",
            "📁"
        )
        TutorialStep.ADD_NOTE_FAB -> TutorialContent(
            "Write a Note",
            "Tap the '+' button to start a new note. You can format text with bold (**), italics (_), or lists (1. or -)!",
            "📝"
        )
        TutorialStep.RICH_TEXT_EDITOR -> TutorialContent(
            "Smart Verse Import",
            "While writing a note, type a reference like 'John 3:16' and wait a second — a prompt will appear to auto-insert the verse!",
            "⚡"
        )
        TutorialStep.ADD_THEME_FAB -> TutorialContent(
            "Verse Themes",
            "Also check out the 'Verse Themes' tab! You can group your favourite Bible verses by theme — like 'Faith' or 'Hope'.",
            "📚"
        )
        TutorialStep.DONE -> TutorialContent("", "", "")
    }
}
