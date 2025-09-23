package com.example.onlyone.views.settingsView

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlyone.BuildConfig
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.ads.LocalConsentManager // ✅

@Composable
fun PrivacySettingsView(
    theme: ThemeTokens
) {
    val consentManager = LocalConsentManager.current            // ✅ from CompositionLocal
    val context = LocalContext.current
    val activity = context as Activity
    var showReviewConsent by remember { mutableStateOf(consentManager.isPrivacyOptionsRequired()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        SectionTitle(text = stringResource(R.string.settings_privacy_header), theme = theme)
        SmallGrey(text = stringResource(R.string.settings_privacy_subtitle), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_intro_title), theme = theme)
        Body(text = stringResource(R.string.settings_privacy_intro_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_collect_title), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_1), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_2), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_3), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_4), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_5), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_collect_6), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_use_title), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_use_1), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_use_2), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_use_3), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_use_4), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_store_title), theme = theme)
        Body(text = stringResource(R.string.settings_privacy_store_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_retention_title), theme = theme)
        Body(text = stringResource(R.string.settings_privacy_retention_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_choices_title), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_choices_1), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_choices_2), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_choices_3), theme = theme)
        Bullet(text = stringResource(R.string.settings_privacy_choices_4), theme = theme)

        // === ACTION BUTTONS ===
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PRIVACY_URL))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open privacy policy")
        }

        if (showReviewConsent) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    consentManager.showPrivacyOptions(activity) { /* err -> snackbar? */ }
                    showReviewConsent = consentManager.isPrivacyOptionsRequired()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_privacy_review_consent))
            }
        }
        // === /ACTION BUTTONS ===

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_privacy_contact_title), theme = theme)
        Body(text = stringResource(R.string.settings_privacy_contact_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SmallGrey(text = stringResource(R.string.settings_privacy_last_updated), theme = theme)
    }
}
@Composable private fun SectionTitle(text: String, theme: ThemeTokens) {
    Text(text, color = theme.textColor, fontSize = 18.sp)
}
@Composable private fun SectionSubtitle(text: String, theme: ThemeTokens) {
    Text(text, color = theme.textColor, style = MaterialTheme.typography.subtitle1)
}
@Composable private fun Body(text: String, theme: ThemeTokens) {
    Text(text, color = theme.textColor.copy(alpha = 0.85f), style = MaterialTheme.typography.body1)
}
@Composable private fun SmallGrey(text: String, theme: ThemeTokens) {
    Text(text, color = theme.textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.body2)
}
@Composable private fun Bullet(text: String, theme: ThemeTokens) {
    Row(Modifier.padding(bottom = 4.dp)) {
        Text("• ", color = theme.textColor, style = MaterialTheme.typography.body1)
        Text(text, color = theme.textColor.copy(alpha = 0.9f), style = MaterialTheme.typography.body1)
    }
}
