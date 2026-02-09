package com.genius.shot.presentation.analyze.screen

import android.R
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genius.shot.domain.model.ScanStatus
import com.genius.shot.presentation.analyze.component.DuplicateGroupItem
import com.genius.shot.presentation.analyze.component.DuplicateGroupsList
import com.genius.shot.presentation.analyze.component.EmptyView
import com.genius.shot.presentation.analyze.component.ErrorView
import com.genius.shot.presentation.analyze.component.LoadingView
import com.genius.shot.presentation.analyze.viewmodel.DuplicateCheckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateCheckScreen(
    onBackClick: () -> Unit,
    viewModel: DuplicateCheckViewModel = hiltViewModel()
) {

    val duplicateGroups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val analyzedCount by viewModel.analyzedCount.collectAsStateWithLifecycle()
    val hasMoreImages by viewModel.hasMoreImages.collectAsStateWithLifecycle()
    val progressMessage by viewModel.progressMessage.collectAsStateWithLifecycle()

    // ✨ 삭제 권한 팝업 런처
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onDeletionSuccess() // 삭제 성공 시 목록 갱신 요청
        }
    }

    // ✨ 권한 요청 이벤트 구독
    LaunchedEffect(Unit) {
        viewModel.permissionNeededEvent.collect { intentSender ->
            intentSenderLauncher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("중복 사진 정리") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // ✨ Idle 케이스 삭제됨 -> 코드가 더 깔끔해짐
            if (duplicateGroups.isEmpty() && !isLoading && analyzedCount > 0 && !hasMoreImages) {
                EmptyView() // 전체 다 분석했는데 중복 없음
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. 상단 정보
                    item {
                        Text(
                            text = "총 ${duplicateGroups.size}개의 중복 그룹 발견 (분석됨: ${analyzedCount}장)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 2. 그룹 리스트
                    items(duplicateGroups) { groupUris ->
                        DuplicateGroupItem(
                            uris = groupUris,
                            onCleanupClick = { keepUri ->
                                viewModel.deleteDuplicatesInGroup(keepUri, groupUris)
                            }
                        )
                    }

                    // 3. ✨ 다음 페이지 분석 버튼 (Footer)
                    item {
                        if (hasMoreImages) {
                            Button(
                                onClick = { viewModel.scanNextBatch() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isLoading, // 로딩 중엔 클릭 방지
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp

                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(progressMessage)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("다음 300장 분석하기")
                                }
                            }
                        } else {
                            // 더 이상 사진이 없을 때
                            Text(
                                text = "모든 사진 분석 완료",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 초기 로딩 시 화면 중앙 인디케이터 (데이터가 아예 없을 때만)
            if (isLoading && analyzedCount == 0) {
                LoadingView(message = "첫 300장 분석 중...")
            }
        }
    }

}