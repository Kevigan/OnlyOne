package com.example.onlyone.views.settingsView

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
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
    var gender by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Prefill when user content changes
    LaunchedEffect(user) {
        if (!isSaving) {
            username = user?.username.orEmpty()
            ageInput = user?.age?.takeIf { it in 1..99 }?.toString() ?: ""
            city = user?.city.orEmpty()
            gender = user?.gender.orEmpty()
        }
    }

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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = theme.textColor,
                cursorColor = theme.textColor,
                focusedBorderColor = theme.textColor,
                unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Age (1..99)
        Text(
            text = stringResource(R.string.onboarding_age_label),
            style = MaterialTheme.typography.subtitle1,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = ageInput,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(2)
                ageInput = digits
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = theme.textColor,
                cursorColor = theme.textColor,
                focusedBorderColor = theme.textColor,
                unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
            ),
            placeholder = { Text("1–99", color = theme.textColor.copy(alpha = 0.6f)) }
        )

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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = theme.textColor,
                cursorColor = theme.textColor,
                focusedBorderColor = theme.textColor,
                unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f)
            ),
            placeholder = {
                Text(
                    text = stringResource(R.string.onboarding_city_placeholder),
                    color = theme.textColor.copy(alpha = 0.6f)
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        // Gender (Dropdown)
        Text(
            text = stringResource(R.string.onboarding_gender_label),
            style = MaterialTheme.typography.subtitle1,
            color = theme.textColor
        )
        Spacer(Modifier.height(6.dp))

        var genderMenuExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Other")

        Box {
            OutlinedTextField(
                value = gender,
                onValueChange = { /* read-only via dropdown */ },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { genderMenuExpanded = true },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = theme.textColor,
                    cursorColor = theme.textColor,
                    focusedBorderColor = theme.textColor,
                    unfocusedBorderColor = theme.textColor.copy(alpha = 0.6f),
                    disabledTextColor = theme.textColor
                ),
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
                genderOptions.forEach { option ->
                    DropdownMenuItem(onClick = {
                        gender = option
                        genderMenuExpanded = false
                    }) {
                        Text(option)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Save button
        Button(
            onClick = {
                val ageOrNull = ageInput.toIntOrNull()?.coerceIn(1, 99)
                val updates = mutableMapOf<String, Any>(
                    "username" to username,
                    "city" to city
                )
                if (ageOrNull != null) updates["age"] = ageOrNull
                if (gender.isNotBlank()) updates["gender"] = gender

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
            enabled = !isSaving,
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
