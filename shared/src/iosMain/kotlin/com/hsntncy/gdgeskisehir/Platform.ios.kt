package com.hsntncy.gdgeskisehir

import platform.UIKit.UIDevice
// .def dosyasında "package = cocoapods.box2d" dediysen import odur.

import kotlinx.cinterop.*





class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()