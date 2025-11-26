package com.hsntncy.gdgeskisehir
actual class ThreeBodyEngine {

    // Kütüphaneyi yükle (CMakeLists.txt'de "box2d_bridge" demiştik)
    init {
        System.loadLibrary("box2d_bridge")
    }



        // JNI Adı değişti dikkat!
        private external fun nativeInitScenario(mode: Int)

        actual fun initializeScenario(mode: Int) {
            nativeInitScenario(mode)
        }

    // --- JNI Tanımları ---
    private external fun nativeInitFigure8()
    private external fun nativeStep()
    // Performans için 6 float'u tek dizide alıyoruz
    private external fun nativeGetState(): FloatArray
    private external fun nativeDestroy()

    // --- Actual Implementasyonları ---



    actual fun step() {
        nativeStep()
    }

    actual fun getCurrentState(): ThreeBodyState {
        // C++'tan [x1, y1, x2, y2, x3, y3] şeklinde dizi gelir
        val rawData = nativeGetState()

        return ThreeBodyState(
            body1 = Point2D(rawData[0], rawData[1]),
            body2 = Point2D(rawData[2], rawData[3]),
            body3 = Point2D(rawData[4], rawData[5])
        )
    }

    actual fun destroy() {
        nativeDestroy()
    }

    private external fun nativeSetBodyPosition(index: Int, x: Float, y: Float)

    actual fun setBodyPosition(index: Int, x: Float, y: Float) {
        nativeSetBodyPosition(index, x, y)
    }



}