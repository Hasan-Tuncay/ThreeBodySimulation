package com.hsntncy.threebodysimulation.model

// Modları isimlendirelim
enum class SimMode(val title: String, val modeId: Int) {
    STABLE("Stable", 0),
    CHAOS("Caos", 1),
    SOLAR("System", 2)
}