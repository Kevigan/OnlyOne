package com.onlyone.app.views.chainMessage

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlyone.app.R
import com.onlyone.app.data.chainMessage.Chain
import com.onlyone.app.data.chainMessage.ChainStep
import com.onlyone.app.theme.ThemeTokens
import com.onlyone.app.viewModels.userViewModel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ChainDetailScreen(
    theme: ThemeTokens,
    userViewModel: UserViewModel,
    navController: NavController,
    chainId: String
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1) Collect chain + steps
    val pair by userViewModel
        .chainManager
        .observeChainAndSteps(chainId)
        .collectAsState(initial = null)

    // derive current user id (adjust to your user model)
    val myUid = remember(userViewModel) { userViewModel.user.value?.uid ?: "" }
    val me = userViewModel.user.value
    val authorNameForSteps =
        me?.username?.takeIf { it.isNotBlank() }
            ?: me?.username?.takeIf { it.isNotBlank() }
            ?: me?.uid
            ?: ""

    // Loading / missing guards
    if (pair == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = theme.textColor)
        }
        return
    }

    val chain: Chain = pair!!.first
    val steps: List<ChainStep> = pair!!.second

    // 2) Hook up the view with callbacks
    ChainDetailView(
        theme = theme,
        userViewModel = userViewModel,
        chain = chain,
        steps = steps,
        myUid = myUid,
        onSend = { text, nextAssigneeOrNull ->
            if (text.isBlank()) return@ChainDetailView
            userViewModel.chainManager.contributeAndPass(
                chainId = chain.id,
                text = text,
                nextAssigneeUid = nextAssigneeOrNull,
                authorName = authorNameForSteps,
                onSuccess = {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.chain_toast_step_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { e ->
                    Toast.makeText(
                        ctx,
                        e.message ?: ctx.getString(R.string.chain_toast_step_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        },
        onReroute = { newAssigneeUid ->
            userViewModel.chainManager.rerouteAssignee(
                chainId = chain.id,
                newAssigneeUid = newAssigneeUid,
                onSuccess = {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.chain_toast_rerouted),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { e ->
                    Toast.makeText(
                        ctx,
                        e.message ?: ctx.getString(R.string.chain_toast_reroute_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        },
        onSaveLocal = {
            userViewModel.chainManager.saveChainLocally(
                chain = chain,
                steps = steps,
                onSuccess = {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.chain_toast_saved_local),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onError = { e ->
                    Toast.makeText(
                        ctx,
                        e.message ?: ctx.getString(R.string.chain_toast_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    )

}
