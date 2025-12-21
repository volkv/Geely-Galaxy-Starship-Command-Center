package com.ggscc.app.ui.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.ggscc.app.MainActivity
import com.ggscc.app.MediaBridgeService
import com.ggscc.app.PrefsConstants
import com.ggscc.app.R
import com.ggscc.app.car.CarModel
import com.ggscc.app.controllers.SystemSettingsController
import com.ggscc.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var systemSettingsController: SystemSettingsController? = null
    private var initialLangCode: String? = null
    private var initialTzId: String? = null
    private var isLoadingState = false

    private val mainActivity: MainActivity?
        get() = activity as? MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isLoadingState = true
        systemSettingsController = SystemSettingsController(requireContext())
        setupInfo()
        setupSpinners()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentState()
    }

    private fun setupInfo() {
        val versionName = try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        }
        binding.textVersion.text = "v$versionName"
        binding.textCarModelInfo.text = CarModel.PRODUCT_NAME
    }

    private fun setupSpinners() {
        val controller = systemSettingsController ?: return

        val languageAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            controller.availableLanguages.map { it.displayName }
        )
        languageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerSystemLanguage.adapter = languageAdapter

        val currentLang = controller.getCurrentLanguage()
        initialLangCode = currentLang
        val langIndex = controller.availableLanguages.indexOfFirst { it.code == currentLang }
        if (langIndex >= 0) binding.spinnerSystemLanguage.setSelection(langIndex)

        val timezoneAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            controller.availableTimeZones.map { "${it.displayName} (${it.offset})" }
        )
        timezoneAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerTimezone.adapter = timezoneAdapter

        val currentTz = controller.getCurrentTimeZone()
        initialTzId = currentTz
        val tzIndex = controller.availableTimeZones.indexOfFirst { it.id == currentTz }
        if (tzIndex >= 0) binding.spinnerTimezone.setSelection(tzIndex)
    }

    private fun setupListeners() {
        val controller = systemSettingsController ?: return

        binding.switchColorMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isLoadingState) return@setOnCheckedChangeListener
            mainActivity?.onColorMusicChanged(isChecked)
        }

        binding.btnRestartService.setOnClickListener {
            mainActivity?.restartService()
        }

        binding.btnScreenClean.setOnClickListener {
            mainActivity?.startScreenClean()
        }

        binding.spinnerSystemLanguage.post {
            if (_binding == null) return@post
            binding.spinnerSystemLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedLang = controller.availableLanguages[position]
                    if (selectedLang.code != initialLangCode) {
                        controller.setSystemLanguage(selectedLang.code)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            binding.spinnerTimezone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedTz = controller.availableTimeZones[position]
                    if (selectedTz.id != initialTzId) {
                        controller.setSystemTimeZone(selectedTz.id)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        binding.textGithubLink.setOnClickListener {
            try {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://github.com/volkv/Geely-Galaxy-Starship-Command-Center")
                )
                startActivity(intent)
            } catch (_: Exception) {}
        }

        binding.btnGeelyEngineering.setOnClickListener { controller.openGeelyEngineeringMenu() }
        binding.btnDeveloperOptions.setOnClickListener { controller.openDeveloperOptions() }
        binding.btnWifiSettings.setOnClickListener { controller.openWifiSettings() }
        binding.btnBluetoothSettings.setOnClickListener { controller.openBluetoothSettings() }
        binding.btnDisplaySettings.setOnClickListener { controller.openDisplaySettings() }
        binding.btnSoundSettings.setOnClickListener { controller.openSoundSettings() }
        binding.btnLanguageSettings.setOnClickListener { controller.openLanguageSettings() }
        binding.btnDateTimeSettings.setOnClickListener { controller.openDateTimeSettings() }
        binding.btnAppsSettings.setOnClickListener { controller.openAppsSettings() }
        binding.btnStorageSettings.setOnClickListener { controller.openStorageSettings() }
        binding.btnSecuritySettings.setOnClickListener { controller.openSecuritySettings() }
        binding.btnInputMethodSettings.setOnClickListener { controller.openInputMethodSettings() }
    }

    private fun loadCurrentState() {
        val mainActivity = mainActivity ?: return
        isLoadingState = true
        try {
            binding.switchColorMusic.isChecked = mainActivity.prefs.getBoolean(PrefsConstants.COLOR_MUSIC_ENABLED, false)
            updateServiceStatus(mainActivity.isServiceRunning(MediaBridgeService::class.java))
        } finally {
            binding.root.postDelayed({ isLoadingState = false }, 100)
        }
    }

    fun updateServiceStatus(isRunning: Boolean) {
        if (_binding == null) return
        binding.indicatorServiceStatus.setBackgroundResource(
            if (isRunning) R.drawable.status_indicator_online
            else R.drawable.status_indicator_offline
        )
        binding.textServiceStatus.text = getString(
            if (isRunning) R.string.status_running
            else R.string.status_stopped
        )
        binding.textServiceStatus.setTextColor(
            resources.getColor(
                if (isRunning) R.color.success else R.color.error,
                null
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
