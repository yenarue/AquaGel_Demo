package com.example.aquagel

sealed class Routes(val route: String) {
    data object Root : Routes("root")
    data object Camera : Routes("camera")
    data object Preview : Routes("preview/{photoUri}") {
        const val ARG_PHOTO_URI = "photoUri"
        fun createRoute(photoUri: String) = "preview/$photoUri"
    }

    data object WoundForm : Routes("wound_form/{photoUri}") {
        const val ARG_PHOTO_URI = "photoUri"
        fun createRoute(photoUri: String) = "wound_form/$photoUri"
    }

    data object Recommend : Routes("recommend/{photoUri}") {
        const val ARG_PHOTO_URI = "photoUri"
        fun createRoute(photoUri: String) = "recommend/$photoUri"
    }
}
