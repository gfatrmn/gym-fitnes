package com.example.arenafitness

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MembershipActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)

        dbHelper = DatabaseHelper(this)
        
        // Ambil userId dari SharedPreferences
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val rgPlans = findViewById<RadioGroup>(R.id.rgPlans)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribe)

        btnSubscribe.setOnClickListener {
            val selectedPlanId = rgPlans.checkedRadioButtonId
            
            if (selectedPlanId == -1) {
                Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tentukan plan_id dan durasi berdasarkan pilihan (sesuai seed di DatabaseHelper)
            // 1: Basic (30 days), 2: Premium (90 days), 3: VIP (365 days)
            val (planId, durationDays) = when (selectedPlanId) {
                R.id.rbBasic -> Pair(1, 30)
                R.id.rbPremium -> Pair(2, 90)
                R.id.rbVIP -> Pair(3, 365)
                else -> Pair(1, 30)
            }

            processMembership(planId, durationDays)
        }

        // Setup Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_membership

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
                R.id.navigation_membership -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun processMembership(planId: Int, duration: Int) {
        val db = dbHelper.writableDatabase
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.DAY_OF_YEAR, duration)

        val startDateStr = sdf.format(startDate.time)
        val endDateStr = sdf.format(endDate.time)

        // 1. Insert ke table user_memberships
        val membershipValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UM_USER_ID, userId)
            put(DatabaseHelper.COLUMN_UM_PLAN_ID, planId)
            put(DatabaseHelper.COLUMN_UM_START_DATE, startDateStr)
            put(DatabaseHelper.COLUMN_UM_END_DATE, endDateStr)
            put(DatabaseHelper.COLUMN_UM_STATUS, "active")
        }

        val result = db.insert(DatabaseHelper.TABLE_USER_MEMBERSHIPS, null, membershipValues)

        if (result != -1L) {
            // 2. Update status is_member di table users
            val userValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 1)
            }
            db.update(
                DatabaseHelper.TABLE_USERS, 
                userValues, 
                "${DatabaseHelper.COLUMN_USER_ID} = ?", 
                arrayOf(userId.toString())
            )

            Toast.makeText(this, "Membership activated until $endDateStr", Toast.LENGTH_LONG).show()
            
            // Setelah subscribe berhasil, navigasi ke Home agar menu aktif terupdate
            startActivity(Intent(this, HomeMemberActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Failed to activate membership", Toast.LENGTH_SHORT).show()
        }
    }
}