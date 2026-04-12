package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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

        val ivProfilePicture = findViewById<ImageView>(R.id.ivProfilePicture)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileBirthDate = findViewById<TextView>(R.id.tvProfileBirthDate)
        val tvProfileGender = findViewById<TextView>(R.id.tvProfileGender)
        val tvProfileAddress = findViewById<TextView>(R.id.tvProfileAddress)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        
        // Load data user lengkap
        loadProfileData(ivProfilePicture, tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileBirthDate, tvProfileGender, tvProfileAddress)

        // Logika klik EDIT PROFILE
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Fitur LOGOUT
        btnLogout.setOnClickListener {
            // Hapus sesi
            sharedPref.edit().clear().apply()
            
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
        refreshProfile()
    }

    private fun refreshProfile() {
        val ivProfilePicture = findViewById<ImageView>(R.id.ivProfilePicture)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfilePhone = findViewById<TextView>(R.id.tvProfilePhone)
        val tvProfileBirthDate = findViewById<TextView>(R.id.tvProfileBirthDate)
        val tvProfileGender = findViewById<TextView>(R.id.tvProfileGender)
        val tvProfileAddress = findViewById<TextView>(R.id.tvProfileAddress)
        loadProfileData(ivProfilePicture, tvProfileName, tvProfileEmail, tvProfilePhone, tvProfileBirthDate, tvProfileGender, tvProfileAddress)
    }

    private fun loadProfileData(
        ivPicture: ImageView,
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
            val imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IMAGE_URI))

            tvName.text = name?.uppercase() ?: ""
            tvEmail.text = email ?: ""
            tvPhone.text = phone ?: "-"
            tvBirth.text = birth ?: "-"
            tvGender.text = gender ?: "-"
            tvAddress.text = address ?: "-"

            if (!imageUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(imageUriStr)
                    // Pre-check permission
                    contentResolver.openInputStream(uri)?.close()
                    ivPicture.setImageURI(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ivPicture.setImageDrawable(null)
                }
            }
        }
        cursor.close()
    }
}