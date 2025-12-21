package com.ggscc.app

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.ggscc.app.car.CarAudioPlayer
import com.ggscc.app.car.CarModel
import com.ggscc.app.car.LoudspeakerController
import com.ggscc.app.car.VehiclePropertyHelper
import com.ggscc.app.databinding.ActivityMainNewBinding
import com.ggscc.app.tools.PropertyConstants
import com.ggscc.app.ui.MainPagerAdapter
import com.ggscc.app.ui.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainNewBinding
    private lateinit var vehicleHelper: VehiclePropertyHelper
    private lateinit var carAudioPlayer: CarAudioPlayer
    lateinit var loudspeakerController: LoudspeakerController
        private set

    var controllerRegistry: ControllerRegistry? = null
        private set
    var isCarConnected = false
        private set

    companion object {
        private const val TAG = "MainActivity"
        @Volatile
        var isInForeground = false
            private set
    }

    private val minimizeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == MediaBridgeService.ACTION_MINIMIZE_ACTIVITY) {
                Log.i(TAG, "Received minimize broadcast")
                moveTaskToBack(true)
            }
        }
    }

    val prefs by lazy { getSharedPreferences("car_control_prefs", Context.MODE_PRIVATE) }
    private val statusHandler = Handler(Looper.getMainLooper())
    private val statusRunnable = object : Runnable {
        override fun run() {
            updateServiceStatus()
            statusHandler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreen()

        carAudioPlayer = CarAudioPlayer(this)
        loudspeakerController = LoudspeakerController()

        binding.textCarModel.text = CarModel.PRODUCT_NAME

        initCarConnection()
        setupViewPager()

        if (!isServiceRunning(MediaBridgeService::class.java)) {
            MediaBridgeService.start(this)
            Log.i(TAG, "MediaBridgeService auto-started")
        }

        registerMinimizeReceiver()
    }

    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            hideSystemBars()
            view.onApplyWindowInsets(insets)
        }
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 4

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(MainPagerAdapter.TAB_TITLE_RES_IDS[position])
        }.attach()

        binding.viewPager.setCurrentItem(MainPagerAdapter.HOME_POSITION, false)

        var isTabClick = false
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                if (isTabClick) {
                    binding.viewPager.setCurrentItem(tab.position, false)
                    isTabClick = false
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        for (i in 0 until binding.tabLayout.tabCount) {
            binding.tabLayout.getTabAt(i)?.view?.setOnClickListener {
                isTabClick = true
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(i))
            }
        }
    }

    private fun registerMinimizeReceiver() {
        val filter = IntentFilter(MediaBridgeService.ACTION_MINIMIZE_ACTIVITY)
        if (Build.VERSION.SDK_INT >= 34) {
            registerReceiver(minimizeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(minimizeReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        isInForeground = true
        statusHandler.post(statusRunnable)
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onPause() {
        super.onPause()
        isInForeground = false
        statusHandler.removeCallbacks(statusRunnable)
    }

    private fun initCarConnection() {
        try {
            vehicleHelper = VehiclePropertyHelper(this)

            if (vehicleHelper.isConnected) {
                isCarConnected = true
                controllerRegistry = ControllerRegistry(vehicleHelper)
                Log.i(TAG, "Car API connected successfully")
            } else {
                isCarConnected = false
                Log.w(TAG, "Car API not connected - running in UI preview mode")
            }
        } catch (e: Exception) {
            isCarConnected = false
            Log.e(TAG, "Error initializing car connection", e)
        }
    }

    private fun updateServiceStatus() {
        val isRunning = isServiceRunning(MediaBridgeService::class.java)
        binding.indicatorServiceStatus.setBackgroundResource(
            if (isRunning) R.drawable.status_indicator_online
            else R.drawable.status_indicator_offline
        )

        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is SettingsFragment) {
                fragment.updateServiceStatus(isRunning)
            }
        }
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any { it.service.className == serviceClass.name }
    }

    private inline fun withCar(action: (ControllerRegistry) -> Unit) {
        if (isCarConnected) controllerRegistry?.let(action)
    }

    fun onDriveModeChanged(checkedId: Int) = withCar { registry ->
        val mode = when (checkedId) {
            R.id.btnDriveModeRangeExt -> PropertyConstants.DriveMode.RANGE_EXT
            R.id.btnDriveModeSport -> PropertyConstants.DriveMode.SPORT
            R.id.btnDriveModeElectric -> PropertyConstants.DriveMode.ELECTRIC
            R.id.btnDriveModeIntelligent -> PropertyConstants.DriveMode.INTELLIGENT
            else -> return@withCar
        }
        registry.driveMode.setDriveMode(mode)
    }

    fun onLightsChanged(checkedId: Int) = withCar { registry ->
        when (checkedId) {
            R.id.btnLightsOff -> registry.exteriorLight.turnOff()
            R.id.btnLightsParking -> registry.exteriorLight.setParkingLights()
            R.id.btnLightsHeadlights -> registry.exteriorLight.setHeadlights()
        }
    }

    fun onFogLightsChanged(isChecked: Boolean) = withCar { registry ->
        if (isChecked) registry.fogLight.enable() else registry.fogLight.disable()
    }

    fun onMassageDriverChanged(isEnabled: Boolean, typeIndex: Int) = withCar { registry ->
        if (isEnabled) {
            registry.massage.enableDriverMassage()
            registry.massage.setMassageType(PropertyConstants.Seat.AREA_DRIVER, typeIndex)
        } else {
            registry.massage.disableDriverMassage()
        }
    }

    fun onMassagePassengerChanged(isEnabled: Boolean, typeIndex: Int) = withCar { registry ->
        if (isEnabled) {
            registry.massage.enablePassengerMassage()
            registry.massage.setMassageType(PropertyConstants.Seat.AREA_PASSENGER, typeIndex)
        } else {
            registry.massage.disablePassengerMassage()
        }
    }

    fun onHeatingDriverChanged(isEnabled: Boolean, levelIndex: Int) = withCar { registry ->
        registry.heating.setLevel(PropertyConstants.Seat.AREA_DRIVER, if (isEnabled) levelIndex else 0)
    }

    fun onHeatingPassengerChanged(isEnabled: Boolean, levelIndex: Int) = withCar { registry ->
        registry.heating.setLevel(PropertyConstants.Seat.AREA_PASSENGER, if (isEnabled) levelIndex else 0)
    }

    fun onSoundPositionChanged(position: Int) = withCar { registry ->
        when (position) {
            0 -> {
                registry.audio.setSurroundSwitch(false)
                registry.audio.setSeatSoundOptimize(PropertyConstants.Audio.OPTIMIZE_ALL_SEATS)
            }
            1 -> {
                registry.audio.setSurroundSwitch(false)
                registry.audio.setSeatSoundOptimize(PropertyConstants.Audio.OPTIMIZE_DRIVER_ONLY)
            }
            2 -> {
                registry.audio.setSurroundSwitch(false)
                registry.audio.setSeatSoundOptimize(PropertyConstants.Audio.OPTIMIZE_FRONT_SEATS)
            }
            3 -> {
                registry.audio.setSurroundSwitch(false)
                registry.audio.setSeatSoundOptimize(PropertyConstants.Audio.OPTIMIZE_REAR_SEATS)
            }
            4 -> registry.audio.setSurroundSwitch(true)
        }
    }

    fun onInteriorLightChanged(zone: Int, isOn: Boolean) = withCar { registry ->
        when (zone) {
            0 -> if (isOn) registry.light.turnOnInteriorLight(PropertyConstants.Light.AREA_1)
                 else registry.light.turnOffInteriorLight(PropertyConstants.Light.AREA_1)
            1 -> if (isOn) registry.light.turnOnInteriorLight(PropertyConstants.Light.AREA_2)
                 else registry.light.turnOffInteriorLight(PropertyConstants.Light.AREA_2)
            2 -> if (isOn) registry.light.turnOnInteriorLight(PropertyConstants.Light.AREA_4)
                 else registry.light.turnOffInteriorLight(PropertyConstants.Light.AREA_4)
            3 -> if (isOn) registry.light.turnOnInteriorLight(PropertyConstants.Light.AREA_16)
                 else registry.light.turnOffInteriorLight(PropertyConstants.Light.AREA_16)
            4 -> if (isOn) registry.light.turnOnAllInteriorLights()
                 else registry.light.turnOffAllInteriorLights()
        }
    }

    fun onAmbientBrightnessChanged(brightness: Int) = withCar { registry ->
        registry.ambientLight.setIntensity(brightness)
    }

    fun onAmbientModeChanged(mode: Int) = withCar { registry ->
        when (mode) {
            0 -> registry.ambientLight.disableEffects()
            1 -> {
                val defaultColor = PropertyConstants.AmbientLight.BREATHE_COLORS.firstOrNull()?.first ?: 0x7B68EE
                registry.ambientLight.enableBreatheMode(defaultColor)
            }
            2 -> {
                val defaultPreset = PropertyConstants.AmbientLight.GRADIENT_PRESETS.firstOrNull()?.first
                registry.ambientLight.enableGradientMode(defaultPreset?.second ?: 0x764BA2)
            }
        }
    }

    fun onAmbientPresetChanged(mode: Int, preset: Int) = withCar { registry ->
        if (mode == 1) {
            val color = PropertyConstants.AmbientLight.BREATHE_COLORS.getOrNull(preset)?.first ?: 0x7B68EE
            registry.ambientLight.enableBreatheMode(color)
        } else {
            val gradientPreset = PropertyConstants.AmbientLight.GRADIENT_PRESETS.getOrNull(preset)?.first
            registry.ambientLight.enableGradientMode(gradientPreset?.second ?: 0x764BA2)
        }
    }

    fun onWindowChanged(windowIndex: Int, position: Int) = withCar { registry ->
        val areaId = when (windowIndex) {
            0 -> PropertyConstants.Window.AREA_FRONT_LEFT
            1 -> PropertyConstants.Window.AREA_FRONT_RIGHT
            2 -> PropertyConstants.Window.AREA_REAR_LEFT
            3 -> PropertyConstants.Window.AREA_REAR_RIGHT
            else -> return@withCar
        }
        registry.window.setWindowPosition(areaId, position)
    }

    fun openTrunk() = withCar { it.trunk.openTrunk() }

    fun closeTrunk() = withCar { it.trunk.closeTrunk() }

    fun foldMirrors() = withCar { it.mirror.foldMirrors() }

    fun unfoldMirrors() = withCar { it.mirror.unfoldMirrors() }

    fun onMirrorFold() = foldMirrors()

    fun onMirrorUnfold() = unfoldMirrors()

    fun onAmbientLightChanged(isEnabled: Boolean) = withCar { registry ->
        if (isEnabled) registry.ambientLight.enable() else registry.ambientLight.disable()
    }

    fun onBatteryModeChanged(isEnabled: Boolean) = withCar { it.battery.setBatteryMode(isEnabled) }

    fun onParkingChargeChanged(isEnabled: Boolean) = withCar { it.battery.setParkingCharge(isEnabled) }

    fun onHudChanged(isEnabled: Boolean) = withCar { registry ->
        if (isEnabled) registry.hud.enable() else registry.hud.disable()
    }

    fun onWirelessChargingChanged(isEnabled: Boolean) = withCar { registry ->
        if (isEnabled) registry.wirelessCharging.enable() else registry.wirelessCharging.disable()
    }

    fun onColorMusicChanged(isEnabled: Boolean) {
        prefs.edit().putBoolean(PrefsConstants.COLOR_MUSIC_ENABLED, isEnabled).apply()
        withCar { registry ->
            if (isEnabled) registry.ambientLight.activateColorMusic()
            else registry.ambientLight.deactivateColorMusic()
        }
        Toast.makeText(this, if (isEnabled) "Color Music: ON" else "Color Music: OFF", Toast.LENGTH_SHORT).show()
    }

    fun restartService() {
        try {
            MediaBridgeService.stop(this)
            statusHandler.postDelayed({
                MediaBridgeService.start(this)
                Toast.makeText(this, getString(R.string.toast_service_restarting), Toast.LENGTH_SHORT).show()
                updateServiceStatus()
            }, 500)
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting service", e)
            Toast.makeText(this, getString(R.string.toast_error_format, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    fun startScreenClean() {
        try {
            startActivity(Intent("com.flyme.auto.scenedirector.wash.screenclean").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error opening screen clean", e)
            Toast.makeText(this, getString(R.string.toast_screen_clean_unavailable), Toast.LENGTH_SHORT).show()
        }
    }

    fun playSound(fileName: String) {
        carAudioPlayer.playFromAssets("media/$fileName")
    }

    fun toggleLoudspeaker() {
        if (loudspeakerController.isActive()) {
            loudspeakerController.stop()
        } else {
            if (!loudspeakerController.start()) {
                Toast.makeText(this, getString(R.string.toast_loudspeaker_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onLoudspeakerGainChanged(gain: Int) {
        loudspeakerController.gainPercent = gain
        carAudioPlayer.gainPercent = gain
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(minimizeReceiver) } catch (_: Exception) {}
        controllerRegistry?.vehicleInfo?.stopAutoUpdate()
        if (::vehicleHelper.isInitialized) vehicleHelper.disconnect()
        if (::carAudioPlayer.isInitialized) carAudioPlayer.release()
        if (::loudspeakerController.isInitialized) loudspeakerController.stop()
    }
}
