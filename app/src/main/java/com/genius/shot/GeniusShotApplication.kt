package com.genius.shot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.genius.shot.worker.LabelingWorker
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class GeniusShotApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)

        // ✨ 새벽 3시 예약 시스템 가동
        setupWorkerAtSpecificTime()
    }

    private fun setupWorkerAtSpecificTime() {
        // 1. 새벽 3시까지 남은 시간 계산
        val initialDelay = calculateInitialDelay() // 3시 기준

        // 2. 작업 제약 조건 설정 (새벽에만 돌도록 더욱 강화)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(true)      // 충전 중일 때만 (배터리 보호)
            .setRequiresDeviceIdle(true)    // 기기가 유휴 상태일 때만 (새벽 3~5시 사이 기기를 안 쓸 때)
            .setRequiresBatteryNotLow(true) // 배터리 부족 시 중단
            .build()

        // 3. 24시간 주기로 반복 예약
        val labelingRequest = PeriodicWorkRequestBuilder<LabelingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        // 4. 작업 등록
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyImageLabeling",
            ExistingPeriodicWorkPolicy.KEEP, // 이미 예약되어 있다면 기존 것 유지
            labelingRequest
        )
    }

    /**
     * ✨ 현재 시간으로부터 특정 시간(targetHour)까지의 지연 시간을 밀리초 단위로 계산합니다.
     */
    private fun calculateInitialDelay(): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 설정한 시간이 이미 지났다면 내일 같은 시간으로 설정
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}