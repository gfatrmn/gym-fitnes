package com.example.arenafitness

import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(android.R.layout.simple_list_item_1, container, false)
        view?.findViewById<TextView>(android.R.id.text1)?.text = "Tip: Stay hydrated during workouts!"
        return view
    }
}

class HomeMemberActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var isMember: Boolean = false
    private var membershipStatus: String = "none"

    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var cvQrContainer: CardView
    private lateinit var ivQrCode: ImageView
    private lateinit var tvQrLabel: TextView
    private lateinit var tvIdTitle: TextView
    private lateinit var tvAnnContent: TextView
    private lateinit var tvAnnDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_member)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        // Initialize Views
        ivProfile = findViewById(R.id.ivHomeProfilePicture)
        tvName = findViewById(R.id.tvHomeMemberName)
        tvStatus = findViewById(R.id.tvHomeMemberStatus)
        val cvCheckIn = findViewById<CardView>(R.id.cvCheckIn)
        cvQrContainer = findViewById(R.id.cvQrCode)
        ivQrCode = findViewById(R.id.ivQrCode)
        tvQrLabel = findViewById(R.id.tvQrLabel)
        tvIdTitle = findViewById(R.id.tvIdTitle)
        tvAnnContent = findViewById(R.id.tvAnnContent)
        tvAnnDate = findViewById(R.id.tvAnnDate)

        // ListView Implementation
        val lvClasses = findViewById<ListView>(R.id.lvClasses)
        lvClasses?.let {
            val suggestions = arrayOf(
                "Monday: Chest & Triceps",
                "Tuesday: Back & Biceps",
                "Wednesday: Rest Day / Cardio",
                "Thursday: Shoulders & Abs",
                "Friday: Leg Day",
                "Saturday: Full Body / HIIT",
                "Sunday: Active Recovery"
            )
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, suggestions)
            it.adapter = adapter
            registerForContextMenu(it)
        }

        ivProfile.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.profile_popup_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.pop_view_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Fragment Transaction
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, InfoFragment())
            .commit()

        // Load Data
        refreshHomeData()

        // Check-In Logic
        cvCheckIn?.setOnClickListener {
            when (membershipStatus) {
                "active" -> showTimePickerAndCheckIn()
                "pending" -> Toast.makeText(this, "Membership Anda sedang menunggu validasi kasir.", Toast.LENGTH_LONG).show()
                else -> {
                    Toast.makeText(this, "Hanya member aktif yang dapat melakukan Check-In", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MembershipActivity::class.java))
                }
            }
        }

        // Bottom Navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation?.let {
            it.selectedItemId = R.id.navigation_home
            it.setOnItemSelectedListener { item ->
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
    }

    override fun onResume() {
        super.onResume()
        refreshHomeData()
    }

    private fun refreshHomeData() {
        loadHeaderData()
        
        if (membershipStatus == "active") {
            tvIdTitle.visibility = View.VISIBLE
            cvQrContainer.visibility = View.VISIBLE
            tvQrLabel.visibility = View.VISIBLE
            displayUserQRCode(ivQrCode)
        } else {
            tvIdTitle.visibility = View.GONE
            cvQrContainer.visibility = View.GONE
            tvQrLabel.visibility = View.GONE
        }

        loadAnnouncement()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle("Schedule Options")
        menu?.add(0, v?.id ?: 0, 0, "Set as Reminder")
        menu?.add(0, v?.id ?: 0, 0, "Show Exercise List")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this, "Action: ${item.title}", Toast.LENGTH_SHORT).show()
        return super.onContextItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                refreshHomeData()
                Toast.makeText(this, "Data Refreshed", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_about -> {
                Toast.makeText(this, "Arena Fitness v1.0", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTimePickerAndCheckIn() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val manualTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            performCheckIn(manualTime)
        }, hour, minute, true).show()
    }

    private fun displayUserQRCode(imageView: ImageView) {
        val qrData = "ARENA_FITNESS_USER_$userId"
        val bitmap = QRCodeHelper.generateQRCode(qrData)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun loadHeaderData() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        try {
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)
                val imageIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_IMAGE_URI)
                
                val name = if (nameIndex != -1) cursor.getString(nameIndex) ?: "User" else "User"
                val imageUriStr = if (imageIndex != -1) cursor.getString(imageIndex) else null
                
                tvName.text = "Hello, ${name.split(" ")[0]}!"
                if (!imageUriStr.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(imageUriStr)
                        ivProfile.setImageURI(uri)
                    } catch (e: Exception) {
                        ivProfile.setImageDrawable(null)
                    }
                }
            }
        } finally {
            cursor?.close()
        }

        val membershipQuery = "SELECT p.${DatabaseHelper.COLUMN_PLAN_NAME}, m.${DatabaseHelper.COLUMN_UM_END_DATE}, m.${DatabaseHelper.COLUMN_UM_STATUS} " +
                "FROM ${DatabaseHelper.TABLE_USER_MEMBERSHIPS} m " +
                "JOIN ${DatabaseHelper.TABLE_PLANS} p ON m.${DatabaseHelper.COLUMN_UM_PLAN_ID} = p.${DatabaseHelper.COLUMN_PLAN_ID} " +
                "WHERE m.${DatabaseHelper.COLUMN_UM_USER_ID} = ? " +
                "ORDER BY m.${DatabaseHelper.COLUMN_UM_ID} DESC LIMIT 1"
        
        val mCursor = db.rawQuery(membershipQuery, arrayOf(userId.toString()))
        try {
            if (mCursor != null && mCursor.moveToFirst()) {
                val planName = mCursor.getString(0) ?: "Unknown"
                val endDate = mCursor.getString(1) ?: "-"
                val status = mCursor.getString(2) ?: "none"
                membershipStatus = status

                when (status) {
                    "active" -> {
                        tvStatus.text = "$planName MEMBER • Expires: $endDate"
                        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_copper))
                        isMember = true
                    }
                    "pending" -> {
                        tvStatus.text = "PENDING VALIDASI KASIR"
                        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                        isMember = false
                    }
                    else -> {
                        tvStatus.text = "NON-MEMBER • INACTIVE"
                        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
                        isMember = false
                    }
                }
            } else {
                tvStatus.text = "NON-MEMBER • INACTIVE"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_gray))
                membershipStatus = "none"
                isMember = false
            }
        } finally {
            mCursor?.close()
        }
    }

    private fun loadAnnouncement() {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ANNOUNCEMENTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_ANN_ID} DESC",
            "1"
        )

        try {
            if (cursor != null && cursor.moveToFirst()) {
                val contentIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ANN_CONTENT)
                val dateIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ANN_DATE)
                
                val content = if (contentIndex != -1) cursor.getString(contentIndex) ?: "No announcements today." else "No announcements today."
                val date = if (dateIndex != -1) cursor.getString(dateIndex) ?: "-" else "-"
                
                tvAnnContent.text = content
                tvAnnDate.text = "Posted on: $date"
            }
        } finally {
            cursor?.close()
        }
    }

    private fun performCheckIn(manualTime: String? = null) {
        val db = dbHelper.writableDatabase
        val sdfDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = sdfDate.format(Date())
        val currentTime = manualTime ?: sdfTime.format(Date())

        val cursor = db.query(
            DatabaseHelper.TABLE_CHECK_INS,
            null,
            "${DatabaseHelper.COLUMN_CI_USER_ID} = ? AND ${DatabaseHelper.COLUMN_CI_DATE} = ?",
            arrayOf(userId.toString(), currentDate),
            null, null, null
        )

        try {
            if (cursor != null && cursor.count > 0) {
                Toast.makeText(this, "You have already checked-in today!", Toast.LENGTH_SHORT).show()
                return
            }
        } finally {
            cursor?.close()
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CI_USER_ID, userId)
            put(DatabaseHelper.COLUMN_CI_DATE, currentDate)
            put(DatabaseHelper.COLUMN_CI_TIME, currentTime)
        }

        val result = db.insert(DatabaseHelper.TABLE_CHECK_INS, null, values)
        if (result != -1L) {
            Toast.makeText(this, "Check-in Successful at $currentTime", Toast.LENGTH_SHORT).show()
            refreshHomeData()
        } else {
            Toast.makeText(this, "Check-in Failed", Toast.LENGTH_SHORT).show()
        }
    }
}
