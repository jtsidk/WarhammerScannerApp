package com.example.warhammer40kscanner

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.warhammer40kscanner.databinding.ActivitySesionBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SesionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySesionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.emailEt.text.toString()
        val contraseña = binding.passwordEt.text.toString()



        binding.iniciarSesionBt.setOnClickListener {
            shineEffect()

            val email = binding.emailEt.text.toString()
            val contraseña = binding.passwordEt.text.toString()


            // Petición a la API
            val loginRequest = LoginRequest(email = email, password = contraseña)

            ApiClient.retrofit.login(loginRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@SesionActivity, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SesionActivity, FragmentsActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@SesionActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@SesionActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun shineEffect() {
        val animation = TranslateAnimation(
            0f,(binding.sessionButtonIv.width + binding.sessionEffectIv.width).toFloat(), 0f, 0f
        ).apply{
            duration = 550
            fillAfter = false
            interpolator = AccelerateDecelerateInterpolator()
        }

        binding.sessionEffectIv.startAnimation(animation)
    }

}