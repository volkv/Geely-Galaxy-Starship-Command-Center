package com.ggscc.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggscc.app.MainActivity
import com.ggscc.app.databinding.FragmentMiscBinding
import com.ggscc.app.tools.PropertyConstants

class MiscFragment : Fragment() {

    private var _binding: FragmentMiscBinding? = null
    private val binding get() = _binding!!
    private var isLoadingState = false

    private val mainActivity: MainActivity?
        get() = activity as? MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiscBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLoadingState = true
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentState()
    }

    private fun setupListeners() {
        binding.sliderWindowFL.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            mainActivity?.onWindowChanged(0, value.toInt())
        }

        binding.sliderWindowFR.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            mainActivity?.onWindowChanged(1, value.toInt())
        }

        binding.sliderWindowRL.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            mainActivity?.onWindowChanged(2, value.toInt())
        }

        binding.sliderWindowRR.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            mainActivity?.onWindowChanged(3, value.toInt())
        }

        binding.btnTrunkOpen.setOnClickListener {
            mainActivity?.openTrunk()
        }

        binding.btnTrunkClose.setOnClickListener {
            mainActivity?.closeTrunk()
        }

        binding.btnMirrorsFold.setOnClickListener {
            mainActivity?.foldMirrors()
        }

        binding.btnMirrorsUnfold.setOnClickListener {
            mainActivity?.unfoldMirrors()
        }

        binding.switchBatteryMode.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onBatteryModeChanged(isChecked)
        }

        binding.switchParkingCharge.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onParkingChargeChanged(isChecked)
        }

        binding.switchHud.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onHudChanged(isChecked)
        }

        binding.switchWirelessCharging.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onWirelessChargingChanged(isChecked)
        }
    }

    private fun loadCurrentState() {
        val registry = (activity as? MainActivity)?.controllerRegistry ?: return
        isLoadingState = true
        try {
            binding.sliderWindowFL.value = (registry.window.getWindowPosition(PropertyConstants.Window.AREA_FRONT_LEFT) ?: 0).toFloat()
            binding.sliderWindowFR.value = (registry.window.getWindowPosition(PropertyConstants.Window.AREA_FRONT_RIGHT) ?: 0).toFloat()
            binding.sliderWindowRL.value = (registry.window.getWindowPosition(PropertyConstants.Window.AREA_REAR_LEFT) ?: 0).toFloat()
            binding.sliderWindowRR.value = (registry.window.getWindowPosition(PropertyConstants.Window.AREA_REAR_RIGHT) ?: 0).toFloat()

            binding.switchBatteryMode.isChecked = registry.battery.isBatteryModeEnabled()
            binding.switchParkingCharge.isChecked = registry.battery.isParkingChargeEnabled()
            binding.switchHud.isChecked = registry.hud.isEnabled()
            binding.switchWirelessCharging.isChecked = registry.wirelessCharging.isEnabled()
        } finally {
            binding.root.postDelayed({ isLoadingState = false }, 100)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
