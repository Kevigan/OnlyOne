package com.example.onlyone.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MainView() {
    val systemUiController = rememberSystemUiController()
    val navBarColor = MaterialTheme.colors.primary

    SideEffect {
        systemUiController.setNavigationBarColor(
            color = navBarColor,
            darkIcons = true
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = "Hello user",
            fontSize = 24.sp,
            style = MaterialTheme.typography.h5
        )
    }
}


