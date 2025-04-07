package com.app.pawcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            Toast.makeText(this, "Se encuentra en registro de usuario", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, RegistroActivity::class.java)

            startActivity(intent)
        }
    }
}