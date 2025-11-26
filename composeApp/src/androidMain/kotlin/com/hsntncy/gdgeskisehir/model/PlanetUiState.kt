package com.hsntncy.threebodysimulation.model

import androidx.compose.ui.graphics.Color
import com.hsntncy.threebodysimulation.Point2D

data class PlanetUiState(
    val currentPos: Point2D,
    val color: Color,
    val trail: List<Point2D> = emptyList() // Kuyruk izi
)
