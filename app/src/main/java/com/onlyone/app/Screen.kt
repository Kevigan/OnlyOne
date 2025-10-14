package com.onlyone.app

sealed class Screen(val route: String, val title: String){
    object MainScreen : Screen("main_screen", "Main")
    object FriendsScreen : Screen("friends_screen", "Friends")
    object ShopScreen : Screen("shop_screen", "Shop")
    object SettingsScreen : Screen("settings_screen", "Settings")
    object LoginScreen : Screen("login_screen", "Login")
    object SplashScreen : Screen("splash_screen", "Splash")
    object AchievementsScreen : Screen("achievements_screen", "Achievements")

    // ✅ NEW
    object FeedbackScreen : Screen("feedback_screen", "Feedback")

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

    object VerifyEmailScreen : Screen(
        route = "verify_email/{uid}/{email}",
        title = "Verify Email"
    ) {
        fun createRoute(uid: String, email: String) = "verify_email/$uid/$email"
    }

    object OnboardingScreen : Screen(
        route = "onboarding?goHome={goHome}",
        title = "Onboarding"
    ) {
        fun createRoute(goHome: Boolean = false): String = "onboarding?goHome=$goHome"
    }

    object AgeGateScreen : Screen("age_gate/{uid}/{email}/{google}", title = "Age Gate") {
        fun createRoute(uid: String, email: String, google: Boolean) =
            "age_gate/$uid/$email/$google"
    }

    object ChatFriendsScreen : Screen(
        route = "ChatFriends/{uid}",
        title = "Chat"
    ) {
        fun createRoute(uid: String) = "ChatFriends/$uid"
    }

    object CreateChainMessageScreen : Screen(
        route = "create_chain_message",
        title = "Start Chain"
    )

    object ChainDetailScreen : Screen(
        route = "chain_detail/{chainId}",
        title = "Chain"
    ) {
        fun createRoute(chainId: String) = "chain_detail/$chainId"
    }

    object ChainsScreen : Screen(
        route = "chains_screen",
        title = "Chains"
    )

    object SavedChainsScreen : Screen("saved_chains_screen", "Saved")
}
