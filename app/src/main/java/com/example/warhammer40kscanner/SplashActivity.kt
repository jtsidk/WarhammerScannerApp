package com.example.warhammer40kscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var splashVideoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splashVideoView = findViewById(R.id.splashVideoView)

        val videoPath = "android.resource://${packageName}/${R.raw.video_intro2_vertical}"
        val uri = Uri.parse(videoPath)

        splashVideoView.setVideoURI(uri)
        splashVideoView.setMediaController(null) // Sin controles visibles

        splashVideoView.setOnCompletionListener {
            goToMainActivity()
        }

        splashVideoView.start()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
