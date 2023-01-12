package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapwidgetdemo.databinding.ActivityMapPinBinding
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AppConstants
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils


class MapPinActivity : BaseActivity() {

    private lateinit var binding: ActivityMapPinBinding
    var datalist: ArrayList<MarkerModel?>? = ArrayList()
    lateinit var adapter: MapPinListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        datalist = ArrayList()
        binding.recPins.layoutManager = LinearLayoutManager(this)

        adapter = MapPinListAdapter(this, object : MyViewCLickedListener {
            override fun onClick(v: View?, adapterPosition: Int) {
                if (v?.tag?.equals("CardMain") == true) {
                    val intent =
                        Intent(this@MapPinActivity, VideoActivity::class.java).putExtra("VideoPath", datalist!![adapterPosition]!!.videopath)
                    startActivity(intent)
                }
            }
        })

        datalist =
            SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)

        binding.recPins.adapter = adapter

        adapter.addAll(SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST))

        binding.imgBack.setOnClickListener {
            finish()
        }


    }


}