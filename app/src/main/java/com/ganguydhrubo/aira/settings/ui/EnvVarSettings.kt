package com.ganguydhrubo.aira.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import com.ganguydhrubo.aira.ui.components.AiraTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ganguydhrubo.aira.R
import com.ganguydhrubo.aira.settings.EnvVarStore
import com.ganguydhrubo.aira.ui.components.GroupCard
import com.ganguydhrubo.aira.ui.components.ListRow
import com.ganguydhrubo.aira.ui.components.AiraDialog
import com.ganguydhrubo.aira.ui.components.PageScaffold
import com.ganguydhrubo.aira.ui.components.RowChevron
import com.ganguydhrubo.aira.ui.theme.AiraTheme

/** 设置 › 环境变量:插件脚本(如 translate)读的名字=值键值对;增删改,空值即删除。 */
@Composable
fun EnvVarSettingsScreen(store: EnvVarStore, onBack: () -> Unit) {
    val vars by store.changes.collectAsState(initial = store.all())
    var adding by remember { mutableStateOf(false) }
    PageScaffold(stringResource(R.string.env_vars_title), onBack) {
        if (vars.isEmpty()) {
            Text(
                stringResource(R.string.env_vars_empty),
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = AiraTheme.colors.ink3,
            )
        } else {
            GroupCard {
                vars.entries.forEachIndexed { i, (k, v) ->
                    ListRow(null, k, v, first = i == 0) {
                        Text(
                            "✕",
                            Modifier.clickable { store.remove(k) }.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = AiraTheme.colors.bad,
                        )
                    }
                }
            }
        }
        GroupCard {
            ListRow(null, stringResource(R.string.env_vars_add), first = true, onClick = { adding = true }) { RowChevron() }
        }
    }
    if (adding) {
        var name by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        AiraDialog(
            onDismissRequest = { adding = false },
            title = { Text(stringResource(R.string.env_vars_add)) },
            text = {
                Column {
                    AiraTextField(
                        value = name, onValueChange = { name = it }, singleLine = true,
                        label = { Text(stringResource(R.string.env_vars_name)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AiraTextField(
                        value = value, onValueChange = { value = it }, singleLine = true,
                        label = { Text(stringResource(R.string.env_vars_value)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { if (name.isNotBlank()) { store.set(name.trim(), value); adding = false } }) {
                    Text(stringResource(R.string.env_vars_add))
                }
            },
            dismissButton = { TextButton(onClick = { adding = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
