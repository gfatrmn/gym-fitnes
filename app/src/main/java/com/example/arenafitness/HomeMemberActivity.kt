package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HomeMemberActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_member)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val ivProfile = findViewById<ImageView>(R.id.ivHomeProfilePicture)
        val tvName = findViewById<TextView>(R.id.tvHomeMemberName)
        val tvStatus = findViewById<TextView>(R.id.tvHomeMemberStatus)
        val cvCheckIn = findViewById<CardView>(R.id.cvCheckIn)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvAnnContent = findViewById<TextView>(R.id.tvAnnContent)
        val tvAnnDate = findViewById<TextView>(R.id.tvAnnDate)

        // Load Header Data
        loadHeaderData(ivProfile, tvName, tvStatus)
        
        // Generate and Display QR Code
        displayUserQRCode(ivQrCode)

        // Load Announcement
        loadAnnouncement(tvAnnContent, tvAnnDate)

        // Fitur Check-In
        cvCheckIn.setOnClickListener {
            performCheckIn()
        }

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
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

    private fun displayUserQRCode(imageView: ImageView) {
        val qrData = "ARENA_FITNESS_USER_$userId"
        val bitmap = QRCodeHelper.generateQRCode(qrData)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun loadHeaderData(iv: ImageView, tv: TextView, tvStatus: TextView) {
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
            val imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IMAGE_URI))
            
            tv.text = "Hello, ${name.split(" ")[0]}!"
            if (!imageUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(imageUriStr)
                    contentResolver.openInputStream(uri)?.close()
                    iv.setImageURI(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    iv.setImageDrawable(null)
                }
            }
        }
        cursor.close()

        // Load Membership Info
        val membershipQuery = "SELECT p.${DatabaseHelper.COLUMN_PLAN_NAME}, m.${DatabaseHelper.COLUMN_UM_END_DATE} " +
                "FROM ${DatabaseHelper.TABLE_USER_MEMBERSHIPS} m " +
                "JOIN ${DatabaseHelper.TABLE_PLANS} p ON m.${DatabaseHelper.COLUMN_UM_PLAN_ID} = p.${DatabaseHelper.COLUMN_PLAN_ID} " +
                "WHERE m.${DatabaseHelper.COLUMN_UM_USER_ID} = ? AND m.${DatabaseHelper.COLUMN_UM_STATUS} = 'active' " +
                "ORDER BY m.${DatabaseHelper.COLUMN_UM_ID} DESC LIMIT 1"
        
        val mCursor = db.rawQuery(membershipQuery, arrayOf(userId.toString()))
        if (mCursor.moveToFirst()) {
            val planName = mCursor.getString(0)
            val endDate = mCursor.getString(1)
            tvStatus.text = "$planName MEMBER • Expires: $endDate"
        } else {
            tvStatus.text = "NON-MEMBER • INACTIVE"
        }
        mCursor.close()
    }

    private fun loadAnnouncement(tvContent: TextView, tvDate: TextView) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ANNOUNCEMENTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_ANN_ID} DESC",
            "1"
        )

        if (cursor.moveToFirst()) {
            val content = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ANN_CONTENT))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ANN_DATE))
            tvContent.text = content
            tvDate.text = "Posted on: $date"
        }
        cursor.close()
    }

    private fun performCheckIn() {
        val db = dbHelper.writableDatabase
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = sdfDate.format(Date())
        val currentTime = sdfTime.format(Date())

        val cursor = db.query(
            DatabaseHelper.TABLE_CHECK_INS,
            null,
            "${DatabaseHelper.COLUMN_CI_USER_ID} = ? AND ${DatabaseHelper.COLUMN_CI_DATE} = ?",
            arrayOf(userId.toString(), currentDate),
            null, null, null
        )

        if (cursor.count > 0) {
            Toast.makeText(this, "You have already checked-in today!", Toast.LENGTH_SHORT).show()
            cursor.close()
            return
        }
        cursor.close()

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CI_USER_ID, userId)
            put(DatabaseHelper.COLUMN_CI_DATE, currentDate)
            put(DatabaseHelper.COLUMN_CI_TIME, currentTime)
        }

        val result = db.insert(DatabaseHelper.TABLE_CHECK_INS, null, values)
        if (result != -1L) {
            Toast.makeText(this, "Check-in Successful at $currentTime", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Check-in Failed", Toast.LENGTH_SHORT).show()
        }
    }
}