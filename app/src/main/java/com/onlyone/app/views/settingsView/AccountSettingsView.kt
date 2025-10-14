@file:Suppress("UnusedImport")
package com.onlyone.app.views.settingsView

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.onlyone.app.Screen
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.SessionViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.onlyone.app.R

@Composable
fun AccountSettingsView(
    sessionViewModel: SessionViewModel,
    theme: ThemeTokens,
    navController: NavController
) {
    val context = LocalContext.current
    val user by sessionViewModel.currentUser.collectAsState()

    val isPasswordUser = remember(user) {
        user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
    }
    val email = user?.email.orEmpty()

    var showChangeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // NEW: progress flags for each dialog
    var changing by remember { mutableStateOf(false) }
    var resetting by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Text(stringResource(R.string.auth_account_title), color = theme.textColor, style = MaterialTheme.typography.h6)
        Spacer(Modifier.height(12.dp))

        // Change Password
        Button(
            onClick = {
                if (isPasswordUser) showChangeDialog = true else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.auth_toast_google_no_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = theme.buttonBackgroundColor,
                contentColor = theme.textColor
            )
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.auth_action_change_password))
        }

        Spacer(Modifier.height(8.dp))

        // Send Reset Email
        Button(
            onClick = {
                if (isPasswordUser) showResetDialog = true else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.auth_toast_google_no_reset),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = theme.buttonBackgroundColor,
                contentColor = theme.textColor
            )
        ) {
            Icon(Icons.Default.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.auth_action_send_reset_email))
        }

        Spacer(Modifier.weight(1f))

        // Delete Account (danger)
        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error,
                contentColor = MaterialTheme.colors.onError
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.settings_privacy_delete_account))
        }
    }

    if (showChangeDialog) {
        ChangePasswordDialog(
            theme = theme,
            isProcessing = changing, // <-- show spinner when true
            onDismiss = { if (!changing) showChangeDialog = false },
            onConfirm = { current, new ->
                if (changing) return@ChangePasswordDialog
                changing = true
                sessionViewModel.changePasswordWithCurrentPassword(
                    currentPassword = current,
                    newPassword = new,
                    onSuccess = {
                        changing = false
                        Toast.makeText(context, context.getString(R.string.auth_change_success), Toast.LENGTH_SHORT).show()
                        showChangeDialog = false
                    },
                    onFailure = {
                        changing = false
                        Toast.makeText(context, it.message ?: context.getString(R.string.auth_error_unknown), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            theme = theme,
            prefillEmail = email,
            isProcessing = resetting, // <-- show spinner when true
            onDismiss = { if (!resetting) showResetDialog = false },
            onConfirm = { targetEmail ->
                if (resetting) return@ResetPasswordDialog
                resetting = true
                sessionViewModel.sendPasswordReset(
                    email = targetEmail,
                    onSuccess = {
                        resetting = false
                        Toast.makeText(context, context.getString(R.string.auth_reset_sent_generic), Toast.LENGTH_SHORT).show()
                        showResetDialog = false
                    },
                    onFailure = {
                        resetting = false
                        Toast.makeText(context, it.message ?: context.getString(R.string.auth_error_unknown), Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            theme = theme,
            isPasswordUser = isPasswordUser,
            isProcessing = deleting, // <-- show spinner when true
            onDismiss = { if (!deleting) showDeleteDialog = false },
            onConfirmWithPassword = { pwd ->
                if (deleting) return@DeleteAccountDialog
                deleting = true
                sessionViewModel.reauthAndDeleteWithPassword(
                    password = pwd,
                    onSuccess = {
                        deleting = false
                        sessionViewModel.signOut()
                        Toast.makeText(
                            context,
                            context.getString(R.string.settings_delete_success),
                            Toast.LENGTH_SHORT
                        ).show()
                        showDeleteDialog = false
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onFailure = {
                        deleting = false
                        Toast.makeText(
                            context,
                            it.message ?: context.getString(R.string.auth_error_unknown),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onRequestGoogleReauth = {
                Toast.makeText(
                    context,
                    context.getString(R.string.auth_toast_google_no_password),
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: run Google reauth flow, then user.reauthenticate(googleCredential) -> user.delete()
            }
        )
    }
}
