package com.genius.shot.presentation.gallery.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectionCount: Int,
    onClear: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {

    TopAppBar(
        title = { Text("$selectionCount 선택됨", fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "선택 취소")
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "공유")
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )

}