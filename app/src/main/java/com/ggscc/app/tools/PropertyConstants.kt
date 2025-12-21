package com.ggscc.app.tools

object PropertyConstants {

    object VehicleInfo {
        const val ODOMETER = 291504644
        const val EV_BATTERY_PERCENTAGE = 559969079
        const val EV_BATTERY_TEMP = 559970006
        const val EV_BATTERY_VOLTAGE = 559970012
        const val FUEL_PERCENTAGE = 557871932
        const val RANGE_EV = 559969024
        const val RANGE_FUEL = 559969023
        const val MAINTENANCE_MILEAGE = 559969075
        const val MAINTENANCE_TIME = 559969076
        const val MILEAGE_SINCE_LAST_ALL = 559969819
        const val MILEAGE_SINCE_LAST_OIL = 559969820
        const val MILEAGE_SINCE_LAST_EV = 559969821
        const val CHARGING_VOLTAGE = 559968945
        const val CHARGING_CURRENT = 559968946
        const val CHARGING_POWER = 559969168
        const val CHARGING_TIME = 559968947
        const val CHARGING_PLUG_STATE = 557871791
        const val ENERGY_DRIVING = 559969560
        const val ENERGY_CLIMATE = 559969561
        const val ENERGY_BATTERY = 559969562
        const val ENERGY_OTHER = 559969563
    }

    object Seat {
        const val AREA_DRIVER = 1
        const val AREA_PASSENGER = 4

        const val LEVEL_OFF = 0
        const val LEVEL_LOW = 1
        const val LEVEL_MEDIUM = 2
        const val LEVEL_HIGH = 3
    }

    object Hvac {
        const val AC_ON_AREA = 117
        const val AUTO_ON_AREA = 1
        const val POWER_ON_AREA = 5
        const val FAN_SPEED_AREA = 5
        const val TEMP_DRIVER_AREA = 1

        const val TEMP_MIN = 15.5f
        const val TEMP_MAX = 28.5f

        const val FAN_SPEED_MIN = 1
        const val FAN_SPEED_MAX = 9
    }

    object Window {
        const val PROPERTY_ID = 322964416

        const val AREA_FRONT_LEFT = 16
        const val AREA_FRONT_RIGHT = 64
        const val AREA_REAR_LEFT = 256
        const val AREA_REAR_RIGHT = 1024
        const val AREA_SUNROOF = 65536
        const val AREA_SUNROOF_CURTAIN = 131072

        const val POSITION_CLOSED = 0
        const val POSITION_OPEN = 100
        const val POSITION_MIN = 0
        const val POSITION_MAX = 100
    }

    object Trunk {
        const val PROPERTY_ID = 373295873
        const val AREA_ID = 536870912

        const val STATE_CLOSED = 0
        const val STATE_OPEN = 1
    }

    object SeatMassage {
        const val SWITCH_PROPERTY_ID = 622883040
        const val POWER_PROPERTY_ID = 624980189
        const val TYPE_PROPERTY_ID = 624980193

        const val POWER_OFF = 0
        const val POWER_HIGH = 3

        const val MAX_TYPE = 8
    }

    object Light {
        const val PROPERTY_ID = 356544592

        const val AREA_1 = 1
        const val AREA_2 = 2
        const val AREA_4 = 4
        const val AREA_16 = 16
        const val AREA_64 = 64

        const val STATE_OFF = 0
        const val STATE_ON = 1
    }

    object SeatHeating {
        const val PROPERTY_ID = 356517131
    }

    object WindowHeat {
        const val PROPERTY_ID = 354419988
        const val AREA = 2
    }

    object DriveMode {
        const val PROPERTY_ID = 557871372
        const val AREA = 0

        const val RANGE_EXT = 0
        const val SPORT = 2
        const val ELECTRIC = 16
        const val INTELLIGENT = 24
    }

    object ExteriorLight {
        const val PROPERTY_ID = 557871126
        const val AREA = 0

        const val OFF = 0
        const val PARKING = 1
        const val HEADLIGHTS = 3
    }

    object SeatVentilation {
        const val PROPERTY_ID = 356517139
    }

    object Environment {
        const val OUTSIDE_TEMPERATURE_F = 559969561
        const val AREA_GLOBAL = 0
    }

    object WheelButton {
        const val KEY_LEFT = 557872226
        const val KEY_RIGHT = 557872227
        const val KEY_CONFIRM = 557872232
    }

    object AmbientLight {
        const val SWITCH_PROPERTY_ID = 555774339
        const val INTENSITY_PROPERTY_ID = 624980457
        const val BREATHE_MODE_PROPERTY_ID = 555775017
        const val TRANSITION_MODE_PROPERTY_ID = 555774464
        const val BREATHE_COLOR_PROPERTY_ID = 624980459
        const val TRANSITION_END_COLOR_PROPERTY_ID = 557871618

        const val MAINCOLOR_PROPERTY_ID = 557871143
        const val MUSIC_SHOW_MODE_PROPERTY_ID = 557871610
        const val MUSIC_SHOW_MODE_INNER_PROPERTY_ID = 557872864
        const val COLOR_SET_PROPERTY_ID = 624980014
        const val THEME_COLOR_PROPERTY_ID = 557871594
        const val ZONES_SYNC_PROPERTY_ID = 622883308

        const val AREA_FRONT_ROW = 5
        const val AREA_GLOBAL = 0

        const val INTENSITY_MIN = 0
        const val INTENSITY_MAX = 20

        const val MAINCOLOR_MUSIC = 1
        const val MAINCOLOR_SETCOLOR = 2
        const val MAINCOLOR_DRIVERMODE = 3
        const val MAINCOLOR_BREATHE = 4
        const val MAINCOLOR_SPEED = 5

        val BREATHE_COLORS = listOf(
            0x7B68EE to "Аметист",
            0x00CED1 to "Бирюза",
            0xFF6B6B to "Коралл"
        )

        val GRADIENT_PRESETS = listOf(
            (0x667EEA to 0x764BA2) to "Закат",
            (0x11998E to 0x38EF7D) to "Северное сияние",
            (0xF093FB to 0xF5576C) to "Розовый рассвет"
        )
    }

    object Audio {
        const val SEAT_SOUND_OPTIMIZE_PROPERTY_ID = 557872198
        const val SURROUND_SWITCH_PROPERTY_ID = 555775047

        const val AREA_GLOBAL = 0

        const val OPTIMIZE_ALL_SEATS = 0
        const val OPTIMIZE_DRIVER_ONLY = 1
        const val OPTIMIZE_REAR_SEATS = 2
        const val OPTIMIZE_FRONT_SEATS = 10
    }

    object Mirror {
        const val PROPERTY_ID = 287312709
        const val AREA = 0
    }

    object FogLight {
        const val PROPERTY_ID = 289410578
        const val AREA = 0
    }

    object Battery {
        const val MODE_PROPERTY_ID = 557872611
        const val PARKING_CHARGE_PROPERTY_ID = 555774850
        const val AREA = 0

        const val MODE_NORMAL = 1
        const val MODE_CHARGE = 3
    }

    object Hud {
        const val ACTIVE_PROPERTY_ID = 555774018
        const val AREA = 0
    }

    object WirelessCharging {
        const val PROPERTY_ID = 622882817
        const val AREA = 5
    }
}
