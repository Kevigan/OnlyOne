package com.example.onlyone

sealed class Screen(val route: String, val title: String){
    object MainScreen : Screen("main_screen", "Main")
    object FriendsScreen : Screen("friends_screen", "Friends")
    object ShopScreen : Screen("shop_screen", "Shop")
    object SettingsScreen : Screen("settings_screen", "Settings")
    object LoginScreen : Screen("login_screen", "Login")
    object SplashScreen : Screen("splash_screen", "Splash")

    object SetUsernameScreen : Screen("SetUsername/{uid}/{email}", "Set Username") {
        fun createRoute(uid: String, email: String): String {
            return "SetUsername/$uid/$email"
        }
    }
}