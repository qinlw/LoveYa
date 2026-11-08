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
    object DiaryEdit : NavRoutes("diaryEdit/{diaryId}?date={date}") {
        /** date 仅在新建日记时使用（yyyy-MM-dd），用于从日历带入指定日期 */
        fun createRoute(diaryId: Long?, date: String? = null): String {
            val base = "diaryEdit/${diaryId ?: "new"}"
            return if (date != null) "$base?date=$date" else base
        }
    }
    object DataBookList : NavRoutes("dataBookList")
    object Calendar : NavRoutes("calendar")
    object Moods : NavRoutes("moods")
    object DataBookDetail : NavRoutes("dataBookDetail/{dataBookId}") {
        fun createRoute(dataBookId: Long) = "dataBookDetail/$dataBookId"
    }
    object DataBookEdit : NavRoutes("dataBookEdit/{dataBookId}") {
        fun createRoute(dataBookId: Long?) = "dataBookEdit/${dataBookId ?: "new"}"
    }
    object Profile : NavRoutes("profile")
    object Settings : NavRoutes("settings")
    object CloudBackup : NavRoutes("cloud_backup")
    object EditAccount : NavRoutes("editAccount")
    object Developer : NavRoutes("developer")
}
