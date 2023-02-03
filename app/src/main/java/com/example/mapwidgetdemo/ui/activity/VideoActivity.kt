package com.example.mapwidgetdemo.ui.activity

import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.databinding.ActivityVideoBinding
import com.example.mapwidgetdemo.request.EditMarkerRequestModel
import com.example.mapwidgetdemo.ui.activity.database.model.MarkerModel
import com.example.mapwidgetdemo.utils.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.launch


class VideoActivity : BaseActivity(), DialogClickInterface {

    private lateinit var binding: ActivityVideoBinding
    private var exoPlayer: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    var datalist: ArrayList<MarkerModel?>? = ArrayList()
    var mcount = 0
    var tempVideoName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)

        //disable strict mode policies
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        datalist = ArrayList()
        if (SharedPreferenceUtils.hasPreferenceKey(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)) {
            datalist = SharedPreferenceUtils.getArrayList(AppConstants.SharedPreferenceKeys.PREF_MAP_VIDEO_LIST)

        }
        setContentView(binding.root)
        preparePlayer()
    }


    private fun preparePlayer() {

        exoPlayer = ExoPlayer.Builder(this).build()

        if (!datalist!![mcount]!!.videopath.contains("https")) {
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(this, Util.getUserAgent(this, "com.example.mapwidgetdemo"))
            val mediaItem = MediaItem.fromUri(Uri.parse(datalist!![mcount]!!.videopath))
            val mediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            exoPlayer?.apply {
                setMediaSource(mediaSource)
                prepare()
            }
        } else {
            val mediaItem =
                MediaItem.Builder().setUri(datalist!![mcount]!!.videopath.replace("https", "http")).build()
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
            }
        }

        binding.playerView.player = exoPlayer

        binding.txtVideoName.text = datalist!![mcount]!!.videoname

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        exoPlayer?.playWhenReady = true
                    }
                    Player.STATE_ENDED -> if (mcount < datalist!!.size) {
                        mcount += 1
                        releasePlayer()
                        preparePlayer()
                    }

                }
            }
        })

        binding.txtVideoName.setOnClickListener {
            if (exoPlayer?.isPlaying!!) {
                pausePlayer()
            }
            DialogUtils.dialogChildNameOrRewardMsg(this, getString(R.string.app_name),
                "Enter Video Name", AppConstants.DialogCodes.DIALOG_CLAIM_REWARD, object :
                DialogClickInterface {
                override fun onClick(code: Int, msg: String) {
                    AppConstants.DialogCodes.apply {
                        when (code) {
                            DIALOG_CLAIM_REWARD -> {
                                tempVideoName = msg
                                loginViewModel.editMarker(
                                    EditMarkerRequestModel(
                                        id = datalist!![mcount]!!.id.toString(), name = msg.toString(), lat = datalist!![mcount]!!.latitude?.toDouble().toString(), long = datalist!![mcount]!!.longitude?.toDouble().toString()
                                    )
                                )
                            }
                        }
                    }
                }

            })

            lifecycleScope.launch {
                loginViewModel.allEventsFlow.collect { event ->
                    when (event) {
                        is AllEvents.SuccessBool -> {
                            when (event.code) {
                                1 -> {
                                    binding.txtVideoName.text = tempVideoName
                                    var model = MarkerModel(
                                        id = datalist!![mcount]!!.id,
                                        videoname = tempVideoName,
                                        latitude = datalist!![mcount]!!.latitude,
                                        longitude = datalist!![mcount]!!.longitude,
                                        isserver = datalist!![mcount]!!.isserver,
                                        videopath = datalist!![mcount]!!.videopath
                                    )
                                }
                            }
                        }
                        else -> {
                            val asString = event.asString(this@VideoActivity)
                            if (asString !is Unit && asString.toString().isNotBlank()) {
                                Toast.makeText(
                                    this@VideoActivity, asString.toString(), Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }




    }


    private fun releasePlayer() {
        exoPlayer?.let { player ->
            playbackPosition = player.currentPosition
            playWhenReady = player.playWhenReady
            player.release()
            exoPlayer = null
        }
    }

    private fun pausePlayer(){
        exoPlayer?.let { player ->
            player.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onClick(code: Int, msg: String) {

    }
}