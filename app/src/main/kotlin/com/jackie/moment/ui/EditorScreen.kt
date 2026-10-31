package com.jackie.moment.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jackie.moment.R
import com.jackie.moment.editor.EditorSessionModel
import com.jackie.moment.editor.TextMetrics
import com.jackie.moment.preferences.AppearanceStore
import kotlin.math.roundToInt

data class ClipboardSession(val id: Long, val text: String, val hasText: Boolean)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    appearance: AppearanceStore,
    editorSession: EditorSessionModel,
    tileAdded: Boolean,
    session: ClipboardSession?,
    openAppearance: Boolean,
    onAppearanceConsumed: () -> Unit,
    onAddTile: () -> Unit,
    onFinish: (String) -> Boolean,
) {
    val editor = rememberSaveable(saver = editorSession.saver) { editorSession.editor() }
    var processedSession by rememberSaveable { mutableLongStateOf(0) }
    var importedText by rememberSaveable { mutableStateOf(false) }
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    val appearanceVisibility = remember { mutableFloatStateOf(if (showAppearance) 1f else 0f) }
    val appearanceScrim = MaterialTheme.colorScheme.scrim.copy(alpha = .32f)
    val fieldInteractions = remember { MutableInteractionSource() }
    val focused by fieldInteractions.collectIsFocusedAsState()
    var completing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val haptics = LocalHapticFeedback.current
    val metrics = remember(editor.value.text) { TextMetrics.from(editor.value.text) }
    val imeVisible = WindowInsets.isImeVisible
    val editorScroll = rememberScrollState()
    var editorViewportHeight by remember { mutableIntStateOf(0) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val caretMargin = with(LocalDensity.current) { 12.dp.toPx() }
    var entered by remember { mutableStateOf(false) }
    val entrance by animateFloatAsState(if (entered) 1f else 0f, tween(340), label = "entrance")

    LaunchedEffect(Unit) { entered = true }
    LaunchedEffect(session?.id) {
        if (session != null && session.id != processedSession) {
            editor.startSession(session.text)
            processedSession = session.id
            importedText = session.hasText
            showAppearance = false
            withFrameNanos { }
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    LaunchedEffect(openAppearance) {
        if (openAppearance) {
            showAppearance = true
            keyboard?.hide()
            onAppearanceConsumed()
        }
    }

    // The outer scroll container sees a tall text field, not its insertion
    // point. Re-evaluate the caret after every actual viewport resize (including
    // IME animation frames), even when focus and selection haven't changed.
    // Scroll offset is deliberately not a key: manual browsing must stay free.
    LaunchedEffect(imeVisible, focused, editorViewportHeight, textLayout, editor.value.selection) {
        val layout = textLayout ?: return@LaunchedEffect
        if (!imeVisible || !focused || editorViewportHeight <= 0 ||
            layout.layoutInput.text.text != editor.value.text) return@LaunchedEffect
        val cursor = layout.getCursorRect(editor.value.selection.end.coerceIn(0, layout.layoutInput.text.length))
        val margin = caretMargin.coerceAtMost(((editorViewportHeight - cursor.height) / 2f).coerceAtLeast(0f))
        val current = editorScroll.value.toFloat()
        val target = when {
            cursor.bottom + margin > current + editorViewportHeight -> cursor.bottom + margin - editorViewportHeight
            cursor.top - margin < current -> cursor.top - margin
            else -> current
        }.roundToInt().coerceIn(0, editorScroll.maxValue)
        if (target != editorScroll.value) editorScroll.scrollTo(target)
    }

    val glassAppearance = LocalGlassAppearance.current
    val glass = glassAppearance.enabled
    val scene = remember { GlassScene() }
    CompositionLocalProvider(LocalGlassScene provides scene) {
        Surface(Modifier.fillMaxSize(), color = if (glassAppearance.floating) Color.Transparent else MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                if (glassAppearance.floating) FloatingBackdrop(Modifier.matchParentSize())
                else if (glassAppearance.frosted) FrostedBackdrop(Modifier.matchParentSize())
                else if (glass) GlassBackdrop(Modifier.matchParentSize())
                BoxWithConstraints(
                    Modifier.widthIn(max = 840.dp).fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                        .navigationBarsPadding(),
                ) {
                    // Decorative layout follows the window, not the intermediate
                    // heights produced by the keyboard animation.
                    val compact = maxHeight < 540.dp
                    BoxWithConstraints(Modifier.fillMaxSize().imePadding()) {
                        val minimal = maxHeight < 300.dp
                        Column(
                            Modifier.fillMaxSize()
                                .padding(horizontal = if (compact) 16.dp else MomentLayout.PageGutter,
                                    vertical = if (compact) 8.dp else 16.dp)
                                .graphicsLayer { alpha = entrance; translationY = (1f - entrance) * 18f },
                            verticalArrangement = Arrangement.spacedBy(MomentLayout.Gap),
                        ) {
                            EditorDocumentSurface(Modifier.fillMaxWidth().weight(1f)) {
                                Column(Modifier.fillMaxSize()) {
                                    EditorSheetHeader(
                                        importedText = importedText,
                                        edited = editor.canUndo || editor.canRedo,
                                        metrics = metrics,
                                        compact = compact,
                                        minimal = minimal,
                                        completing = completing,
                                        onDone = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            completing = true
                                            if (!onFinish(editor.value.text)) completing = false
                                        },
                                    )
                                    HorizontalDivider(Modifier.padding(horizontal = 20.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))
                                    Box(Modifier.fillMaxWidth().weight(1f)) {
                                        val editorDescription = stringResource(R.string.text_editor)
                                        BasicTextField(
                                            value = editor.value,
                                            onValueChange = editor::update,
                                            modifier = Modifier.fillMaxSize()
                                                .padding(start = 20.dp, end = 20.dp,
                                                    top = if (compact) 16.dp else 20.dp, bottom = 20.dp)
                                                .focusRequester(focusRequester)
                                                .testTag("editor")
                                                .semantics { contentDescription = editorDescription }
                                                .onSizeChanged { editorViewportHeight = it.height }
                                                .verticalScroll(editorScroll),
                                            onTextLayout = { textLayout = it },
                                            interactionSource = fieldInteractions,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = appearance.fontSize.sp,
                                                lineHeight = (appearance.fontSize * 1.75f).sp,
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.Sentences,
                                                keyboardType = KeyboardType.Text,
                                            ),
                                            decorationBox = { inner ->
                                                Box(Modifier.fillMaxSize()) {
                                                    if (editor.value.text.isEmpty()) {
                                                        Column {
                                                            Text(stringResource(R.string.editor_hint), style = MaterialTheme.typography.bodyLarge.copy(
                                                                fontSize = appearance.fontSize.sp, lineHeight = (appearance.fontSize * 1.75f).sp,
                                                            ), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Spacer(Modifier.height(12.dp))
                                                            Text(stringResource(R.string.empty_editor_help), style = MaterialTheme.typography.bodyMedium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                    inner()
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                            if (!minimal) EditorSheetToolbar(editor, appearance, onAppearance = {
                                keyboard?.hide()
                                showAppearance = true
                            })
                        }
                    }
                }
                if (showAppearance) Canvas(Modifier.matchParentSize()) {
                    drawRect(appearanceScrim, alpha = appearanceVisibility.floatValue)
                }
            }
        }
    }
    if (showAppearance) AppearanceSheet(appearance, tileAdded, onDismiss = { showAppearance = false }, onAddTile = onAddTile,
        onVisibilityChanged = { appearanceVisibility.floatValue = it })
}
