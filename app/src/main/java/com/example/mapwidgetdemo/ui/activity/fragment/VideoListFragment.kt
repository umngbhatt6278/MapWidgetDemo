package com.example.mapwidgetdemo.ui.activity.fragment

import android.content.Intent
import android.os.Bundle
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
    lateinit var adapter: MapPinListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        binding = ActivityMapPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        datalist = ArrayList()
        binding.recPins.layoutManager = LinearLayoutManager(activity!!)

        adapter = MapPinListAdapter(activity!!, object : MyViewCLickedListener {
            override fun onClick(v: View?, adapterPosition: Int) {
                if (v?.tag?.equals("CardMain") == true) {
                    val intent =
                        Intent(activity!!, VideoActivity::class.java).putExtra("VideoPath", datalist!![adapterPosition]!!.videopath)
                    startActivity(intent)
                }
            }
        })

        datalist =
            SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)

        binding.recPins.adapter = adapter

        adapter.addAll(SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST))

        binding.imgBack.setOnClickListener {
            activity!!.finish()
        }


    }
}