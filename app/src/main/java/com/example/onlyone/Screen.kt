package com.example.onlyone

sealed class Screen(val route: String, val title: String){
    object MainScreen : Screen("main_screen", "Main")
    object FriendsScreen : Screen("friends_screen", "Friends")
    object ShopScreen : Screen("shop_screen", "Shop")
    object SettingsScreen : Screen("settings_screen", "Settings")
    object LoginScreen : Screen("login_screen", "Login")
    object SplashScreen : Screen("splash_screen", "Splash")

    object SetUsernameScreen : Screen(
        route = "SetUsername/{uid}/{email}?google={google}",
        title = "Set Username"
    ) {
        fun createRoute(uid: String, email: String, isGoogleUser: Boolean = false): String {
            return "SetUsername/$uid/$email?google=$isGoogleUser"
        }
    }

    object ChatScreen : Screen("ChatScreen/{uid}/{isFriend}", "Chat") {
        fun createRoute(uid: String, isFriend: Boolean): String = "ChatScreen/$uid/$isFriend"
    }

}