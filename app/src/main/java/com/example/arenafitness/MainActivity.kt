package com.example.arenafitness

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cari tombol
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnAlreadyAccount = findViewById<Button>(R.id.btnAlreadyAccount)

        // Pindah ke RegisterActivity saat Get Started diklik
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Pindah ke LoginActivity saat Already Account diklik
        btnAlreadyAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}