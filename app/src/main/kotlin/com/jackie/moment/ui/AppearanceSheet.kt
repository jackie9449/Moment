package com.jackie.moment.ui

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.jackie.moment.R
import com.jackie.moment.preferences.AppearanceStore
import com.jackie.moment.preferences.InterfaceStyle
import com.jackie.moment.preferences.Palette
import com.jackie.moment.preferences.ThemeMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job

private data class SheetGeometry(val containerHeight: Int = 0, val height: Int = 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(appearance: AppearanceStore, tileAdded: Boolean, onDismiss: () -> Unit, onAddTile: () -> Unit,
    onVisibilityChanged: (Float) -> Unit = {}) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val closeProgress = remember { Animatable(0f) }
    var closing by remember { mutableStateOf(false) }
    var backGesture by remember { mutableStateOf(false) }
    var backStartProgress by remember { mutableFloatStateOf(0f) }
    var backMotionJob by remember { mutableStateOf<Job?>(null) }
    var geometry by remember { mutableStateOf(SheetGeometry()) }
    val reportVisibility by rememberUpdatedState(onVisibilityChanged)
    val dismiss by rememberUpdatedState(onDismiss)
    LaunchedEffect(sheetState) {
        snapshotFlow {
            // Offsets initialize during measurement; never force an early read in composition.
            val offset = runCatching { sheetState.requireOffset() }.getOrNull()
            val travel = minOf(geometry.containerHeight, geometry.height).toFloat()
            if (offset == null || !offset.isFinite() || travel <= 0f) 0f
            else ((geometry.containerHeight - offset) / travel).coerceIn(0f, 1f) * (1f - closeProgress.value)
        }.collect { reportVisibility(it) }
    }
    DisposableEffect(Unit) { onDispose { reportVisibility(0f) } }
    val closeSheet: () -> Unit = {
        if (!closing) {
            closing = true
            backMotionJob?.cancel()
            backGesture = false
            scope.launch {
                // 1.4.0's hide spec is internal and uses FastEffects. Animate the
                // remaining screen distance once, then dispose only when offscreen.
                closeProgress.animateTo(1f, tween(420, easing = CubicBezierEasing(.32f, 0f, .18f, 1f)))
                dismiss()
            }
        }
    }
    ModalBottomSheet(onDismissRequest=onDismiss,
        sheetState=sheetState,
        sheetGesturesEnabled=!closing && !backGesture,
        properties=ModalBottomSheetProperties(shouldDismissOnBackPress=false, shouldDismissOnClickOutside=!closing && !backGesture),
        // Keep the library's touch / accessibility dismissal surface; paint our own
        // position-driven scrim instead of its independently animated visible-state fade.
        scrimColor=Color.Transparent,
        dragHandle={ BottomSheetDefaults.DragHandle(Modifier.testTag("appearanceDragHandle")) },
        modifier=Modifier.testTag("appearanceSheet").layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            geometry = SheetGeometry(constraints.maxHeight, placeable.height)
            layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        }.graphicsLayer {
            val progress = closeProgress.value
            translationY = if (progress == 0f) 0f else {
                val offset = runCatching { sheetState.requireOffset() }.getOrDefault(geometry.containerHeight.toFloat())
                if (offset.isFinite()) (geometry.containerHeight - offset).coerceAtLeast(0f) * progress else 0f
            }
        },
        containerColor=MaterialTheme.colorScheme.surfaceContainerLow,
        shape=RoundedCornerShape(topStart=MomentLayout.PageCorner,topEnd=MomentLayout.PageCorner)) {
        SheetBackHandler(
            onStart = {
                if (!closing) {
                    backMotionJob?.cancel()
                    backStartProgress = closeProgress.value
                    backGesture = true
                }
            },
            onProgress = { progress ->
                if (!closing && backGesture) {
                    backMotionJob?.cancel()
                    backMotionJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        closeProgress.snapTo(backStartProgress + (.24f - backStartProgress) * progress.coerceIn(0f, 1f))
                    }
                }
            },
            onCancel = {
                if (!closing && backGesture) {
                    backMotionJob?.cancel()
                    backMotionJob = scope.launch {
                        closeProgress.animateTo(0f, tween(260, easing = CubicBezierEasing(.32f, 0f, .18f, 1f)))
                        backGesture = false
                    }
                }
            },
            onCommit = closeSheet,
        )
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            // The floating Activity theme enables native dim-behind. The sheet
            // dialog inherits it, adding a fixed veil until its window disappears.
            // Only the position-driven Compose scrim should shade the editor.
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setDimAmount(0f)
            onDispose { }
        }
        Column(Modifier.widthIn(max=560.dp).fillMaxWidth().align(Alignment.CenterHorizontally)
            .verticalScroll(rememberScrollState()).padding(start=20.dp,end=20.dp,bottom=24.dp),
            verticalArrangement=Arrangement.spacedBy(24.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text(stringResource(R.string.personalization),style=MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                GlassToolbarButton(closeSheet,Modifier.testTag("closeAppearance")) {
                    Icon(MomentIcons.Close,stringResource(R.string.close_appearance_sheet),Modifier.size(20.dp))
                }
            }
            SettingsSection(R.string.interface_style) {
                Row(Modifier.height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    InterfaceStyle.entries.forEach { style ->
                        StyleChoice(style,style==appearance.style,Modifier.weight(1f).fillMaxHeight()) { appearance.updateStyle(style) }
                    }
                }
            }
            if(appearance.style!=InterfaceStyle.MATERIAL && Build.VERSION.SDK_INT>=30) {
                AppearanceToggleRow(stringResource(R.string.floating_mode),stringResource(R.string.floating_mode_description),
                    appearance.floatingMode,"floatingMode",appearance::updateFloatingMode)
            }
            SettingsSection(R.string.appearance_mode) {
                SegmentedOptions {
                    ThemeMode.entries.forEach { mode ->
                        val selected=mode==appearance.mode
                        SelectionSegment(selected,Modifier.weight(1f).testTag("mode_${mode.name}"),{ appearance.updateMode(mode) }) {
                            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                Icon(when(mode) {
                                    ThemeMode.SYSTEM -> MomentIcons.Device
                                    ThemeMode.LIGHT -> MomentIcons.Sun
                                    ThemeMode.DARK -> MomentIcons.Moon
                                },null,Modifier.size(18.dp),tint=if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stringResource(when(mode) {
                                    ThemeMode.SYSTEM -> R.string.theme_system
                                    ThemeMode.LIGHT -> R.string.theme_light
                                    ThemeMode.DARK -> R.string.theme_dark
                                }),style=MaterialTheme.typography.labelMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                LanguagePicker()
            }
            SettingsSection(R.string.theme_palette) {
                SegmentedOptions {
                    Palette.entries.forEach { palette ->
                        val selected=palette==appearance.palette && !appearance.dynamicColor
                        SelectionSegment(selected,Modifier.weight(1f).testTag("palette_${palette.name}"),{ appearance.updatePalette(palette) }) {
                            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                PaletteDot(when(palette) {
                                    Palette.IRIS -> Color(0xFFBBA5EF)
                                    Palette.OCEAN -> Color(0xFF8FBFEA)
                                    Palette.FOREST -> Color(0xFF97C5A4)
                                },selected)
                                Text(stringResource(when(palette) {
                                    Palette.IRIS -> R.string.palette_iris
                                    Palette.OCEAN -> R.string.palette_ocean
                                    Palette.FOREST -> R.string.palette_forest
                                }),style=MaterialTheme.typography.labelMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
                if(Build.VERSION.SDK_INT>=31) AppearanceToggleRow(stringResource(R.string.dynamic_color),
                    stringResource(R.string.dynamic_color_description),appearance.dynamicColor,"dynamicColor",appearance::setDynamic)
            }
            if(!tileAdded) QuickTileEntry(onAddTile)
        }
    }
}

@Composable
private fun SettingsSection(resource: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(stringResource(resource),style=MaterialTheme.typography.labelMedium,
            color=MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun QuickTileEntry(onAddTile: () -> Unit) {
    val description=stringResource(R.string.add_quick_tile)
    Surface(Modifier.testTag("quickTileCard"),shape=RoundedCornerShape(MomentLayout.ControlCorner),
        color=MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Icon(MomentIcons.Quick,null,Modifier.size(22.dp),tint=MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.quick_tile_title),style=MaterialTheme.typography.bodyMedium.copy(fontWeight=FontWeight.Medium))
                Text(stringResource(R.string.quick_tile_description),style=MaterialTheme.typography.labelMedium,
                    color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onAddTile,modifier=Modifier.heightIn(min=48.dp).testTag("addTile")
                .semantics { contentDescription=description },shape=RoundedCornerShape(MomentLayout.ControlCorner),
                contentPadding=PaddingValues(horizontal=16.dp)) {
                Text(stringResource(R.string.add))
            }
        }
    }
}
