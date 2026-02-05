package com.genius.shot.presentation.gallery.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.genius.shot.domain.model.GalleryItem
import com.genius.shot.presentation.gallery.component.DateHeader
import com.genius.shot.presentation.gallery.component.GalleryImageItem
import com.genius.shot.presentation.gallery.component.SelectionTopAppBar
import com.genius.shot.presentation.gallery.viewmodel.GalleryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onBackClick: () -> Unit,
    onImageClick: (Uri) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {

    val pagingItems: LazyPagingItems<GalleryItem> = viewModel.galleryData.collectAsLazyPagingItems()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()
    val isSelectionMode = selectedItems.isNotEmpty()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사용자가 "허용"을 눌렀음 -> 다시 삭제 시도
            viewModel.onDeletePermissionGranted()
        }
    }

    // ✨ 2. 뷰모델에서 권한 요청 이벤트가 오면 런처 실행
    LaunchedEffect(Unit) {
        viewModel.permissionNeededEvent.collect { intentSender ->
            val intentSenderRequest = IntentSenderRequest.Builder(intentSender).build()
            intentSenderLauncher.launch(intentSenderRequest)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshEvent.collect {
            pagingItems.refresh() // PagingSource를 다시 실행시킴 (UI 갱신)
        }
    }

    Scaffold(
        topBar = {
            if(isSelectionMode) {
                SelectionTopAppBar(
                    selectionCount = selectedItems.size,
                    onClear = { viewModel.clearSelection() },
                    onShare = {
                        val uris = ArrayList(selectedItems.map { it.uri })
                        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/*"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "사진 공유"))
                    },
                    onDelete = { showDeleteDialog = true }
                )
            }else {
                CenterAlignedTopAppBar(
                    title = { Text("갤러리") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading) {
                Text(
                    text = "표시할 사진이 없습니다.\n사진을 찍거나 권한을 확인해주세요.",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            val refreshState = pagingItems.loadState.refresh
            if(refreshState is LoadState.Error) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "불러오기 실패 \uD83D\uDE22", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text(text = refreshState.error.localizedMessage ?: "알 수 없는 오류")
                    Button(onClick = { pagingItems.retry() }) {
                        Text("다시 시도")
                    }
                }
            }

            if(refreshState is LoadState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if(pagingItems.itemCount > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { item ->
                            when(item) {
                                is GalleryItem.DateHeader -> "header_${item.date}"
                                is GalleryItem.Image -> "image_${item.item.id}"
                            }
                        },
                        contentType = pagingItems.itemContentType { item ->
                            when(item) {
                                is GalleryItem.DateHeader -> 0
                                is GalleryItem.Image -> 1
                            }
                        },
                        span = { index ->
                            when (pagingItems[index]) {
                                is GalleryItem.DateHeader -> GridItemSpan(3)
                                is GalleryItem.Image -> GridItemSpan(1)
                                null -> GridItemSpan(1)
                            }
                        }
                    ) { index ->
                        when( val item = pagingItems[index]) {
                            is GalleryItem.DateHeader -> DateHeader(date = item.date)
                            is GalleryItem.Image -> GalleryImageItem(
                                image = item,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItems.contains(item.item),
                                onClick = { onImageClick(item.item.uri) },
                                onLongClick = {viewModel.toggleSelection(item.item)}
                            )
                            null -> {
                                Box(
                                    modifier = Modifier.aspectRatio(1f).background(Color.LightGray)
                                )
                            }
                        }
                    }
                }
            }

        }

    }

    if(showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("${selectedItems.size}장의 사진 삭제") },
            text = { Text("선택한 사진을 모두 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedItems()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

}