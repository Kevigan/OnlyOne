// AboutSettingsView.kt
package com.onlyone.app.views.settingsView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlyone.app.BuildConfig
import com.onlyone.app.R
import com.onlyone.app.theme.ThemeTokens

@Composable
fun AboutSettingsView(theme: ThemeTokens) {
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        SectionTitle(text = stringResource(R.string.settings_about_header), theme = theme)
        SmallGrey(text = stringResource(R.string.settings_about_version, versionName, versionCode), theme = theme)
        Spacer(Modifier.height(12.dp))

        SectionSubtitle(text = stringResource(R.string.settings_about_tagline_title), theme = theme)
        Body(text = stringResource(R.string.settings_about_tagline_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_about_what_title), theme = theme)
        Body(text = stringResource(R.string.settings_about_what_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_about_features_title), theme = theme)
        Bullet(text = stringResource(R.string.settings_about_feature_1), theme = theme)
        Bullet(text = stringResource(R.string.settings_about_feature_2), theme = theme)
        Bullet(text = stringResource(R.string.settings_about_feature_3), theme = theme)
        Bullet(text = stringResource(R.string.settings_about_feature_4), theme = theme)
        Bullet(text = stringResource(R.string.settings_about_feature_5), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_about_tech_title), theme = theme)
        Body(text = stringResource(R.string.settings_about_tech_body), theme = theme)

        Spacer(Modifier.height(12.dp))
        SectionSubtitle(text = stringResource(R.string.settings_about_contact_title), theme = theme)
        Body(text = stringResource(R.string.settings_about_contact_body), theme = theme)
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
