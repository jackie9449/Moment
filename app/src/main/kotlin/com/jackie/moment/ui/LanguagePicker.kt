package com.jackie.moment.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jackie.moment.R
import com.jackie.moment.preferences.AppLanguage
import com.jackie.moment.preferences.AppLanguages

@Composable
internal fun LanguagePicker() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val selected = AppLanguages.selected(context)
    var expanded by remember { mutableStateOf(false) }
    val systemLabel = stringResource(R.string.language_follow_system)
    val shape = RoundedCornerShape(MomentLayout.ControlCorner)
    Box {
        Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape)
                .testTag("languageSelector")
                .clickable(enabled = activity != null, role = Role.Button) { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.language), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(if (selected == AppLanguage.SYSTEM) systemLabel else selected.nativeName,
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Icon(MomentIcons.DropDown, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 240.dp)) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(if (language == AppLanguage.SYSTEM) systemLabel else language.nativeName) },
                    trailingIcon = { if (selected == language) Icon(MomentIcons.Check, null, Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("language_${language.name}"),
                    onClick = {
                        expanded = false
                        activity?.let { AppLanguages.select(it, language) }
                    },
                )
            }
        }
    }
}
