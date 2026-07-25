package com.example.loveyapp.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.loveyapp.R
import com.example.loveyapp.ui.navigation.NavRoutes

data class NavItem(val route: NavRoutes, val label: String, val iconId: Int)

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navItems = listOf(
            NavItem(NavRoutes.Home, "首页", R.drawable.ic_home),
            NavItem(NavRoutes.DiaryList, "日记", R.drawable.ic_diary),
            NavItem(NavRoutes.DataBookList, "数据手册", R.drawable.ic_data_book),
            NavItem(NavRoutes.Settings, "设置", R.drawable.ic_settings)
        )

        navItems.forEach { item ->
            val isSelected = currentRoute == item.route.route
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconId),
                        contentDescription = item.label,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                label = { 
                    Text(
                        item.label,
                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ) 
                },
                selected = isSelected,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                onClick = {
                    navController.navigate(item.route.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
