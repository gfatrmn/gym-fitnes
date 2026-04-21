package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileBirthDate: TextView
    private lateinit var tvProfileGender: TextView
    private lateinit var tvProfileAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        tvProfileBirthDate = findViewById(R.id.tvProfileBirthDate)
        tvProfileGender = findViewById(R.id.tvProfileGender)
        tvProfileAddress = findViewById(R.id.tvProfileAddress)
        
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnRenewMembership = findViewById<Button>(R.id.btnRenewMembership)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Logika klik EDIT PROFILE
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Logika klik PERPANJANG
        btnRenewMembership.setOnClickListener {
            val intent = Intent(this, MembershipActivity::class.java)
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
        lifecycleScope.launch {
            val profileData = withContext(Dispatchers.IO) {
                getProfileData()
            }
            
            profileData?.let { data ->
                tvProfileName.text = data.name.uppercase()
                tvProfileEmail.text = data.email
                tvProfilePhone.text = data.phone
                tvProfileBirthDate.text = data.birthDate
                tvProfileGender.text = data.gender
                tvProfileAddress.text = data.address

                if (!data.imageUriStr.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(data.imageUriStr)
                        ivProfilePicture.setImageURI(uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ivProfilePicture.setImageDrawable(null)
                    }
                }
            }
        }
    }

    private data class ProfileData(
        val name: String,
        val email: String,
        val phone: String,
        val birthDate: String,
        val gender: String,
        val address: String,
        val imageUriStr: String?
    )

    private fun getProfileData(): ProfileData? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        var data: ProfileData? = null
        if (cursor.moveToFirst()) {
            data = ProfileData(
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)) ?: "",
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)) ?: "",
                phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE)) ?: "-",
                birthDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BIRTHDATE)) ?: "-",
                gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER)) ?: "-",
                address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ADDRESS)) ?: "-",
                imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IMAGE_URI))
            )
        }
        cursor.close()
        return data
    }
}
