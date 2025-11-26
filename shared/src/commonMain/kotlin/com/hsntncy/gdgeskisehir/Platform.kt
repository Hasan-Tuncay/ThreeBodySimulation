package com.hsntncy.threebodysimulation

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform