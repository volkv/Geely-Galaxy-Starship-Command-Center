package com.ggscc.app.controllers

import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.SeatVentilation

class SeatVentilationController(vehicleHelper: VehiclePropertyHelper) :
    SeatLevelController(vehicleHelper, SeatVentilation.PROPERTY_ID) {

    override val TAG = "SeatVentilationController"
}
