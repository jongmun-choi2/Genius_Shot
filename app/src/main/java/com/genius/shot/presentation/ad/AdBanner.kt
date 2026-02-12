package com.genius.shot.presentation.ad

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.genius.shot.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // 개발용 테스트 광고 단위 ID (출시 시 교체 필수)
                adUnitId = if(BuildConfig.IS_DEV) "ca-app-pub-3940256099942544/6300978111" else "ca-app-pub-2228020730126116/8506473411"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}