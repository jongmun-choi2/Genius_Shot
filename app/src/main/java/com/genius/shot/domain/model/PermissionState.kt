package com.genius.shot.domain.model

sealed class PermissionState {
    object Granted : PermissionState()
    object Denied : PermissionState()
    object ShouldShowRationale : PermissionState()
    object PermanentlyDenied : PermissionState()
}