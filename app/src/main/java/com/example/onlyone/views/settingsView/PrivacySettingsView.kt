// PrivacySettingsView.kt  (content-only page)
package com.example.onlyone.views.settingsView

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
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens

@Composable
fun PrivacySettingsView(theme: ThemeTokens) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
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
