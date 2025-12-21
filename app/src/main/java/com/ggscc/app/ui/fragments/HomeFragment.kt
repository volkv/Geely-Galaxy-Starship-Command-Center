package com.ggscc.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.ggscc.app.MainActivity
import com.ggscc.app.R
import com.ggscc.app.controllers.VehicleInfo
import com.ggscc.app.databinding.FragmentHomeBinding
import com.ggscc.app.tools.PropertyConstants
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var isLoadingState = false

    private val mainActivity: MainActivity?
        get() = activity as? MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLoadingState = true
        setupSpinners()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentState()
        startVehicleInfoUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopVehicleInfoUpdates()
    }

    private fun startVehicleInfoUpdates() {
        val registry = mainActivity?.controllerRegistry ?: return
        registry.vehicleInfo.onInfoUpdated = { info -> updateVehicleStats(info) }
        registry.vehicleInfo.startAutoUpdate(5000)
    }

    private fun stopVehicleInfoUpdates() {
        mainActivity?.controllerRegistry?.vehicleInfo?.apply {
            stopAutoUpdate()
            onInfoUpdated = null
        }
    }

    private fun updateVehicleStats(info: VehicleInfo) {
        if (_binding == null) return

        val batteryPct = info.batteryPercent?.let { "${it.toInt()}%" } ?: "--"
        val batteryTemp = info.batteryTemp?.let { String.format(Locale.US, "%.0f°C", it) } ?: ""
        binding.textBatteryPercent.text = if (batteryTemp.isNotEmpty()) "$batteryPct, $batteryTemp" else batteryPct
        binding.textBatteryRange.text = info.rangeEv?.let {
            String.format(Locale.US, "%.0f km", it)
        } ?: "--"

        binding.textFuelPercent.text = info.fuelPercent?.let { "$it%" } ?: "--"
        binding.textRangeFuel.text = info.rangeFuel?.let {
            String.format(Locale.US, "%.0f km", it)
        } ?: "--"

        val rangeEvVal = info.rangeEv ?: 0f
        val rangeFuelVal = info.rangeFuel ?: 0f
        val rangeTotal = rangeEvVal + rangeFuelVal
        binding.textRangeTotal.text = if (rangeTotal > 0) {
            String.format(Locale.US, "%.0f km", rangeTotal)
        } else "--"
        val fuelRange = info.rangeFuel?.let { String.format(Locale.US, "%.0f", it) } ?: "--"
        val evRange = info.rangeEv?.let { String.format(Locale.US, "%.0f", it) } ?: "--"
        binding.textRangeBreakdown.text = "F: $fuelRange / E: $evRange"

        binding.textMileage.text = info.odometer?.let {
            String.format(Locale.US, "%.0f km", it)
        } ?: "--"
        val fuelMileage = info.mileageSinceLastOil?.let { String.format(Locale.US, "%.0f", it) } ?: "--"
        val evMileage = info.mileageSinceLastEv?.let { String.format(Locale.US, "%.0f", it) } ?: "--"
        binding.textMileageBreakdown.text = "F: $fuelMileage / E: $evMileage"

        binding.textMaintenanceMileage.text = info.maintenanceMileage?.let {
            String.format(Locale.US, "%.0f km", it)
        } ?: "--"
        binding.textMaintenanceTime.text = info.maintenanceTime?.let {
            String.format(Locale.US, "%.0f days", it)
        } ?: "--"

        val isCharging = info.chargingPower != null && info.chargingPower > 0
        binding.cardCharging.visibility = if (isCharging) View.VISIBLE else View.GONE
        if (isCharging) {
            binding.textChargingPower.text = info.chargingPower?.let {
                String.format(Locale.US, "%.1f kW", it)
            } ?: "--"
            binding.textChargingTime.text = info.chargingTimeMinutes?.let {
                val hours = (it / 60).toInt()
                val mins = (it % 60).toInt()
                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            } ?: "--"
        }
    }

    private fun setupSpinners() {
        val massageTypes = resources.getStringArray(R.array.massage_types)
        val heatingLevels = resources.getStringArray(R.array.heating_levels)

        val massageAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, massageTypes)
        massageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        val heatingAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, heatingLevels)
        heatingAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        binding.spinnerMassageDriverType.adapter = massageAdapter
        binding.spinnerMassagePassengerType.adapter = massageAdapter
        binding.spinnerHeatingDriverLevel.adapter = heatingAdapter
        binding.spinnerHeatingPassengerLevel.adapter = heatingAdapter
    }

    private fun setupListeners() {
        binding.toggleDriveMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            mainActivity?.onDriveModeChanged(checkedId)
        }

        binding.toggleLights.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            mainActivity?.onLightsChanged(checkedId)
        }

        binding.switchFogLights.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onFogLightsChanged(isChecked)
        }

        binding.switchHud.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onHudChanged(isChecked)
        }

        binding.switchWirelessCharging.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onWirelessChargingChanged(isChecked)
        }

        binding.switchMassageDriver.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onMassageDriverChanged(isChecked, binding.spinnerMassageDriverType.selectedItemPosition)
        }

        binding.switchMassagePassenger.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onMassagePassengerChanged(isChecked, binding.spinnerMassagePassengerType.selectedItemPosition)
        }

        binding.switchHeatingDriver.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            val level = binding.spinnerHeatingDriverLevel.selectedItemPosition + 1
            mainActivity?.onHeatingDriverChanged(isChecked, level)
        }

        binding.switchHeatingPassenger.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            val level = binding.spinnerHeatingPassengerLevel.selectedItemPosition + 1
            mainActivity?.onHeatingPassengerChanged(isChecked, level)
        }

        binding.toggleSoundPosition.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            val position = when (checkedId) {
                R.id.btnSoundAll -> 0
                R.id.btnSoundDriver -> 1
                R.id.btnSoundFront -> 2
                R.id.btnSoundSurround -> 4
                else -> 0
            }
            mainActivity?.onSoundPositionChanged(position)
        }

        binding.toggleMirrors.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnMirrorFold -> mainActivity?.onMirrorFold()
                R.id.btnMirrorUnfold -> mainActivity?.onMirrorUnfold()
            }
        }

        binding.switchAmbientLight.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onAmbientLightChanged(isChecked)
        }

        binding.seekBarAmbientBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isLoadingState) return
                mainActivity?.onAmbientBrightnessChanged(seekBar?.progress ?: 50)
            }
        })

        binding.switchColorMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onColorMusicChanged(isChecked)
        }
    }

    private fun loadCurrentState() {
        val registry = (activity as? MainActivity)?.controllerRegistry ?: return
        isLoadingState = true
        try {
            val driveMode = registry.driveMode.getDriveMode() ?: PropertyConstants.DriveMode.INTELLIGENT
            val driveBtnId = when (driveMode) {
                PropertyConstants.DriveMode.RANGE_EXT -> R.id.btnDriveModeRangeExt
                PropertyConstants.DriveMode.SPORT -> R.id.btnDriveModeSport
                PropertyConstants.DriveMode.ELECTRIC -> R.id.btnDriveModeElectric
                PropertyConstants.DriveMode.INTELLIGENT -> R.id.btnDriveModeIntelligent
                else -> R.id.btnDriveModeIntelligent
            }
            binding.toggleDriveMode.check(driveBtnId)

            val lightState = registry.exteriorLight.getLightState() ?: 0
            val lightBtnId = when (lightState) {
                PropertyConstants.ExteriorLight.OFF -> R.id.btnLightsOff
                PropertyConstants.ExteriorLight.PARKING -> R.id.btnLightsParking
                PropertyConstants.ExteriorLight.HEADLIGHTS -> R.id.btnLightsHeadlights
                else -> R.id.btnLightsOff
            }
            binding.toggleLights.check(lightBtnId)

            binding.switchFogLights.isChecked = registry.fogLight.isEnabled()
            binding.switchHud.isChecked = registry.hud.isEnabled()
            binding.switchWirelessCharging.isChecked = registry.wirelessCharging.isEnabled()

            val massageDriverEnabled = registry.massage.isDriverMassageEnabled()
            binding.switchMassageDriver.isChecked = massageDriverEnabled
            if (massageDriverEnabled) {
                binding.spinnerMassageDriverType.setSelection(registry.massage.getDriverMassageType() ?: 0)
            }

            val massagePassengerEnabled = registry.massage.isPassengerMassageEnabled()
            binding.switchMassagePassenger.isChecked = massagePassengerEnabled
            if (massagePassengerEnabled) {
                binding.spinnerMassagePassengerType.setSelection(registry.massage.getPassengerMassageType() ?: 0)
            }

            val heatingDriverLevel = registry.heating.getLevel(PropertyConstants.Seat.AREA_DRIVER) ?: 0
            binding.switchHeatingDriver.isChecked = heatingDriverLevel > 0
            if (heatingDriverLevel > 0) {
                binding.spinnerHeatingDriverLevel.setSelection(heatingDriverLevel - 1)
            }

            val heatingPassengerLevel = registry.heating.getLevel(PropertyConstants.Seat.AREA_PASSENGER) ?: 0
            binding.switchHeatingPassenger.isChecked = heatingPassengerLevel > 0
            if (heatingPassengerLevel > 0) {
                binding.spinnerHeatingPassengerLevel.setSelection(heatingPassengerLevel - 1)
            }

            val surroundEnabled = registry.audio.getSurroundSwitch() ?: false
            if (surroundEnabled) {
                binding.toggleSoundPosition.check(R.id.btnSoundSurround)
            } else {
                val soundMode = registry.audio.getSeatSoundOptimize() ?: PropertyConstants.Audio.OPTIMIZE_ALL_SEATS
                val btnId = when (soundMode) {
                    PropertyConstants.Audio.OPTIMIZE_ALL_SEATS -> R.id.btnSoundAll
                    PropertyConstants.Audio.OPTIMIZE_DRIVER_ONLY -> R.id.btnSoundDriver
                    PropertyConstants.Audio.OPTIMIZE_FRONT_SEATS -> R.id.btnSoundFront
                    else -> R.id.btnSoundAll
                }
                binding.toggleSoundPosition.check(btnId)
            }

            val mirrorsFolded = registry.mirror.getMirrorState()
            if (mirrorsFolded) {
                binding.toggleMirrors.check(R.id.btnMirrorFold)
            } else {
                binding.toggleMirrors.check(R.id.btnMirrorUnfold)
            }

            binding.switchAmbientLight.isChecked = registry.ambientLight.isEnabled()
            binding.seekBarAmbientBrightness.progress = registry.ambientLight.getIntensity()
            binding.switchColorMusic.isChecked = registry.ambientLight.getMusicShowModeInner() == 1
        } finally {
            binding.root.postDelayed({
                isLoadingState = false
            }, 100)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
