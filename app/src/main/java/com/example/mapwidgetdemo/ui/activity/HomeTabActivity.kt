package com.example.mapwidgetdemo.ui.activity

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.mapwidgetdemo.databinding.ActivityTabBinding
import com.example.mapwidgetdemo.ui.activity.fragment.MapViewFragment
import com.example.mapwidgetdemo.ui.activity.fragment.VideoListFragment
import com.google.android.material.tabs.TabLayout


class HomeTabActivity : AppCompatActivity(), MapViewFragment.OnFragmentInteractionListener {


    lateinit var binding: ActivityTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tabs.addTab(binding.tabs.newTab().setText("Map"))
        binding.tabs.addTab(binding.tabs.newTab().setText("Video List"))

        val adapter = MyAdapter(
            this, supportFragmentManager, binding.tabs.tabCount
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1

        val onPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { //Default Implementation
            }

            override fun onPageSelected(position: Int) {
                addFragments(position) //                handleScrollState()
            }

            override fun onPageScrollStateChanged(state: Int) { //Default Implementation
            }
        }

        binding.viewPager.addOnPageChangeListener(onPageChangeListener)

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        addFragments(0)
    }

    private fun addFragments(position: Int) {
        val fragTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        when (position) {
            0 -> {
                val fragment: MapViewFragment? = MapViewFragment().getInstance()
                fragTransaction.replace(binding.frame.id, fragment!!)
            }
            1 -> {
                val mtFragment: VideoListFragment? = VideoListFragment().getInstance()
                fragTransaction.replace(binding.frame.id, mtFragment!!)
            }
            else -> {}
        }
        fragTransaction.commit()
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

    override fun messageFromParentFragment() {
        Log.d("logger", "messageFromParentFragment Called")
        MapViewFragment().getCornerBoundsPointList()
    }
}