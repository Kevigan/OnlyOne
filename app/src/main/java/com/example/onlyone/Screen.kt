package com.example.onlyone

sealed class Screen(val route: String, val title: String){
    object MainScreen : Screen("main_screen", "Main")
}