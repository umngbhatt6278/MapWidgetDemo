package com.example.mapwidgetdemo.ui.activity.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapwidgetdemo.databinding.ActivityMapPinBinding
import com.example.mapwidgetdemo.ui.activity.BaseFragment
import com.example.mapwidgetdemo.ui.activity.MapPinListAdapter
import com.example.mapwidgetdemo.ui.activity.MyViewCLickedListener
import com.example.mapwidgetdemo.ui.activity.VideoActivity
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils

class VideoListFragment : BaseFragment() {

    lateinit var binding: ActivityMapPinBinding
    var datalist: ArrayList<MarkerModel?>? = ArrayList()
    var adapter: MapPinListAdapter? = null


    private var offersFragmentTwo: VideoListFragment? = null
    fun getInstance(): VideoListFragment? {
        if (offersFragmentTwo == null) {
            offersFragmentTwo = VideoListFragment()
        }
        return offersFragmentTwo
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        binding = ActivityMapPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {

        Log.d("logger", "VideoFragment initViews called")

        datalist = ArrayList()
        binding.recPins.layoutManager = LinearLayoutManager(activity!!)

        adapter = MapPinListAdapter(activity!!, object : MyViewCLickedListener {
            override fun onClick(v: View?, adapterPosition: Int) {
                if (v?.tag?.equals("CardMain") == true) {
                    val intent = Intent(activity!!, VideoActivity::class.java)
                    startActivity(intent)
                }
            }
        })
        binding.recPins.adapter = adapter

        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)) {
            datalist =
                SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)
            if (datalist!!.isNotEmpty()) {
                adapter?.addAll(datalist)
            }
        }
    }


    override fun onResume() {
        super.onResume() //        MapViewFragment().getCornerBoundsPointList()

    }
}