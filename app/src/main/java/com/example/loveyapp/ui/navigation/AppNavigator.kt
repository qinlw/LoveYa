package com.example.loveyapp.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.loveyapp.R
import com.example.loveyapp.data.service.UserManager
import com.example.loveyapp.di.AuthServiceEntryPoint
import com.example.loveyapp.di.UserManagerEntryPoint
import com.example.loveyapp.ui.screen.DataBookDetailScreen
import com.example.loveyapp.ui.screen.DataBookEditScreen
import com.example.loveyapp.ui.screen.DataBookListScreen
import com.example.loveyapp.ui.screen.DiaryDetailScreen
import com.example.loveyapp.ui.screen.DiaryEditScreen
import com.example.loveyapp.ui.screen.DiaryListScreen
import com.example.loveyapp.ui.screen.HomeScreen
import com.example.loveyapp.ui.screen.LoginScreen
import com.example.loveyapp.ui.screen.RegisterScreen
import com.example.loveyapp.ui.screen.SettingsScreen
import com.example.loveyapp.ui.screen.UserListScreen
import com.example.loveyapp.ui.component.BottomNavBar
import com.example.loveyapp.ui.component.AddAnniversaryDialog
import com.example.loveyapp.ui.viewmodel.AnniversaryViewModel
import com.example.loveyapp.security.AuthService
import dagger.hilt.android.EntryPointAccessors
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppNavigator(
    context: Context,
    onSelectStoragePath: () -> Unit = {},
    onStoragePathChanged: () -> Unit = {},
    onExportData: ((Boolean) -> Unit) -> Unit = {},
    onImportData: ((Boolean) -> Unit) -> Unit = {},
    onDataReload: () -> Unit = {}
) {
    val authService = EntryPointAccessors.fromApplication(
        context,
        AuthServiceEntryPoint::class.java
    ).authService()

    val userManager = EntryPointAccessors.fromApplication(
        context,
        UserManagerEntryPoint::class.java
    ).userManager()

    val navController = rememberNavController()
    val isLoggedIn = authService.isLoggedIn

    NavHost(navController = navController, startDestination = if (isLoggedIn) NavRoutes.Home.route else NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(NavRoutes.Home.route) { popUpTo(NavRoutes.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(NavRoutes.Register.route) },
                onNavigateToUserList = { navController.navigate(NavRoutes.UserList.route) }
            )
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) { popUpTo(NavRoutes.Register.route) { inclusive = true } } }
            )
        }

        composable(NavRoutes.UserList.route) {
            UserListScreen(
                userManager = userManager,
                onUserSelected = { navController.navigate(NavRoutes.Home.route) { popUpTo(NavRoutes.UserList.route) { inclusive = true } } },
                navController = navController
            )
        }

        composable(NavRoutes.Home.route) {
            HomeScreenWithFab(navController = navController)
        }

        composable(NavRoutes.DiaryList.route) {
            DiaryListScreenWithFab(navController = navController)
        }

        composable(NavRoutes.DiaryDetail.route) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getString("diaryId")?.toLongOrNull()
            DiaryDetailScreen(
                diaryId = diaryId,
                onNavigateToEdit = { navController.navigate(NavRoutes.DiaryEdit.createRoute(diaryId)) },
                onNavigateBack = { navController.popBackStack() },
                onDeleteSuccess = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.DiaryEdit.route) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getString("diaryId")?.takeIf { it != "new" }?.toLongOrNull()
            DiaryEditScreen(
                diaryId = diaryId,
                onSaveSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.DataBookList.route) {
            Scaffold(
                bottomBar = { BottomNavBar(navController = navController, currentRoute = NavRoutes.DataBookList.route) },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(NavRoutes.DataBookEdit.createRoute(null)) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "添加数据手册"
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.End
            ) { padding ->
                DataBookListScreen(
                    onNavigateToDetail = { dataBookId -> navController.navigate(NavRoutes.DataBookDetail.createRoute(dataBookId)) },
                    onNavigateToEdit = { dataBookId ->
                        navController.navigate(NavRoutes.DataBookEdit.createRoute(dataBookId))
                    },
                    navController = navController,
                    padding = padding
                )
            }
        }

        composable(NavRoutes.DataBookDetail.route) { backStackEntry ->
            val dataBookId = backStackEntry.arguments?.getString("dataBookId")?.toLongOrNull()
            if (dataBookId != null) {
                DataBookDetailScreen(
                    dataBookId = dataBookId,
                    onNavigateToEdit = { navController.navigate(NavRoutes.DataBookEdit.createRoute(dataBookId)) },
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }

        composable(NavRoutes.DataBookEdit.route) { backStackEntry ->
            val dataBookId = backStackEntry.arguments?.getString("dataBookId")?.takeIf { it != "new" }?.toLongOrNull()
            DataBookEditScreen(
                dataBookId = dataBookId,
                onSaveSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            Scaffold(
                bottomBar = { BottomNavBar(navController = navController, currentRoute = NavRoutes.Settings.route) }
            ) { padding ->
                SettingsScreen(
                        onLogout = {
                            authService.logout()
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(NavRoutes.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateToUserList = {
                            authService.logout()
                            navController.navigate(NavRoutes.UserList.route)
                        },
                        navController = navController,
                        onExportData = onExportData,
                        onImportData = onImportData,
                        onSelectStoragePath = onSelectStoragePath,
                        onDataReload = onDataReload,
                        padding = padding
                    )
            }
        }
    }
}

@Composable
private fun HomeScreenWithFab(navController: NavHostController) {
    var showAddAnniversary by remember { mutableStateOf(false) }
    val viewModel: AnniversaryViewModel = hiltViewModel()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, currentRoute = NavRoutes.Home.route) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAnniversary = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "添加纪念日"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        HomeScreen(
            onNavigateToAddAnniversary = {},
            navController = navController,
            padding = padding
        )
    }

    if (showAddAnniversary) {
        AddAnniversaryDialog(
            onDismiss = { showAddAnniversary = false },
            onSave = { name, date, calendarType ->
                viewModel.addAnniversary(name, date, calendarType) {
                    showAddAnniversary = false
                }
            }
        )
    }
}

@Composable
private fun DiaryListScreenWithFab(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController, currentRoute = NavRoutes.DiaryList.route) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(NavRoutes.DiaryEdit.createRoute(null)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "添加日记"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        DiaryListScreen(
            onNavigateToDetail = { diaryId -> navController.navigate(NavRoutes.DiaryDetail.createRoute(diaryId)) },
            onNavigateToEdit = {},
            navController = navController,
            padding = padding
        )
    }
}