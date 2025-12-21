package com.ggscc.app.controllers

import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.tools.PropertyConstants.SeatHeating

class SeatHeatingController(vehicleHelper: VehiclePropertyHelper) :
    SeatLevelController(vehicleHelper, SeatHeating.PROPERTY_ID) {

    override val TAG = "SeatHeatingController"
}
