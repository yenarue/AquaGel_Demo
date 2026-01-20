package com.example.aquagel

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val rootEntry = remember(navController) {
        navController.getBackStackEntry(Routes.Root.route)
    }
    val sharedViewModel: WoundSharedViewModel = viewModel(rootEntry)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.Camera.route,
                route = Routes.Root.route
            ) {
                composable(Routes.Camera.route) {
                    CameraCaptureScreen(
                        onNext = { dummyUri ->
                            sharedViewModel.photoUri = dummyUri
                            val encodedUri = Uri.encode(dummyUri)
                            navController.navigate(Routes.Preview.createRoute(encodedUri))
                        }
                    )
                }
                composable(
                    route = Routes.Preview.route,
                    arguments = listOf(navArgument(Routes.Preview.ARG_PHOTO_URI) { type = NavType.StringType })
                ) { backStackEntry ->
                    val photoUri = backStackEntry.arguments?.getString(Routes.Preview.ARG_PHOTO_URI).orEmpty()
                    sharedViewModel.photoUri = photoUri
                    PhotoPreviewScreen(
                        photoUri = photoUri,
                        onNext = { uri ->
                            sharedViewModel.photoUri = uri
                            val encodedUri = Uri.encode(uri)
                            navController.navigate(Routes.WoundForm.createRoute(encodedUri))
                        }
                    )
                }
                composable(
                    route = Routes.WoundForm.route,
                    arguments = listOf(navArgument(Routes.WoundForm.ARG_PHOTO_URI) { type = NavType.StringType })
                ) { backStackEntry ->
                    val photoUri = backStackEntry.arguments?.getString(Routes.WoundForm.ARG_PHOTO_URI).orEmpty()
                    sharedViewModel.photoUri = photoUri
                    WoundInfoScreen(
                        photoUri = photoUri,
                        onSubmit = { info ->
                            sharedViewModel.woundInfo = info
                            val encodedUri = Uri.encode(photoUri)
                            navController.navigate(Routes.Recommend.createRoute(encodedUri))
                        }
                    )
                }
                composable(
                    route = Routes.Recommend.route,
                    arguments = listOf(navArgument(Routes.Recommend.ARG_PHOTO_URI) { type = NavType.StringType })
                ) { backStackEntry ->
                    val photoUri = backStackEntry.arguments?.getString(Routes.Recommend.ARG_PHOTO_URI).orEmpty()
                    TreatmentRecommendationsScreen(
                        photoUri = photoUri,
                        woundInfo = sharedViewModel.woundInfo
                    )
                }
            }
        }
    }
}
