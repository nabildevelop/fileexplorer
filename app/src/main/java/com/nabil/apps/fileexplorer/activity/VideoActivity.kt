package com.nabil.apps.fileexplorer.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import com.nabil.apps.fileexplorer.AppConstants.Companion.EXTRA_FILE_PATH
import com.nabil.apps.fileexplorer.R
import com.nabil.apps.fileexplorer.formatSeconds
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity: ThemeableAppCompatActivity() {


    lateinit var updateRunnable: Runnable
    lateinit var updateHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        val color = ColorDrawable(theme.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).
                getColor(0, 0xffffeeeeee.toInt()))
        color.alpha = 51
        layout.background = color

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if(intent.hasExtra(EXTRA_FILE_PATH)){
            video_view.setVideoPath(intent.getStringExtra(EXTRA_FILE_PATH))

            video_view.setOnPreparedListener{vp->
                val durationDecisecs = video_view.duration/100
                val formattedDuration = formatSeconds(durationDecisecs/10)
                tx_media_duration.text = formattedDuration
                seek_bar_media.max = durationDecisecs

                updateHandler = Handler()
                updateRunnable = object:Runnable{
                    override fun run() {
                        val currentPositionDecisecs = video_view.currentPosition / 100
                        seek_bar_media.progress = currentPositionDecisecs
                        tx_elapsed_time.text = "${formatSeconds(currentPositionDecisecs/10)}"
                        updateHandler.postDelayed(this, 100)
                    }
                }
                runOnUiThread(updateRunnable)
                seek_bar_media.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if(fromUser){
                            video_view.seekTo(progress*100)
                            tx_elapsed_time.text = "${formatSeconds(progress/10)}"
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                img_btn_play_pause.setOnClickListener{
                    if(video_view.isPlaying){
                        video_view.pause()
                        img_btn_play_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                    }else{
                        video_view.start()
                        img_btn_play_pause.setImageResource(R.drawable.ic_pause_black_24dp)
                    }
                }
                video_view.setOnCompletionListener {
                    img_btn_play_pause.setImageResource(R.drawable.ic_play_arrow_black_24dp)
                }

                savedInstanceState?.let{video_view.seekTo(it.getInt("POSITION"))}

                video_view.start()

                video_view.setOnTouchListener { v, event ->
                    if(event.action == MotionEvent.ACTION_UP){
                        control_panel.apply{visibility=if(visibility==View.GONE)View.VISIBLE else View.GONE}
                        window.decorView.systemUiVisibility = if(window.decorView.systemUiVisibility==View.SYSTEM_UI_FLAG_FULLSCREEN)
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE else View.SYSTEM_UI_FLAG_FULLSCREEN
                    }
                    true
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.putInt("POSITION", video_view.currentPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateRunnable)
    }

}