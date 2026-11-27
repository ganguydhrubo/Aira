package com.ganguydhrubo.aira.settings.ui

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.i18n.trPlural
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.AiraDialog
import com.ganguydhrubo.aira.ui.components.RadioRow
import com.ganguydhrubo.aira.ui.components.RowChevron
import com.ganguydhrubo.aira.ui.components.RowValue
import com.ganguydhrubo.aira.ui.theme.AiraIcons

/** 只认几档的设置(沙箱时长、上传体积、工作区空间等):一行显示当前档位,点开单选;没有「不限制」。 */
@Composable
internal fun TierSettingRow(
    @StringRes title: Int,
    @StringRes description: Int,
    tiers: List<Int>,
    @PluralsRes label: Int,
    current: Int,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf(false) }
    ListRow(AiraIcons.Clock, stringResource(title), onClick = { editing = true }, modifier = modifier) {
        RowValue(trPlural(label, current))
        RowChevron()
    }
    if (editing) AiraDialog(
        onDismissRequest = { editing = false },
        title = { Text(stringResource(title)) },
        text = {
            Column {
                Text(stringResource(description), Modifier.padding(bottom = 8.dp))
                tiers.forEachIndexed { i, value ->
                    RadioRow(trPlural(label, value), selected = current == value, onClick = { onPick(value); editing = false }, first = i == 0)
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { editing = false }) { Text(stringResource(R.string.cancel)) } },
    )
}
