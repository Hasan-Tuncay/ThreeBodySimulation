package com.hsntncy.threebodysimulation.ui

// androidApp/src/main/java/com/hsntncy/threebodysimulation/ui/ThreeBodyScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsntncy.threebodysimulation.ThreeBodyViewModel
import com.hsntncy.threebodysimulation.model.SimMode
import com.hsntncy.threebodysimulation.model.SpaceCanvas


@Composable
fun ThreeBodyScreen() {
    val viewModel: ThreeBodyViewModel = viewModel()
    val planets by viewModel.planets.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState() // Seçili modu dinle

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Simülasyon
        SpaceCanvas(
            planets = planets,
            modifier = Modifier.fillMaxSize(),
            onDrag = { index, x, y ->
                // ViewModel üzerinden C++'a gönder
                viewModel.onPlanetDragged(index, x, y)
            }
        )

        // 2. Kontrol Paneli (Column içine alalım)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 50.dp, start = 16.dp, end = 16.dp)
        ) {

            // SENARYO BUTONLARI
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimMode.entries.forEach { mode ->
                    val isSelected = currentMode == mode
                    Button(
                        onClick = { viewModel.loadScenario(mode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFFFFD600) else Color.Transparent,
                            contentColor = if (isSelected) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = mode.title)
                    }
                }
            }

            // BAŞLAT / DURDUR BUTONU
            Button(
                onClick = { viewModel.toggleSimulation() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF).copy(alpha = 0.8f),
                    contentColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "  Start / Stop")
            }
        }
    }
}