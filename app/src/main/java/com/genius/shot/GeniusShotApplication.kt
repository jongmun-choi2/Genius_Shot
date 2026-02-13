package com.genius.shot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration // ✨ 이거 필수
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.genius.shot.worker.LabelingWorker
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GeniusShotApplication : Application(), Configuration.Provider { // ✨ 인터페이스 구현 필수

    @Inject lateinit var workerFactory: HiltWorkerFactory // ✨ Hilt 공장 주입

    // ✨ WorkManager가 이 설정을 보고 워커를 만듭니다.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        setupWorker()
    }

    private fun setupWorker() {
        val request = OneTimeWorkRequestBuilder<LabelingWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "ImageLabelingWork",
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}