package com.hsntncy.threebodysimulation



expect class ThreeBodyEngine() {

    fun initializeScenario(mode: Int)
    fun setBodyPosition(index: Int, x: Float, y: Float) // <-- EKLE
    fun step()
    fun getCurrentState(): ThreeBodyState
    fun destroy()
}