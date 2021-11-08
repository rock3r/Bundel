package dev.sebastiano.bundel.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import dev.sebastiano.bundel.MainScreenWithBottomNav
import dev.sebastiano.bundel.R
import dev.sebastiano.bundel.onboarding.OnboardingScreen
import dev.sebastiano.bundel.preferences.AppsListScreen
import dev.sebastiano.bundel.preferences.Preferences
import dev.sebastiano.bundel.preferences.PreferencesScreen
import dev.sebastiano.bundel.preferences.SelectDaysDialog
import dev.sebastiano.bundel.preferences.SelectTimeRangesDialog
import dev.sebastiano.bundel.storage.DataRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalAnimationApi::class,
    FlowPreview::class
)
internal fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    onUrlClick: (String) -> Unit
) {
    navigation(
        route = NavigationRoute.SettingsGraph.route,
        startDestination = NavigationRoute.SettingsGraph.SettingsScreen.route
    ) {
        composable(
            route = NavigationRoute.SettingsGraph.SettingsScreen.route
        ) {
            PreferencesScreen(
                onSelectAppsClicked = { navController.navigate(NavigationRoute.SettingsGraph.SelectApps.route) },
                onSelectDaysClicked = { navController.navigate(NavigationRoute.SettingsGraph.SelectDays.route) },
                onSelectTimeRangesClicked = { navController.navigate(NavigationRoute.SettingsGraph.SelectTimeRanges.route) },
                onLicensesLinkClick = { navController.navigate(NavigationRoute.SettingsGraph.Licenses.route) },
                onSourcesLinkClick = { onUrlClick("https://github.com/rock3r/bundel") },
                onBackPress = { navController.popBackStack() },
            )
        }

        composable(
            route = NavigationRoute.SettingsGraph.SelectApps.route
        ) {
            AppsListScreen(onBackPress = { navController.popBackStack() })
        }

        composable(
            route = NavigationRoute.SettingsGraph.Licenses.route
        ) {
            LicensesScreen(onBackPress = { navController.popBackStack() })
        }

        bottomSheet(route = NavigationRoute.SettingsGraph.SelectDays.route) {
            SelectDaysDialog(onDialogDismiss = { navController.popBackStack() })
        }

        dialog(
            route = NavigationRoute.SettingsGraph.SelectTimeRanges.route,
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            SelectTimeRangesDialog(onDialogDismiss = { navController.popBackStack() })
        }
    }
}

@ExperimentalAnimationApi
internal fun NavGraphBuilder.mainScreenGraph(
    navController: NavHostController,
    lifecycle: Lifecycle,
    repository: DataRepository,
    preferences: Preferences
) {
    navigation(
        route = NavigationRoute.MainScreenGraph.route,
        startDestination = NavigationRoute.MainScreenGraph.MainScreen.route,
    ) {
        composable(route = NavigationRoute.MainScreenGraph.MainScreen.route) {
            MainScreenWithBottomNav(lifecycle, repository, preferences) { navController.navigate(NavigationRoute.SettingsGraph.route) }
        }
    }
}

@ExperimentalAnimationApi
internal fun NavGraphBuilder.onboardingGraph(
    navController: NavHostController,
    needsNotificationsPermissionFlow: Flow<Boolean>,
    preferences: Preferences,
    onOpenSettingsClick: () -> Unit
) {
    navigation(
        route = NavigationRoute.OnboardingGraph.route,
        startDestination = NavigationRoute.OnboardingGraph.OnboardingScreen.route,
    ) {
        composable(NavigationRoute.OnboardingGraph.OnboardingScreen.route) {
            val needsNotificationsPermission by needsNotificationsPermissionFlow.collectAsState(true)
            val scope = rememberCoroutineScope()

            OnboardingScreen(
                viewModel = hiltViewModel(),
                needsPermission = needsNotificationsPermission,
                onSettingsIntentClick = onOpenSettingsClick,
                onOnboardingDoneClicked = {
                    scope.launch { preferences.setIsOnboardingSeen(true) }
                    navController.navigate(
                        route = NavigationRoute.MainScreenGraph.MainScreen.route,
                    ) {
                        popUpTo(NavigationRoute.OnboardingGraph.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

private const val SPAGHETTI_CODE: Long = 250

@ExperimentalAnimationApi
internal fun NavGraphBuilder.splashScreenGraph(
    preferences: Preferences,
    onPizzaReady: (NavigationRoute) -> Unit
) {
    composable(NavigationRoute.SplashoScreenButWithAWeirdNameNotToTriggerLint.route) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberVectorPainter(ImageVector.vectorResource(id = R.drawable.ic_bundel_launcher_foreground)),
                contentDescription = "This is not a sandwich but a slice of pizza."
            )
        }

        LaunchedEffect(key1 = Unit) {
            val startDestination = if (preferences.isOnboardingSeen()) {
                NavigationRoute.MainScreenGraph
            } else {
                NavigationRoute.OnboardingGraph
            }

            delay(SPAGHETTI_CODE)

            onPizzaReady(startDestination)
        }
    }
}
