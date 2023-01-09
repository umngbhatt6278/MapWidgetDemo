package com.example.mapwidgetdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapwidgetdemo.databinding.ActivityMapPinBinding
import com.example.mapwidgetdemo.ui.activity.database.MarkerViewModel
import com.example.mapwidgetdemo.ui.activity.database.WordViewModelFactory
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.AllEvents
import kotlinx.coroutines.launch
import java.io.File

class MapPinActivity : BaseActivity() {

    private lateinit var binding: ActivityMapPinBinding
    var datalist: ArrayList<MarkerModel> = ArrayList()
    lateinit var adapter: MapPinListAdapter

    private val wordViewModel: MarkerViewModel by viewModels {
        WordViewModelFactory((application as MainApplication).repository)
    }


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
                        Intent(this@MapPinActivity, VideoActivity::class.java).putExtra("VideoPath", datalist[adapterPosition].videopath)
                    startActivity(intent)
                }
            }
        })

        binding.recPins.adapter = adapter

        wordViewModel.allWords.observe(this@MapPinActivity) { words -> // Update the cached copy of the words in the adapter.
            words.let {
                val data = it
                datalist = ArrayList()
                if (!data.isNullOrEmpty()) {
                    datalist.addAll(data)
                }
                adapter.addAll(datalist)
            }

        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        /*loginViewModel.saveVideo(currentLatitude, currentLongitude, File(cameraView!!.mediaPath.toString()).name, cameraView!!.mediaPath.toString())

        lifecycleScope.launch {
            loginViewModel.allEventsFlow.collect { event ->
                when (event) {
                    is AllEvents.SuccessBool -> {
                        when (event.code) {
                            1 -> {
                                Log.d("mytag", "Video Uploaded Sucessfully")
                                setCameraClose()
                                requireActivity().finishAffinity()
                            }
                        }
                    }
                    else -> {
                        val asString = event.asString(requireActivity())
                        if (asString !is Unit && asString.toString().isNotBlank()) {
                            Toast.makeText(
                                requireActivity(), asString.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }*/
    }


}