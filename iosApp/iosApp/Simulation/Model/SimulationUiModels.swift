//
// Created by hasan tuncay on 25.11.2025.
//

import Foundation
import SwiftUI
import Shared // KMP Modülümüz

// Her bir gezegenin ekranda nasıl görüneceğini tutan struct
struct PlanetUiState: Identifiable {
    let id = UUID()
    let currentPos: Point2D // KMP'den gelen sınıf
    let color: Color
    let trail: [Point2D] // Kuyruk izi
}

// Mod seçimi için Enum
enum SimMode: Int, CaseIterable {
    case stable = 0
    case chaos = 1
    case solar = 2

    var title: String {
        switch self {
        case .stable: return "Stable"
        case .chaos: return "Caos"
        case .solar: return "System"
        }
    }
}