package com.example.onlyone.views.settingsView

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.onlyone.R
import com.example.onlyone.theme.ThemeTokens
import com.example.onlyone.viewModels.userViewModel.UserViewModel

@Composable
fun ProfileSettingsView(
    userViewModel: UserViewModel,
    theme: ThemeTokens
) {
    val context = LocalContext.current
    val user by userViewModel.user.observeAsState()

    // Local editable state
    var username by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }     // keep as string for input control
    var city by remember { mutableStateOf("") }

    // ⚠️ Use API values internally, display pretty labels in the dropdown
    val genderOptions: List<Pair<String, String>> = listOf(
        "Male" to "male",
        "Female" to "female",
        "Non-binary" to "nonbinary",
        "Other" to "other",
        "Unspecified" to "unspecified"
    )
    var genderValue by remember { mutableStateOf("unspecified") } // store API value

    var genderMenuExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Prefill when user content changes
    LaunchedEffect(user) {
        if (!isSaving) {
            username = user?.username.orEmpty()
            // Show only valid (18..100), otherwise blank
            ageInput = user?.age?.takeIf { it in 18..100 }?.toString() ?: ""
            city = user?.city.orEmpty()

            // Normalize to API value; fallback to "unspecified"
            val current = (user?.gender ?: "unspecified").lowercase()
            genderValue = genderOptions.map { it.second }.firstOrNull { it == current } ?: "unspecified"
        }
    }

    // --- Validation helpers ---
    fun sanitizeAgeInput(raw: String): String {
        val digits = raw.filter { it.isDigit() }.take(3)  // allow up to 3 digits
        if (digits.isEmpty()) return ""
        // Clamp upper bound only if user types > 100
        val n = digits.toInt()
        return if (n > 100) "100" else digits
    }
    val ageValid = ageInput.toIntOrNull()?.let { it in 18..100 } == true
    val formValid = username.isNotBlank() && city.isNotBlank() && ageValid

    // Reusable colors with error overrides (so border truly turns red)
    @Composable
    fun tfColors() = TextFieldDefaults.outlinedTextFieldColors(
        textColor = theme.textColor,
        cursorColor = theme.textColor,
        focusedBorderColor = theme.textColor,
        unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
        disabledTextColor = theme.textColor,
        errorBorderColor = MaterialTheme.colors.error,
        errorCursorColor = MaterialTheme.colors.error,
        errorLabelColor = MaterialTheme.colors.error
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        // Title
        Text(
            text = stringResource(R.string.settings_section_profile),
            style = MaterialTheme.typography.h6,
            color = theme.textColor
        )
        Spacer(Modifier.height(12.dp))

        // Username
        Text(
            text = stringResource(R.string.onboarding_username_label),
            style = MaterialTheme.typography.subtitle1,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it.take(32) }, // soft cap
            modifier = Modifier.fillMaxWidth(),
            colors = tfColors()
        )

        Spacer(Modifier.height(12.dp))

        // Age label + "min. 18"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.onboarding_age_label),
                style = MaterialTheme.typography.subtitle1,
                color = theme.textColor
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "min. 18",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = ageInput,
            onValueChange = { raw ->
                ageInput = if (raw.isBlank()) "" else sanitizeAgeInput(raw)
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = ageInput.isNotBlank() && !ageValid,
            colors = tfColors(),
            placeholder = { Text("18–100", color = theme.textColor.copy(alpha = 0.6f)) }
        )
        if (ageInput.isNotBlank() && !ageValid) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_form_invalid), // or a dedicated "Age must be 18–100"
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.error
            )
        }

        Spacer(Modifier.height(12.dp))

        // City (≤ 50 chars)
        Text(
            text = stringResource(R.string.onboarding_city_label),
            style = MaterialTheme.typography.subtitle1,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it.take(50) },
            modifier = Modifier.fillMaxWidth(),
            colors = tfColors(),
            placeholder = {
                Text(
                    text = stringResource(R.string.onboarding_city_placeholder),
                    color = theme.textColor.copy(alpha = 0.6f)
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        // Gender (Dropdown) — stores API value, shows label
        Text(
            text = stringResource(R.string.onboarding_gender_label),
            style = MaterialTheme.typography.subtitle1,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))

        Box {
            val currentLabel = genderOptions.firstOrNull { it.second == genderValue }?.first ?: "Unspecified"
            OutlinedTextField(
                value = currentLabel,
                onValueChange = { /* read-only via dropdown */ },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { genderMenuExpanded = true },
                colors = tfColors(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.onboarding_gender_placeholder),
                        color = theme.textColor.copy(alpha = 0.6f)
                    )
                }
            )
            DropdownMenu(
                expanded = genderMenuExpanded,
                onDismissRequest = { genderMenuExpanded = false }
            ) {
                genderOptions.forEach { (label, value) ->
                    DropdownMenuItem(onClick = {
                        genderValue = value  // store API value
                        genderMenuExpanded = false
                    }) {
                        Text(label)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Save button
        Button(
            onClick = {
                if (!formValid) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.onboarding_form_invalid),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                val updates = mutableMapOf<String, Any>(
                    "username" to username,
                    "city" to city,
                    "gender" to genderValue // send API value (lowercase)
                )
                ageInput.toIntOrNull()?.let { updates["age"] = it }

                isSaving = true
                userViewModel.updatePublicProfile(
                    updates = updates,
                    onSuccess = {
                        isSaving = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.profile_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = {
                        isSaving = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.onboarding_form_invalid),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            enabled = !isSaving && formValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = theme.textColor
                )
            } else {
                Text(text = stringResource(R.string.profile_save), color = theme.textColor)
            }
        }
    }
}
