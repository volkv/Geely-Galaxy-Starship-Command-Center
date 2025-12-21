package com.ggscc.app

import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.controllers.*

class ControllerRegistry(private val vehicleHelper: VehiclePropertyHelper) {
    val driveMode by lazy { DriveModeController(vehicleHelper) }
    val trunk by lazy { TrunkController(vehicleHelper) }
    val window by lazy { WindowController(vehicleHelper) }
    val mirror by lazy { MirrorController(vehicleHelper) }
    val exteriorLight by lazy { ExteriorLightController(vehicleHelper) }
    val fogLight by lazy { FogLightController(vehicleHelper) }
    val light by lazy { LightController(vehicleHelper) }
    val ambientLight by lazy { AmbientLightController(vehicleHelper) }
    val massage by lazy { SeatMassageController(vehicleHelper) }
    val heating by lazy { SeatHeatingController(vehicleHelper) }
    val ventilation by lazy { SeatVentilationController(vehicleHelper) }
    val audio by lazy { AudioController(vehicleHelper) }
    val battery by lazy { BatteryController(vehicleHelper) }
    val vehicleInfo by lazy { VehicleInfoController(vehicleHelper) }
    val hud by lazy { HudController(vehicleHelper) }
    val wirelessCharging by lazy { WirelessChargingController(vehicleHelper) }
}
