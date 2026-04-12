package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cek Session sebelum menampilkan layout
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userId = sharedPref.getInt("USER_ID", -1)

        if (isLoggedIn && userId != -1) {
            val intent = Intent(this, HomeMemberActivity::class.java)
            startActivity(intent)
            finish()
            return // Keluar agar tidak load layout onboarding
        }

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