package com.jackie.moment.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jackie.moment.R
import com.jackie.moment.preferences.InterfaceStyle

@Composable
internal fun StyleChoice(style: InterfaceStyle, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(MomentLayout.ControlCorner)
    Surface(modifier.clip(shape).testTag("style_${style.name}")
        .selectable(selected,onClick=onClick,role=Role.RadioButton), shape=shape,
        color=MaterialTheme.colorScheme.surfaceContainerLowest,
        border=BorderStroke(if (selected) 1.2.dp else .7.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha=.65f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha=.55f))) {
        Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            ThemePreview(style,Modifier.fillMaxWidth().height(54.dp))
            Row(Modifier.fillMaxWidth().heightIn(min=24.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(stringResource(when(style) {
                    InterfaceStyle.MATERIAL -> R.string.style_material
                    InterfaceStyle.LIQUID_GLASS -> R.string.style_liquid_glass
                    InterfaceStyle.FROSTED_GLASS -> R.string.style_frosted_glass
                }),style=MaterialTheme.typography.labelLarge,modifier=Modifier.weight(1f),minLines=2,maxLines=2,overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.width(3.dp))
                if(selected) Icon(MomentIcons.Check,null,Modifier.size(15.dp),tint=MaterialTheme.colorScheme.primary)
                else Spacer(Modifier.size(15.dp))
            }
        }
    }
}

@Composable
internal fun SegmentedOptions(content: @Composable RowScope.() -> Unit) {
    Surface(shape=RoundedCornerShape(MomentLayout.ControlCorner),color=MaterialTheme.colorScheme.surfaceContainerLowest,
        border=BorderStroke(.7.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))) {
        Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),content=content)
    }
}

@Composable
internal fun SelectionSegment(selected: Boolean, modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier.heightIn(min=48.dp).clip(RoundedCornerShape(12.dp))
        .background(if(selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha=.65f) else Color.Transparent)
        .selectable(selected,onClick=onClick,role=Role.RadioButton).padding(horizontal=6.dp,vertical=10.dp),
        contentAlignment=Alignment.Center) {
        content()
    }
}

@Composable
internal fun AppearanceToggleRow(title: String, description: String, checked: Boolean, tag: String,
    onCheckedChange: (Boolean) -> Unit) {
    val shape=RoundedCornerShape(MomentLayout.ControlCorner)
    Surface(shape=shape,color=MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(Modifier.fillMaxWidth().clip(shape).testTag(tag)
            .toggleable(checked,role=Role.Switch,onValueChange=onCheckedChange)
            .padding(horizontal=16.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
                Text(title,style=MaterialTheme.typography.bodyMedium.copy(fontWeight=FontWeight.Medium))
                Text(description,style=MaterialTheme.typography.labelMedium.copy(fontWeight=FontWeight.Normal),
                    color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked=checked,onCheckedChange=null)
        }
    }
}

@Composable
internal fun PaletteDot(color: Color, selected: Boolean) {
    Box(Modifier.size(18.dp).background(color,CircleShape),contentAlignment=Alignment.Center) {
        if(selected) Icon(MomentIcons.Check,null,Modifier.size(12.dp),tint=Color(0xFF292536))
    }
}
