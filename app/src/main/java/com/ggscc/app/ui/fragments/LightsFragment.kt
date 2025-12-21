package com.ggscc.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggscc.app.MainActivity
import com.ggscc.app.R
import com.ggscc.app.databinding.FragmentLightsBinding
import com.ggscc.app.tools.PropertyConstants

class LightsFragment : Fragment() {

    private var _binding: FragmentLightsBinding? = null
    private val binding get() = _binding!!
    private var isLoadingState = false

    private val mainActivity: MainActivity?
        get() = activity as? MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLightsBinding.inflate(inflater, container, false)
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
        binding.toggleLights.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            mainActivity?.onLightsChanged(checkedId)
        }

        binding.switchFogLights.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onFogLightsChanged(isChecked)
        }

        binding.switchLightFL.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onInteriorLightChanged(0, isChecked)
        }

        binding.switchLightFR.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onInteriorLightChanged(1, isChecked)
        }

        binding.switchLightRL.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onInteriorLightChanged(2, isChecked)
        }

        binding.switchLightRR.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onInteriorLightChanged(3, isChecked)
        }

        binding.switchLightAll.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onInteriorLightChanged(4, isChecked)
        }

        binding.sliderAmbientIntensity.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            mainActivity?.onAmbientBrightnessChanged(value.toInt())
        }

        binding.toggleAmbientMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnAmbientOff -> 0
                R.id.btnAmbientBreathe -> 1
                R.id.btnAmbientGradient -> 2
                else -> 0
            }
            mainActivity?.onAmbientModeChanged(mode)
            updatePresetsVisibility(mode)
        }

        binding.btnBreathe1.setOnClickListener { mainActivity?.onAmbientPresetChanged(1, 0) }
        binding.btnBreathe2.setOnClickListener { mainActivity?.onAmbientPresetChanged(1, 1) }
        binding.btnBreathe3.setOnClickListener { mainActivity?.onAmbientPresetChanged(1, 2) }
        binding.btnGradient1.setOnClickListener { mainActivity?.onAmbientPresetChanged(2, 0) }
        binding.btnGradient2.setOnClickListener { mainActivity?.onAmbientPresetChanged(2, 1) }
        binding.btnGradient3.setOnClickListener { mainActivity?.onAmbientPresetChanged(2, 2) }
    }

    private fun updatePresetsVisibility(mode: Int) {
        binding.layoutBreathePresets.visibility = if (mode == 1) View.VISIBLE else View.GONE
        binding.layoutGradientPresets.visibility = if (mode == 2) View.VISIBLE else View.GONE
    }

    private fun loadCurrentState() {
        val registry = (activity as? MainActivity)?.controllerRegistry ?: return
        isLoadingState = true
        try {
            val lightState = registry.exteriorLight.getLightState() ?: 0
            val lightBtnId = when (lightState) {
                PropertyConstants.ExteriorLight.OFF -> R.id.btnLightsOff
                PropertyConstants.ExteriorLight.PARKING -> R.id.btnLightsParking
                PropertyConstants.ExteriorLight.HEADLIGHTS -> R.id.btnLightsHeadlights
                else -> R.id.btnLightsOff
            }
            binding.toggleLights.check(lightBtnId)

            binding.switchFogLights.isChecked = registry.fogLight.isEnabled()

            binding.switchLightFL.isChecked = registry.light.isLightOn(PropertyConstants.Light.AREA_1)
            binding.switchLightFR.isChecked = registry.light.isLightOn(PropertyConstants.Light.AREA_2)
            binding.switchLightRL.isChecked = registry.light.isLightOn(PropertyConstants.Light.AREA_4)
            binding.switchLightRR.isChecked = registry.light.isLightOn(PropertyConstants.Light.AREA_16)
            binding.switchLightAll.isChecked = registry.light.areAllLightsOn()

            binding.sliderAmbientIntensity.value = (registry.ambientLight.getIntensity() ?: 10).toFloat()

            val isBreatheOn = registry.ambientLight.isBreatheEnabled()
            val isGradientOn = registry.ambientLight.isGradientEnabled()
            val ambientBtnId = when {
                isBreatheOn -> R.id.btnAmbientBreathe
                isGradientOn -> R.id.btnAmbientGradient
                else -> R.id.btnAmbientOff
            }
            binding.toggleAmbientMode.check(ambientBtnId)
            updatePresetsVisibility(if (isBreatheOn) 1 else if (isGradientOn) 2 else 0)
        } finally {
            binding.root.postDelayed({ isLoadingState = false }, 100)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
