package com.example.mapwidgetdemo.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.mapwidgetdemo.databinding.ActivityTabBinding
import com.example.mapwidgetdemo.ui.activity.fragment.MapViewFragment
import com.example.mapwidgetdemo.ui.activity.fragment.VideoListFragment
import com.google.android.material.tabs.TabLayout

class HomeTabActivity : AppCompatActivity() {


    lateinit var binding: ActivityTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tabs.addTab(binding.tabs.newTab().setText("MapView"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Video List"))

        val adapter = MyAdapter(
            this, supportFragmentManager, binding.tabs.tabCount
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabs))

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    @Suppress("DEPRECATION")
    internal class MyAdapter(var context: Context, fm: FragmentManager, var totalTabs: Int) :
        FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    MapViewFragment()
                }
                1 -> {
                    VideoListFragment()
                }
                else -> getItem(position)
            }
        }

        override fun getCount(): Int {
            return totalTabs
        }
    }
}