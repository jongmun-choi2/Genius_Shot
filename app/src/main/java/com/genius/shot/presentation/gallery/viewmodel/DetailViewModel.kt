package com.genius.shot.presentation.gallery.viewmodel

import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genius.shot.data.repository.GalleryRepository
import com.genius.shot.domain.usecase.DeleteImageUseCase
import com.genius.shot.domain.usecase.NotifyDataChangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val deleteImageUseCase: DeleteImageUseCase,
    private val notifyDataChangeUseCase: NotifyDataChangeUseCase,
) : ViewModel() {

    // ✨ UI에 "팝업 띄워줘"라고 요청할 채널
    private val _permissionNeededEvent = Channel<IntentSender>()
    val permissionNeededEvent = _permissionNeededEvent.receiveAsFlow()

    private val _popBackStackEvent = Channel<Unit>()
    val popBackStackEvent = _popBackStackEvent.receiveAsFlow()

    fun deleteImage(uri: Uri) {
        viewModelScope.launch {
            try {
                // minSdk 32이므로 무조건 createDeleteRequest 사용 가능!
                val intentSender = deleteImageUseCase(uri)
                _permissionNeededEvent.send(intentSender)
            } catch (e: Exception) {
                e.printStackTrace()
                // 혹시 모를 에러 처리
            }
        }
    }

    fun onDeleteSuccess() {
        viewModelScope.launch {
            notifyDataChangeUseCase()
            _popBackStackEvent.send(Unit)
        }
    }

}