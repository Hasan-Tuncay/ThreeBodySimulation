package com.hsntncy.gdgeskisehir



// X ve Y koordinatlarını tutan basit sınıf
data class Point2D(
    val x: Float,
    val y: Float
)

// O anki simülasyon karesindeki (frame) 3 cismin durumu
data class ThreeBodyState(
    val body1: Point2D,
    val body2: Point2D,
    val body3: Point2D
)