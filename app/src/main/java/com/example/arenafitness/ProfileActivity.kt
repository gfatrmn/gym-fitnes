package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileBirthDate = findViewById<TextView>(R.id.tvProfileBirthDate)
        val tvProfileGender = findViewById<TextView>(R.id.tvProfileGender)
        val tvProfileAddress = findViewById<TextView>(R.id.tvProfileAddress)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        
        // Load data user lengkap
        loadProfileData(tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileBirthDate, tvProfileGender, tvProfileAddress)

        // Logika klik EDIT PROFILE
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_profile

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeMemberActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_membership -> {
                    startActivity(Intent(this, MembershipActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali dari halaman Edit Profile
        refreshProfile()
    }

    private fun refreshProfile() {
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileBirthDate = findViewById<TextView>(R.id.tvProfileBirthDate)
        val tvProfileGender = findViewById<TextView>(R.id.tvProfileGender)
        val tvProfileAddress = findViewById<TextView>(R.id.tvProfileAddress)
        loadProfileData(tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileBirthDate, tvProfileGender, tvProfileAddress)
    }

    private fun loadProfileData(
        tvName: TextView, 
        tvEmail: TextView, 
        tvPhone: TextView, 
        tvBirth: TextView, 
        tvGender: TextView, 
        tvAddress: TextView
    ) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE))
            val birth = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BIRTHDATE))
            val gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ADDRESS))

            tvName.text = name.uppercase()
            tvEmail.text = email
            tvPhone.text = phone ?: "-"
            tvBirth.text = birth ?: "-"
            tvGender.text = gender ?: "-"
            tvAddress.text = address ?: "-"
        }
        cursor.close()
    }
}