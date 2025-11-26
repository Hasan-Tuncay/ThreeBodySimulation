package com.hsntncy.threebodysimulation.model

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.random.Random

@Composable
fun SpaceCanvas(
    planets: List<PlanetUiState>,
    modifier: Modifier = Modifier,
    onDrag: (Int, Float, Float) -> Unit
) {
    val stars = remember {
        List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                alpha = Random.nextFloat() * 0.8f + 0.2f
            )
        }
    }

    var draggedPlanetIndex by remember { mutableStateOf<Int?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1117), Color.Black),
                    radius = 1800f
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val scale = 120f

                        // 1. Çizimdeki boyutlarla BİREBİR AYNI olmalı (Hata buradaydı)
                        fun getVisualRadius(index: Int): Float = when(index) {
                            0 -> 40f // Güneş
                            1 -> 25f // Dünya
                            else -> 15f // Ay
                        }

                        // Parmak payı
                        val touchPadding = 50f

                        val foundIndex = planets
                            .mapIndexed { index, planet ->
                                val screenX = centerX + (planet.currentPos.x * scale)
                                val screenY = centerY - (planet.currentPos.y * scale)

                                val dx = offset.x - screenX
                                val dy = offset.y - screenY
                                // Parmağın merkeze uzaklığı
                                val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                                // Bu gezegenin tıklanabilir alanı
                                val hitRadius = getVisualRadius(index) + touchPadding

                                // (Index, Mesafe, HitRadius)
                                Triple(index, distance, hitRadius)
                            }
                            // Sadece sınırları içine tıklananları al
                            .filter { it.second <= it.third }

                            // --- KESİN ÇÖZÜM ---
                            // Hiçbir karmaşık mantık yok.
                            // Parmağıma mesafesi (distance) EN AZ olan hangisiyse onu getir.
                            .minByOrNull { it.second }
                            ?.first

                        draggedPlanetIndex = foundIndex
                    },
                    onDrag = { change, _ ->
                        draggedPlanetIndex?.let { index ->
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val scale = 120f

                            // Parmağın yeni konumu
                            val newPhysX = (change.position.x - centerX) / scale
                            val newPhysY = (centerY - change.position.y) / scale

                            onDrag(index, newPhysX, newPhysY)
                        }
                    },
                    onDragEnd = { draggedPlanetIndex = null },
                    onDragCancel = { draggedPlanetIndex = null }
                )
            }
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val scale = 120f

        // Yıldızlar
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(star.x * size.width, star.y * size.height)
            )
        }

        // Gezegenler
        planets.forEachIndexed { index, planet ->

            // Çizim boyutları (Yukarıdaki ile aynı)
            val visualRadius = when(index) {
                0 -> 40f
                1 -> 25f
                else -> 15f
            }

            // Kuyruk İzleri
            if (planet.trail.isNotEmpty()) {
                val path = Path()
                val startX = centerX + (planet.trail.first().x * scale)
                val startY = centerY - (planet.trail.first().y * scale)
                path.moveTo(startX, startY)

                planet.trail.drop(1).forEach { point ->
                    val screenX = centerX + (point.x * scale)
                    val screenY = centerY - (point.y * scale)
                    path.lineTo(screenX, screenY)
                }

                drawPath(
                    path = path,
                    color = planet.color.copy(alpha = 0.4f),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }

            // Gezegen Gövdesi
            val currentScreenX = centerX + (planet.currentPos.x * scale)
            val currentScreenY = centerY - (planet.currentPos.y * scale)

            drawCircle(
                color = planet.color.copy(alpha = 0.2f),
                radius = visualRadius * 2.5f,
                center = Offset(currentScreenX, currentScreenY)
            )

            drawCircle(
                color = planet.color,
                radius = visualRadius,
                center = Offset(currentScreenX, currentScreenY)
            )
        }
    }
}

//.pointerInput(planets) {
//    awaitEachGesture {
//        // 1) Parmağın ilk bastığı an:
//        val down = awaitFirstDown()
//
//        val centerX = size.width / 2f
//        val centerY = size.height / 2f
//        val scale = 120f
//
//        fun getVisualRadius(index: Int): Float = when (index) {
//            0 -> 40f // Güneş
//            1 -> 25f // Dünya
//            else -> 15f // Diğerleri
//        }
//
//        val touchPadding = 35f
//
//        // 2) En yakın gezegeni seç (tap anında)
//        val startOffset = down.position
//
//        val foundIndex = planets
//            .mapIndexed { index, planet ->
//                val screenX = centerX + (planet.currentPos.x * scale)
//                val screenY = centerY - (planet.currentPos.y * scale)
//
//                val dx = startOffset.x - screenX
//                val dy = startOffset.y - screenY
//                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
//
//                val hitRadius = getVisualRadius(index) + touchPadding
//                Triple(index, distance, hitRadius)
//            }
//            .filter { it.second <= it.third }
//            .minByOrNull { it.second }
//            ?.first
//
//        draggedPlanetIndex = foundIndex
//
//        if (foundIndex == null) {
//            // Hiçbir gezegenin hit alanına basılmamış, bu gesture'ı bitir
//            return@awaitEachGesture
//        }
//
//        // 3) Parmağın hareketlerini takip et ve onDrag çağır
//        var pointer = down
//
//        while (true) {
//            val event = awaitPointerEvent()
//            val change = event.changes.firstOrNull { it.id == pointer.id } ?: break
//
//            if (!change.pressed) {
//                // Parmağı kaldırdı, drag bitti
//                break
//            }
//
//            val newPhysX = (change.position.x - centerX) / scale
//            val newPhysY = (centerY - change.position.y) / scale
//
//            onDrag(foundIndex, newPhysX, newPhysY)
//
//            change.consume()
//            pointer = change
//        }
//
//        draggedPlanetIndex = null
//    }
//}