package com.nabil.apps.fileexplorer.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.nabil.apps.fileexplorer.AppConstants
import com.nabil.apps.fileexplorer.AppConstants.Companion.EXTRA_FILE_PATH
import com.nabil.apps.fileexplorer.AppConstants.Companion.PERMISSION_CODE_RECORD_AUDIO
import com.nabil.apps.fileexplorer.AppData
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.formatSeconds
import com.nabil.apps.fileexplorer.view.SimpleWaveFormRenderer
import kotlinx.android.synthetic.main.activity_sound.*
import java.io.File

class SoundActivity: ThemeableAppCompatActivity(){

    companion object{
        const val CAPTURE_SIZE = 256
    }

    lateinit var mediaPlayer: MediaPlayer
    lateinit var updateRunnable: Runnable
    lateinit var updateHandler: Handler

    private val captureListener = object:Visualizer.OnDataCaptureListener{
        override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
            view_waveform.waveform = waveform
        }
        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound)

        val color = ColorDrawable(theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).
                getColor(0, 0xffffeeeeee.toInt()))
        color.alpha = 51
        coord_layout.background = color

        val audioPath = intent.getStringExtra(EXTRA_FILE_PATH)
        tx_song_name.text = File(audioPath).name
        tx_song_name.movementMethod = ScrollingMovementMethod()
        tx_song_name.isSelected = true

        fab_back.setOnClickListener{finish()}

        mediaPlayer = MediaPlayer()

        try{
            mediaPlayer.setDataSource(audioPath)
            mediaPlayer.prepare()

            val durationDecisecs = mediaPlayer.duration/100
            val formattedDuration = formatSeconds(durationDecisecs/10)
            seek_bar_media.max = durationDecisecs
            updateHandler = Handler()
            updateRunnable = object:Runnable{
                override fun run() {
                    val currentPositionDecisecs = mediaPlayer.currentPosition / 100
                    seek_bar_media.progress = currentPositionDecisecs
                    tx_time.text = "${formatSeconds(currentPositionDecisecs/10)}  |  $formattedDuration"
                    updateHandler.postDelayed(this, 100)
                }
            }
            runOnUiThread(updateRunnable)
            seek_bar_media.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(fromUser){
                        mediaPlayer.seekTo(progress*100)
                        tx_time.text = "${formatSeconds(progress/10)}  |  $formattedDuration"

                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            mediaPlayer.setOnCompletionListener {
                img_btn_play_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            }

            savedInstanceState?.let{mediaPlayer.seekTo(it.getInt("POSITION"))}

            img_btn_play_pause.setImageResource(R.drawable.ic_pause_black_24dp)
            mediaPlayer.start()
        }
        catch(ex: Exception){
            ex.printStackTrace()
        }

        img_btn_play_pause.setOnClickListener{
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                img_btn_play_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
            }else{
                mediaPlayer.start()
                img_btn_play_pause.setImageResource(R.drawable.ic_pause_black_24dp)
            }
        }

        btn_show_vis.setOnClickListener{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO)){
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage("The record audio permission is required to show the visualizer.\n" +
                        "Please grant the app permission").setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            PERMISSION_CODE_RECORD_AUDIO)
                }.show()
            }
            else{
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        PERMISSION_CODE_RECORD_AUDIO)
            }
        }


        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED ){
            startVisualizer()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode){
            AppConstants.PERMISSION_CODE_RECORD_AUDIO ->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startVisualizer()
                }
                else{
                    Toast.makeText(this, "Couldn't show visualizer!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun startVisualizer(){
        view_waveform.renderer = SimpleWaveFormRenderer.newInstance(Color.TRANSPARENT,
                theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).getColor(0, 0xffffeeeeee.toInt()))
        btn_show_vis.visibility = View.GONE

        val visualizer = Visualizer(mediaPlayer.audioSessionId)
        visualizer.setDataCaptureListener(captureListener, Visualizer.getMaxCaptureRate(), true, false)
        visualizer.enabled=false
        visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        visualizer.enabled = true
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putInt("POSITION", mediaPlayer.currentPosition)
    }


    override fun onStop() {
        super.onStop()
        mediaPlayer.pause()
        img_btn_play_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        updateHandler.removeCallbacks(updateRunnable)
    }
}