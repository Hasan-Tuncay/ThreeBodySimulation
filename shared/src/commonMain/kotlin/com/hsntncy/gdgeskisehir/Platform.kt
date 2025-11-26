package com.hsntncy.gdgeskisehir

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform