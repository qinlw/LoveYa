package com.example.loveyapp.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * 离开编辑页前的未保存提示：可选保存后退出、不保存退出或继续编辑。
 */
@Composable
fun UnsavedChangesDialog(
    canSave: Boolean,
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
    onContinueEditing: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinueEditing,
        title = { Text("尚未保存") },
        text = { Text("当前编辑的内容还没有保存，是否保存后退出？") },
        confirmButton = {
            TextButton(
                onClick = onSaveAndExit,
                enabled = canSave
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscardAndExit) {
                    Text("不保存")
                }
                TextButton(onClick = onContinueEditing) {
                    Text("继续编辑")
                }
            }
        }
    )
}
