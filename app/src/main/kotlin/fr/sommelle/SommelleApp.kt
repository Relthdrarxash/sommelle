package fr.sommelle

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.sommelle.core.data.SommelleRepository
import fr.sommelle.core.designsystem.SommelleIcons
import fr.sommelle.feature.diary.DiaryScreen
import fr.sommelle.feature.home.HomeScreen
import fr.sommelle.feature.home.WakeTimeScreen
import fr.sommelle.feature.night.NightScreen
import fr.sommelle.feature.onboarding.OnboardingScreen
import fr.sommelle.feature.week.WeekScreen
import kotlinx.coroutines.flow.first
import java.time.LocalDate

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val WEEK = "week"
    const val NIGHT = "night"
    const val WAKE_TIME = "wake-time"
    const val DIARY = "diary/{nightOf}"
    fun diary(nightOf: LocalDate) = "diary/$nightOf"
}

@Composable
fun SommelleApp(repository: SommelleRepository) {
    // null tant que l'on ne sait pas encore si le questionnaire a été rempli.
    val start by produceState<String?>(initialValue = null) {
        value = if (repository.snapshot.first() == null) Routes.ONBOARDING else Routes.HOME
    }
    val startRoute = start ?: return
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = route == Routes.HOME || route == Routes.WEEK

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { if (showBar) BottomBar(navController, route) },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = remember { startRoute },
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(repository, onDone = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    repository = repository,
                    onFillNight = { navController.navigate(Routes.diary(it)) },
                    onChooseWakeTime = { navController.navigate(Routes.WAKE_TIME) },
                    onCantSleep = { navController.navigate(Routes.NIGHT) },
                )
            }
            composable(Routes.WEEK) { WeekScreen(repository) }
            composable(Routes.WAKE_TIME) { WakeTimeScreen(repository, onDone = { navController.popBackStack() }) }
            composable(Routes.NIGHT) { NightScreen(onClose = { navController.popBackStack() }) }
            composable(
                route = Routes.DIARY,
                arguments = listOf(navArgument("nightOf") { type = NavType.StringType }),
            ) { entry ->
                val nightOf = LocalDate.parse(checkNotNull(entry.arguments?.getString("nightOf")))
                DiaryScreen(repository, nightOf, onClose = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, route: String?) {
    val label = stringResource(R.string.nav_label)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        listOf(
            Triple(Routes.HOME, R.string.nav_home, Icons.Filled.Home),
            Triple(Routes.WEEK, R.string.nav_week, SommelleIcons.Chart),
        ).forEach { (target, text, icon) ->
            NavigationBarItem(
                selected = route == target,
                onClick = {
                    navController.navigate(target) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(stringResource(text), style = MaterialTheme.typography.labelMedium) },
                alwaysShowLabel = true,
                colors = colors,
            )
        }
    }
}
