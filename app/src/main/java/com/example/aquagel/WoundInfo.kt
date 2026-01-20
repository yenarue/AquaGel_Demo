package com.example.aquagel

import java.io.Serializable

data class WoundInfo(
    val ageRange: String,
    val gender: String?,
    val woundType: String,
    val location: String,
    val size: String,
    val timeSinceInjury: String
) : Serializable
