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

enum class AppRoutes() {
    List,
    Detail,
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
                onNavigateToDetails = { mediaItemId->
                    navController.navigate(
                        MediaDetailRoute(
                            mediaItemId = mediaItemId,
                        )
                    )
                }
            )
        }

        composable<MediaDetailRoute> { backStackEntry ->
            val mediaItemRoute = backStackEntry.toRoute<MediaDetailRoute>()
            DetailScreen(
                onNavigateBack = {
                    navController.navigate(AppRoutes.List.name)
                },
                mediaItemId = mediaItemRoute.mediaItemId,
            )
        }
    }
}
