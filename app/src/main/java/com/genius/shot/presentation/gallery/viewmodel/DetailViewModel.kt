package com.genius.shot.presentation.gallery.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genius.shot.data.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: GalleryRepository
) : ViewModel() {

    private val _deleteEvent = Channel<Unit>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    fun deleteImage(uri: Uri) {
        viewModelScope.launch {
            repository.deleteImage(uri)
            _deleteEvent.send(Unit)
        }
    }

}