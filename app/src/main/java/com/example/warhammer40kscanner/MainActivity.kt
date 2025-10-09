package com.example.warhammer40kscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.warhammer40kscanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Reproducir vídeo de fondo
        val videoView = binding.videoFondo
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.background}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f) // Silenciar si quieres
        }
        videoView.start()

        // Botones
        binding.iniciarSesion.setOnClickListener {
            val intent = Intent(this, SesionActivity::class.java)
            startActivity(intent)
        }

        binding.consultarTransfondo.setOnClickListener {
            val intent = Intent(this, LoreActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val videoView = binding.videoFondo
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.background}")
        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)
        }
        videoView.start()
    }
}