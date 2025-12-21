package com.ggscc.app.ui.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ggscc.app.MainActivity
import com.ggscc.app.R
import com.ggscc.app.databinding.FragmentAudioBinding
import com.ggscc.app.tools.PropertyConstants
import com.ggscc.app.ui.SoundConfig

class AudioFragment : Fragment() {

    private var _binding: FragmentAudioBinding? = null
    private val binding get() = _binding!!
    private var isLoadingState = false

    private val mainActivity: MainActivity?
        get() = activity as? MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLoadingState = true
        setupSoundButtons()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentState()
    }

    private fun setupSoundButtons() {
        try {
            val json = requireContext().assets.open("media/sounds.json")
                .bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<SoundConfig>>() {}.type
            val sounds: List<SoundConfig> = Gson().fromJson(json, type)

            sounds.forEach { sound ->
                val button = MaterialButton(requireContext()).apply {
                    text = sound.name
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    minimumHeight = resources.getDimensionPixelSize(R.dimen.button_height_large)
                    setOnClickListener { mainActivity?.playSound(sound.file) }
                }
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_small)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_small)
                }
                button.layoutParams = params
                binding.soundButtonsContainer.addView(button)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        binding.toggleSoundPosition.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isLoadingState || !isChecked) return@addOnButtonCheckedListener
            val position = when (checkedId) {
                R.id.btnSoundAll -> 0
                R.id.btnSoundFront -> 2
                R.id.btnSoundRear -> 3
                R.id.btnSoundDriver -> 1
                R.id.btnSoundSurround -> 4
                else -> 0
            }
            mainActivity?.onSoundPositionChanged(position)
        }

        binding.btnLoudspeaker.setOnClickListener {
            mainActivity?.toggleLoudspeaker()
        }

        binding.sliderLoudspeakerGain.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || isLoadingState) return@addOnChangeListener
            binding.textGainValue.text = "${value.toInt()}%"
            mainActivity?.onLoudspeakerGainChanged(value.toInt())
        }
    }

    private fun loadCurrentState() {
        val registry = (activity as? MainActivity)?.controllerRegistry ?: return
        val mainActivity = mainActivity ?: return
        isLoadingState = true
        try {
            val surroundEnabled = registry.audio.getSurroundSwitch() ?: false
            if (surroundEnabled) {
                binding.toggleSoundPosition.check(R.id.btnSoundSurround)
            } else {
                val soundMode = registry.audio.getSeatSoundOptimize() ?: PropertyConstants.Audio.OPTIMIZE_ALL_SEATS
                val btnId = when (soundMode) {
                    PropertyConstants.Audio.OPTIMIZE_ALL_SEATS -> R.id.btnSoundAll
                    PropertyConstants.Audio.OPTIMIZE_DRIVER_ONLY -> R.id.btnSoundDriver
                    PropertyConstants.Audio.OPTIMIZE_FRONT_SEATS -> R.id.btnSoundFront
                    PropertyConstants.Audio.OPTIMIZE_REAR_SEATS -> R.id.btnSoundRear
                    else -> R.id.btnSoundAll
                }
                binding.toggleSoundPosition.check(btnId)
            }

            binding.sliderLoudspeakerGain.value = mainActivity.loudspeakerController.gainPercent.toFloat()
            binding.textGainValue.text = "${mainActivity.loudspeakerController.gainPercent}%"
        } finally {
            binding.root.postDelayed({ isLoadingState = false }, 100)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
