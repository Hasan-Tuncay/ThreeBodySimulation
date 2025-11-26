//
// Created by hasan tuncay on 25.11.2025.
//


import Foundation
import SwiftUI
import Shared

@MainActor // UI güncellemeleri için ana thread garantisi
class ThreeBodyViewModel: ObservableObject {

    // KMP Motoru
    private let engine = ThreeBodyEngine()

    // UI State (Published: Değişince ekranı günceller)
    @Published var planets: [PlanetUiState] = []
    @Published var currentMode: SimMode = .stable
    @Published var isRunning: Bool = false

    private var simulationTask: Task<Void, Never>?
    private let maxTrailSize = 100

    init() {
        loadScenario(mode: .stable)
    }

    func loadScenario(mode: SimMode) {
        currentMode = mode
        stopLoop()

        // HATA BURADAYDI: Artık 'try' ve 'do-catch' zorunlu
        do {
            // Kotlin'den gelen fonksiyonu güvenli çağırıyoruz
            try engine.initializeScenario(mode: Int32(mode.rawValue))

            // Başarılı olursa durumu güncelle
            updateStateFromEngine(resetTrails: true)

            if isRunning {
                startLoop()
            }
        } catch {
            // Eğer C++ veya Kotlin tarafında bir hata olursa uygulama çökmez, buraya düşer
            print("🚨 FATAL ERROR: Simülasyon başlatılamadı: \(error)")
            // İstersen burada kullanıcıya bir uyarı (Alert) gösterebilirsin
        }
    }

    func toggleSimulation() {
        isRunning.toggle()
        if isRunning {
            startLoop()
        } else {
            stopLoop()
        }
    }

    private func startLoop() {
        // Eski task varsa iptal et
        simulationTask?.cancel()

        simulationTask = Task {
            while isRunning {
                // 1. Adım At
                engine.step()

                // 2. UI Güncelle
                updateStateFromEngine(resetTrails: false)

                // 3. Bekle (60 FPS ~ 16ms)
                try? await Task.sleep(nanoseconds: 16_000_000)
            }
        }
    }

    private func stopLoop() {
        simulationTask?.cancel()
        simulationTask = nil
    }

    // Sürükleme olayı
    func onPlanetDragged(index: Int, x: Float, y: Float) {
        // Motoru güncelle
        engine.setBodyPosition(index: Int32(index), x: x, y: y)

        // UI'ı anlık güncelle (Kuyruğu silerek)
        // Swift'te array'i kopyalamaya gerek yok, value type'dır.
        if index < planets.count {
            var updatedPlanet = planets[index]
            // Swift'te KMP sınıfı immutable olabilir, yeni struct oluşturuyoruz
            // Not: Point2D bir data class olduğu için init gerektirir
            let newPos = Point2D(x: x, y: y)

            // Struct'ı güncelle (PlanetUiState)
            planets[index] = PlanetUiState(
                currentPos: newPos,
                color: updatedPlanet.color,
                trail: [] // Sürüklerken izi sil
            )
        }
    }

    private func updateStateFromEngine(resetTrails: Bool) {
        let state = engine.getCurrentState()
        let newPositions = [state.body1, state.body2, state.body3]

        // İlk kez oluşturuluyorsa renkleri ata
        if planets.isEmpty {
            planets = [
                PlanetUiState(currentPos: newPositions[0], color: .cyan, trail: []),
                PlanetUiState(currentPos: newPositions[1], color: .pink, trail: []),
                PlanetUiState(currentPos: newPositions[2], color: .yellow, trail: [])
            ]
            return
        }

        // Mevcut listeyi güncelle
        for i in 0..<planets.count {
            let newPos = newPositions[i]
            var currentTrail = resetTrails ? [] : planets[i].trail

            if !resetTrails {
                currentTrail.append(newPos)
                if currentTrail.count > maxTrailSize {
                    currentTrail.removeFirst()
                }
            }

            planets[i] = PlanetUiState(
                currentPos: newPos,
                color: planets[i].color,
                trail: currentTrail
            )
        }
    }

    deinit {
        engine.destroy()
    }
}