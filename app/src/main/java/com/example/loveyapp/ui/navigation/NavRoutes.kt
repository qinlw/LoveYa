package com.example.loveyapp.ui.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object UserList : NavRoutes("userList")
    object Home : NavRoutes("home")
    object DiaryList : NavRoutes("diaryList")
    object DiaryDetail : NavRoutes("diaryDetail/{diaryId}") {
        fun createRoute(diaryId: Long) = "diaryDetail/$diaryId"
    }
    object DiaryEdit : NavRoutes("diaryEdit/{diaryId}") {
        fun createRoute(diaryId: Long?) = "diaryEdit/${diaryId ?: "new"}"
    }
    object DataBookList : NavRoutes("dataBookList")
    object DataBookDetail : NavRoutes("dataBookDetail/{dataBookId}") {
        fun createRoute(dataBookId: Long) = "dataBookDetail/$dataBookId"
    }
    object DataBookEdit : NavRoutes("dataBookEdit/{dataBookId}") {
        fun createRoute(dataBookId: Long?) = "dataBookEdit/${dataBookId ?: "new"}"
    }
    object Settings : NavRoutes("settings")
}
