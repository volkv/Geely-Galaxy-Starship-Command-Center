package com.ggscc.app.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ggscc.app.R
import com.ggscc.app.ui.fragments.AudioFragment
import com.ggscc.app.ui.fragments.HomeFragment
import com.ggscc.app.ui.fragments.LightsFragment
import com.ggscc.app.ui.fragments.MiscFragment
import com.ggscc.app.ui.fragments.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LightsFragment()
            1 -> AudioFragment()
            2 -> HomeFragment()
            3 -> MiscFragment()
            4 -> SettingsFragment()
            else -> HomeFragment()
        }
    }

    companion object {
        const val HOME_POSITION = 2
        val TAB_TITLE_RES_IDS = listOf(
            R.string.nav_lights,
            R.string.nav_audio,
            R.string.nav_home,
            R.string.nav_misc,
            R.string.nav_settings
        )
    }
}
