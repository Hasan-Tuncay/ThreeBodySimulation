package com.hsntncy.gdgeskisehir

// androidApp/src/main/java/com/hsntncy/gdgeskisehir/simulation/ThreeBodyViewModel.kt

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsntncy.gdgeskisehir.ThreeBodyEngine
import com.hsntncy.gdgeskisehir.Point2D
import com.hsntncy.gdgeskisehir.model.PlanetUiState
import com.hsntncy.gdgeskisehir.model.SimMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// UI'ın çizeceği her bir gezegenin verisi

class ThreeBodyViewModel : ViewModel() {

    // Shared modüldeki motorumuz
    private val engine = ThreeBodyEngine()

    // UI State
    private val _planets = MutableStateFlow<List<PlanetUiState>>(emptyList())
    val planets = _planets.asStateFlow()

    private var isRunning = false

    // Kuyruk uzunluğu (Ne kadar geçmişi hatırlasın?)
    private val maxTrailSize = 100

    private val _currentMode = MutableStateFlow(SimMode.STABLE)
    val currentMode = _currentMode.asStateFlow()

    init {
        loadScenario(SimMode.STABLE)
    }

    fun loadScenario(mode: SimMode) {
        _currentMode.value = mode

        // Simülasyonu durdur, motoru yeniden kur, temizle
        val wasRunning = isRunning
        isRunning = false

        engine.initializeScenario(mode.modeId)

        // State'i sıfırla (Kuyrukları sil)
        updateStateFromEngine(resetTrails = true)

        // Eğer çalışıyorduysa tekrar başlat
        if (wasRunning) {
            isRunning = true
            startLoop()
        }
    }
    fun toggleSimulation() {
        if (isRunning) {
            isRunning = false
        } else {
            isRunning = true
            startLoop()
        }
    }

    private fun startLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive && isRunning) {
                // 1. Fizik motorunu ilerlet
                engine.step()

                // 2. UI verisini güncelle
                updateStateFromEngine(resetTrails = false)

                // 3. 60 FPS (yaklaşık 16ms)
                delay(16)
            }
        }
    }

    private fun updateStateFromEngine(resetTrails: Boolean) {
        val state = engine.getCurrentState()

        // Mevcut listeyi al veya yeni oluştur
        val currentList = _planets.value.ifEmpty {
            listOf(
                PlanetUiState(state.body1, Color(0xFF00E5FF)), // Neon Cyan
                PlanetUiState(state.body2, Color(0xFFFF4081)), // Neon Pink
                PlanetUiState(state.body3, Color(0xFFFFD740))  // Neon Amber
            )
        }

        // Yeni pozisyonları ve izleri hesapla
        val newPositions = listOf(state.body1, state.body2, state.body3)

        val updatedList = currentList.mapIndexed { index, planet ->
            val newPos = newPositions[index]

            // Kuyruk hesaplama
            val newTrail = if (resetTrails) emptyList() else (planet.trail + newPos).takeLast(maxTrailSize)

            planet.copy(currentPos = newPos, trail = newTrail)
        }

        _planets.value = updatedList
    }

    override fun onCleared() {
        super.onCleared()
        engine.destroy()
    }

    fun onPlanetDragged(index: Int, x: Float, y: Float) {
        // Motoru güncelle
        engine.setBodyPosition(index, x, y)

        // UI'ı anlık güncelle (titreme olmasın diye)
        val currentList = _planets.value.toMutableList()
        if (index < currentList.size) {
            // Sadece pozisyonu güncelle, kuyruğu koru veya temizle
            val updatedPlanet = currentList[index].copy(
                currentPos = Point2D(x, y),
                trail = emptyList() // Sürükleyince kuyruk silinsin ki çirkin durmasın
            )
            currentList[index] = updatedPlanet
            _planets.value = currentList
        }
    }
}