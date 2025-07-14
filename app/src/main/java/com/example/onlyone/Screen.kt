package com.example.onlyone

sealed class Screen(val route: String, val title: String){
    object MainScreen : Screen("main_screen", "Main")
    object FriendsScreen : Screen("friends_screen", "Friends")
    object ShopScreen : Screen("shop_screen", "Shop")
    object SettingsScreen : Screen("settings_screen", "Settings")
}