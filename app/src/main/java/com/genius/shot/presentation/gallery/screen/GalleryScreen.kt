package com.genius.shot.presentation.gallery.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
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
    onCleanClick: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    // 1. 상태 수집
    val mainPagingItems = viewModel.galleryData.collectAsLazyPagingItems()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val selectedItems by viewModel.selectedItems.collectAsStateWithLifecycle()

    val isSelectionMode = selectedItems.isNotEmpty()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 뒤로가기 핸들링
    BackHandler(enabled = isSelectionMode) { viewModel.clearSelection() }

    // 삭제 권한 요청 처리
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.onDeletePermissionGranted()
    }

    LaunchedEffect(Unit) {
        viewModel.permissionNeededEvent.collect { intentSender ->
            intentSenderLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshEvent.collect {
            mainPagingItems.refresh()
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
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
                    onDelete = { viewModel.deleteSelectedItems() }
                )
            } else {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("갤러리") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                            }
                        },
                        actions = {
                            IconButton(onClick = onCleanClick) {
                                Icon(Icons.Default.CleaningServices, contentDescription = "정리", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )

                    // 검색바
                    TextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("키워드 검색 (예: 고양이, 바다)") },
                        leadingIcon = {
                            IconButton(onClick = {
                                viewModel.onSearch()
                                focusManager.clearFocus()
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "검색")
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "지우기")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.onSearch()
                            focusManager.clearFocus()
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ✨ 검색어가 있으면 [검색 결과 List]를, 없으면 [전체 Paging Grid]를 보여줍니다.
            if (searchQuery.isNotEmpty()) {
                // --- 검색 모드 ---
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (searchResults.isEmpty()) {
                    Text(
                        text = "검색 결과가 없습니다. 🤔",
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults, key = { "search_${it.item.id}" }) { item ->
                            GalleryImageItem(
                                image = item,
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItems.contains(item.item),
                                onClick = {
                                    if (isSelectionMode) viewModel.toggleSelection(item.item)
                                    else onImageClick(item.item.uri)
                                },
                                onLongClick = { viewModel.toggleSelection(item.item) }
                            )
                        }
                    }
                }
            } else {
                // --- 일반 갤러리 모드 (Paging) ---
                if (mainPagingItems.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (mainPagingItems.itemCount == 0) {
                    Text("표시할 사진이 없습니다.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            count = mainPagingItems.itemCount,
                            key = mainPagingItems.itemKey { item ->
                                when (item) {
                                    is GalleryItem.DateHeader -> "header_${item.date}"
                                    is GalleryItem.Image -> "image_${item.item.id}"
                                }
                            },
                            contentType = mainPagingItems.itemContentType { item ->
                                when (item) {
                                    is GalleryItem.DateHeader -> 0
                                    is GalleryItem.Image -> 1
                                }
                            },
                            span = { index ->
                                when (mainPagingItems[index]) {
                                    is GalleryItem.DateHeader -> GridItemSpan(3)
                                    else -> GridItemSpan(1)
                                }
                            }
                        ) { index ->
                            val item = mainPagingItems[index]
                            if (item != null) {
                                when (item) {
                                    is GalleryItem.DateHeader -> DateHeader(date = item.date)
                                    is GalleryItem.Image -> GalleryImageItem(
                                        image = item,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedItems.contains(item.item),
                                        onClick = {
                                            if (isSelectionMode) viewModel.toggleSelection(item.item)
                                            else onImageClick(item.item.uri)
                                        },
                                        onLongClick = { viewModel.toggleSelection(item.item) }
                                    )
                                }
                            } else {
                                // Placeholder
                                Box(modifier = Modifier.aspectRatio(1f).background(Color.LightGray))
                            }
                        }
                    }
                }

                // 에러 처리
                if (mainPagingItems.loadState.refresh is LoadState.Error) {
                    Button(
                        onClick = { mainPagingItems.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    ) { Text("다시 시도") }
                }
            }
        }
    }
}