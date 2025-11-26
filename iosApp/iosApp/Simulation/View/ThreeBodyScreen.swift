//
// Created by hasan tuncay on 25.11.2025.
//

import Foundation
import SwiftUI

struct ThreeBodyScreen: View {
    @StateObject private var viewModel = ThreeBodyViewModel()

    var body: some View {
        ZStack(alignment: .bottom) {
            // 1. Arka Plan (Canvas) - Tam ekran yayılır
            SpaceCanvas(
                planets: viewModel.planets,
                onDrag: { index, x, y in
                    viewModel.onPlanetDragged(index: index, x: x, y: y)
                }
            )
            .ignoresSafeArea() // Kenar boşluklarını yok eder (Tam ekran)

            // 2. Kontrol Paneli
            VStack(spacing: 20) {

                // Mod Seçimi (Segmented Control benzeri butonlar)
                HStack(spacing: 10) {
                    ForEach(SimMode.allCases, id: \.self) { mode in
                        Button(action: {
                            viewModel.loadScenario(mode: mode)
                        }) {
                            Text(mode.title)
                                .font(.system(size: 14, weight: .bold))
                                .padding(.vertical, 8)
                                .padding(.horizontal, 16)
                                .background(viewModel.currentMode == mode ? Color.yellow : Color.white.opacity(0.1))
                                .foregroundColor(viewModel.currentMode == mode ? .black : .white)
                                .cornerRadius(12)
                        }
                    }
                }
                .padding(8)
                .background(Color.white.opacity(0.1))
                .cornerRadius(16)

                // Başlat / Durdur Butonu
                Button(action: {
                    viewModel.toggleSimulation()
                }) {
                    Text("Start/ Stop")
                        .font(.headline)
                        .foregroundColor(.black)
                        .frame(maxWidth: 250)
                        .padding()
                        .background(Color.cyan.opacity(0.8))
                        .cornerRadius(12)
                }
            }
            .padding(.bottom, 50) // Alttan boşluk
        }
    }
}