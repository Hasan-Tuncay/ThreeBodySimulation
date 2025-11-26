//
// Created by hasan tuncay on 25.11.2025.
//

import Foundation
import SwiftUI
import Shared

struct Star: Identifiable {
    let id = UUID()
    let x: CGFloat
    let y: CGFloat
    let size: CGFloat
    let opacity: Double
}

struct SpaceCanvas: View {
    let planets: [PlanetUiState]
    let onDrag: (Int, Float, Float) -> Void

    // Sabitler (Android ile aynı)
    private let scale: CGFloat = 60.0
    private let touchPadding: CGFloat = 50.0

    // Yıldızları bir kere oluştur (State)
    @State private var stars: [Star] = []

    // Sürükleme durumu
    @State private var draggedPlanetIndex: Int? = nil

    var body: some View {
        GeometryReader { geometry in
            let centerX = geometry.size.width / 2
            let centerY = geometry.size.height / 2

            ZStack {
                // 1. Arka Plan (Yıldızlar)
                Canvas { context, size in
                    for star in stars {
                        let rect = CGRect(
                            x: star.x * size.width,
                            y: star.y * size.height,
                            width: star.size,
                            height: star.size
                        )
                        context.opacity = star.opacity
                        context.fill(Path(ellipseIn: rect), with: .color(.white))
                    }
                }

                // 2. Gezegenler ve İzler
                Canvas { context, size in

                    for (index, planet) in planets.enumerated() {
                        let visualRadius = getVisualRadius(index: index)

                        // İZLER (Trail)
                        if !planet.trail.isEmpty {
                            var path = Path()
                            let startPoint = toScreen(planet.trail[0], centerX, centerY)
                            path.move(to: startPoint)

                            for point in planet.trail.dropFirst() {
                                path.addLine(to: toScreen(point, centerX, centerY))
                            }

                            context.stroke(
                                path,
                                with: .color(planet.color.opacity(0.4)),
                                lineWidth: 5
                            )
                        }

                        // GEZEGEN GÖVDESİ
                        let planetCenter = toScreen(planet.currentPos, centerX, centerY)

                        // Glow (Parlama)
                        let glowRect = CGRect(
                            x: planetCenter.x - visualRadius * 2.5,
                            y: planetCenter.y - visualRadius * 2.5,
                            width: visualRadius * 5,
                            height: visualRadius * 5
                        )
                        context.fill(Path(ellipseIn: glowRect), with: .color(planet.color.opacity(0.2)))

                        // Çekirdek
                        let coreRect = CGRect(
                            x: planetCenter.x - visualRadius,
                            y: planetCenter.y - visualRadius,
                            width: visualRadius * 2,
                            height: visualRadius * 2
                        )
                        context.fill(Path(ellipseIn: coreRect), with: .color(planet.color))
                    }
                }
                    // --- DOKUNMA VE SÜRÜKLEME ---
                .gesture(
                    DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        // Başlangıç anı (İlk yakalama)
                        if draggedPlanetIndex == nil {
                            let startLoc = value.startLocation

                            // MÜKEMMEL SEÇİM MANTIĞI (Android'dekiyle aynı)
                            // Mesafesi limit içinde olan EN YAKIN gezegeni bul
                            let found = planets.enumerated()
                            .map { (index, planet) -> (Int, CGFloat) in
                                let screenPos = toScreen(planet.currentPos, centerX, centerY)
                                let dx = startLoc.x - screenPos.x
                                let dy = startLoc.y - screenPos.y
                                let dist = sqrt(dx*dx + dy*dy)

                                // Tıklanabilir alan = Kendi yarıçapı + Padding
                                let hitRadius = getVisualRadius(index: index) + touchPadding

                                // Eğer menzil içindeyse (index, distance) döndür, değilse distance sonsuz olsun
                                return (dist <= hitRadius) ? (index, dist) : (index, CGFloat.infinity)
                            }
                            .filter { $0.1 != CGFloat.infinity } // Menzil dışındakileri at
                            .min { $0.1 < $1.1 } // Mesafesi en küçük olanı seç

                            draggedPlanetIndex = found?.0
                        }

                        // Sürükleme anı
                        if let index = draggedPlanetIndex {
                            // Screen -> Physics Dönüşümü
                            let currentLoc = value.location
                            let physX = Float((currentLoc.x - centerX) / scale)
                            let physY = Float((centerY - currentLoc.y) / scale) // Y ekseni ters

                            onDrag(index, physX, physY)
                        }
                    }
                    .onEnded { _ in
                        draggedPlanetIndex = nil
                    }
                )
            }
        }
        .background(
            RadialGradient(
                gradient: Gradient(colors: [Color(hex: 0x0D1117), .black]),
                center: .center,
                startRadius: 0,
                endRadius: 500
            )
        )
        .onAppear {
            generateStars()
        }
    }

    // Yardımcılar
    private func getVisualRadius(index: Int) -> CGFloat {
        switch index {
        case 0: return 16.0 // Güneş
        case 1: return 8.0 // Dünya
        case 2: return 2.0 // Ay
        default: return 15.0
        }
    }

    private func toScreen(_ point: Point2D, _ cx: CGFloat, _ cy: CGFloat) -> CGPoint {
        return CGPoint(
            x: cx + CGFloat(point.x) * scale,
            y: cy - CGFloat(point.y) * scale
        )
    }

    private func generateStars() {
        if !stars.isEmpty { return }
        for _ in 0..<100 {
            stars.append(Star(
                x: CGFloat.random(in: 0...1),
                y: CGFloat.random(in: 0...1),
                size: CGFloat.random(in: 1...4),
                opacity: Double.random(in: 0.2...1.0)
            ))
        }
    }
}

// Renk Hex Helper
extension Color {
    init(hex: UInt, alpha: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xff) / 255,
            green: Double((hex >> 08) & 0xff) / 255,
            blue: Double((hex >> 00) & 0xff) / 255,
            opacity: alpha
        )
    }
}