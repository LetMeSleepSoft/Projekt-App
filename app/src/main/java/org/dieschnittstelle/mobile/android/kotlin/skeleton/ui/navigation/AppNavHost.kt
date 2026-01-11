package org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItem
import org.dieschnittstelle.mobile.android.kotlin.skeleton.model.MediaItemDetails
import org.dieschnittstelle.mobile.android.kotlin.skeleton.remote.MediaItemDTO
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.detail.DetailScreen
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.list.ListScreen
import org.dieschnittstelle.mobile.android.kotlin.skeleton.ui.map.MapScreen

enum class AppRoutes() {
    List,
    Map,
}

@Serializable
data class MediaDetailRoute(val mediaItemId: String)

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.List.name,
        modifier = modifier
    ) {
        composable(route = AppRoutes.List.name) {
            ListScreen(
                onNavigateToDetails = { mediaItemId ->
                    navController.navigate(
                        MediaDetailRoute(
                            mediaItemId = mediaItemId,
                        )
                    )
                },
                onNavigateToMap = {
                    navController.navigate(
                        route = AppRoutes.Map.name
                    )
                }
            )
        }

        composable<MediaDetailRoute> { backStackEntry ->
            val mediaItemRoute = backStackEntry.toRoute<MediaDetailRoute>()
            DetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                mediaItemId = mediaItemRoute.mediaItemId,
            )
        }

        composable(route = AppRoutes.Map.name) {
            MapScreen(
                onNavigateToList = {
                    navController.navigate(
                        route = AppRoutes.List.name
                    )
                },
                onNavigateToDetails = { mediaItemId ->
                    navController.navigate(
                        MediaDetailRoute(
                            mediaItemId = mediaItemId.toString(),
                        )
                    )
                },
            )
        }
    }
}
