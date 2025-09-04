import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.onlyone.R
import com.example.onlyone.composables.CustomColorOverlay
import com.example.onlyone.theme.ThemeTokens
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110" // TODO: replace with real ID from admob unit

@Composable
fun NativeAdGateCard(
    theme: ThemeTokens,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load once
    LaunchedEffect(Unit) {
        val loader = AdLoader.Builder(ctx, TEST_NATIVE_ID)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
                isLoading = false
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .build()
        loader.loadAd(AdRequest.Builder().build())
    }

    // Clean up
    DisposableEffect(Unit) {
        onDispose { nativeAd?.destroy() }
    }

    CustomColorOverlay(
        modifier = modifier,
        paddingBox1 = PaddingValues(1.dp),
        paddingBox2 = PaddingValues(12.dp),
        gradientColor1 = theme.gradientColor1.copy(alpha = 0.95f),
        gradientColor2 = theme.gradientColor2.copy(alpha = 0.95f),
        borderWidth = 1.dp,
        shape = RoundedCornerShape(20.dp),
        theme = theme,
        onDismiss = {}
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.ad_sponsored), style = MaterialTheme.typography.caption, color = theme.textColor.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = theme.textColor) }
            } else {
                nativeAd?.let { ad ->
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        factory = { context ->
                            buildNativeAdView(context).also { bindNativeAd(it, ad) }
                        },
                        update = { adView -> bindNativeAd(adView, ad) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.ad_continue), color = theme.textColor) }
        }
    }
}

private fun buildNativeAdView(context: android.content.Context): NativeAdView {
    val adView = NativeAdView(context)

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(24, 24, 24, 24)
    }

    val media = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (200 * context.resources.displayMetrics.density).toInt()
        )
    }
    val headline = TextView(context).apply { textSize = 18f }
    val body = TextView(context)
    val advertiser = TextView(context).apply { textSize = 12f }
    val icon = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(64, 64)
        visibility = View.GONE
    }
    val cta = android.widget.Button(context)

    // Order: media, headline+icon, body, advertiser, CTA
    root.addView(media)
    root.addView(headline)
    root.addView(icon)
    root.addView(body)
    root.addView(advertiser)
    root.addView(cta)

    adView.mediaView = media
    adView.headlineView = headline
    adView.bodyView = body
    adView.advertiserView = advertiser
    adView.iconView = icon
    adView.callToActionView = cta

    adView.addView(root)
    return adView
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    (adView.headlineView as TextView).text = ad.headline
    ad.mediaContent?.let { adView.mediaView?.setMediaContent(it) }

    (adView.bodyView as TextView).apply {
        text = ad.body ?: ""
        visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (adView.advertiserView as TextView).apply {
        text = ad.advertiser ?: ""
        visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    (adView.iconView as ImageView).apply {
        val icon = ad.icon
        if (icon != null) {
            setImageDrawable(icon.drawable)
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }
    (adView.callToActionView as android.widget.Button).apply {
        text = ad.callToAction ?: adView.context.getString(R.string.ad_cta_learn_more)
        visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    adView.setNativeAd(ad)
}
