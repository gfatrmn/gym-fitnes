package com.example.arenafitness

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MembershipActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var selectedProofUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedProofUri = it
            findViewById<ImageView>(R.id.ivPaymentProof).apply {
                setImageURI(it)
                setPadding(0, 0, 0, 0)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)

        dbHelper = DatabaseHelper(this)
        
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val llCurrent = findViewById<LinearLayout>(R.id.llCurrentMembership)
        val llSubscribe = findViewById<LinearLayout>(R.id.llSubscribeSection)
        val tvPlanName = findViewById<TextView>(R.id.tvCurrentPlanName)
        val tvPlanExpiry = findViewById<TextView>(R.id.tvCurrentPlanExpiry)
        val btnStop = findViewById<Button>(R.id.btnStopMembership)

        val rgPlans = findViewById<RadioGroup>(R.id.rgPlans)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribe)
        val cvPaymentProof = findViewById<androidx.cardview.widget.CardView>(R.id.cvPaymentProof)
        val spinnerPayment = findViewById<Spinner>(R.id.spinnerPayment)

        // Setup Payment Spinner with custom layout for black text
        val paymentMethods = resources.getStringArray(R.array.payment_methods)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, paymentMethods)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerPayment.adapter = adapter

        // Check current membership status
        loadMembershipStatus(llCurrent, llSubscribe, tvPlanName, tvPlanExpiry)

        cvPaymentProof.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubscribe.setOnClickListener {
            val selectedPlanId = rgPlans.checkedRadioButtonId
            if (selectedPlanId == -1) {
                Toast.makeText(this, "Silakan pilih membership terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedProofUri == null) {
                Toast.makeText(this, "Silakan unggah bukti pembayaran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val (planId, durationDays) = when (selectedPlanId) {
                R.id.rbDaily -> Pair(1, 1)
                R.id.rbMonthly -> Pair(2, 30)
                else -> Pair(2, 30)
            }
            processMembership(planId, durationDays)
        }

        btnStop.setOnClickListener {
            showStopMembershipDialog(tvPlanExpiry.text.toString())
        }

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

    private fun loadMembershipStatus(llCurrent: View, llSubscribe: View, tvName: TextView, tvExpiry: TextView) {
        val db = dbHelper.readableDatabase
        val query = "SELECT p.${DatabaseHelper.COLUMN_PLAN_NAME}, m.${DatabaseHelper.COLUMN_UM_END_DATE} " +
                "FROM ${DatabaseHelper.TABLE_USER_MEMBERSHIPS} m " +
                "JOIN ${DatabaseHelper.TABLE_PLANS} p ON m.${DatabaseHelper.COLUMN_UM_PLAN_ID} = p.${DatabaseHelper.COLUMN_PLAN_ID} " +
                "WHERE m.${DatabaseHelper.COLUMN_UM_USER_ID} = ? AND m.${DatabaseHelper.COLUMN_UM_STATUS} = 'active' " +
                "ORDER BY m.${DatabaseHelper.COLUMN_UM_ID} DESC LIMIT 1"
        
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            val name = cursor.getString(0)
            val expiry = cursor.getString(1)
            
            llCurrent.visibility = View.VISIBLE
            llSubscribe.visibility = View.GONE
            tvName.text = name
            tvExpiry.text = "Berakhir pada: $expiry"
        } else {
            llCurrent.visibility = View.GONE
            llSubscribe.visibility = View.VISIBLE
        }
        cursor.close()
    }

    private fun showStopMembershipDialog(expiryInfo: String) {
        AlertDialog.Builder(this)
            .setTitle("Berhenti Berlangganan?")
            .setMessage("Anda masih memiliki sisa waktu. $expiryInfo\n\nJika Anda berhenti sekarang, akses member akan segera dicabut. Apakah Anda yakin?")
            .setPositiveButton("YA, BERHENTI") { _, _ ->
                stopMembership()
            }
            .setNegativeButton("BATAL", null)
            .show()
    }

    private fun stopMembership() {
        val db = dbHelper.writableDatabase
        
        // 1. Update status membership menjadi 'stopped'
        val mValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UM_STATUS, "stopped")
        }
        db.update(DatabaseHelper.TABLE_USER_MEMBERSHIPS, mValues, 
            "${DatabaseHelper.COLUMN_UM_USER_ID} = ? AND ${DatabaseHelper.COLUMN_UM_STATUS} = 'active'", 
            arrayOf(userId.toString()))

        // 2. Update status is_member di table users menjadi 0 (Non-Member)
        val uValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 0)
        }
        db.update(DatabaseHelper.TABLE_USERS, uValues, 
            "${DatabaseHelper.COLUMN_USER_ID} = ?", 
            arrayOf(userId.toString()))

        Toast.makeText(this, "Membership telah dihentikan.", Toast.LENGTH_SHORT).show()
        
        // Refresh activity to show subscribe section again
        recreate()
    }

    private fun processMembership(planId: Int, duration: Int) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.DAY_OF_YEAR, duration)

        val startDateStr = sdf.format(startDate.time)
        val endDateStr = sdf.format(endDate.time)

        val membershipValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UM_USER_ID, userId)
            put(DatabaseHelper.COLUMN_UM_PLAN_ID, planId)
            put(DatabaseHelper.COLUMN_UM_START_DATE, startDateStr)
            put(DatabaseHelper.COLUMN_UM_END_DATE, endDateStr)
            put(DatabaseHelper.COLUMN_UM_STATUS, "active")
            put(DatabaseHelper.COLUMN_UM_PAYMENT_PROOF, selectedProofUri.toString())
        }

        val result = db.insert(DatabaseHelper.TABLE_USER_MEMBERSHIPS, null, membershipValues)

        if (result != -1L) {
            val userValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 1)
            }
            db.update(DatabaseHelper.TABLE_USERS, userValues, "${DatabaseHelper.COLUMN_USER_ID} = ?", arrayOf(userId.toString()))
            Toast.makeText(this, "Membership aktif sampai $endDateStr", Toast.LENGTH_LONG).show()
            recreate() // Refresh to show current membership
        } else {
            Toast.makeText(this, "Gagal mengaktifkan membership", Toast.LENGTH_SHORT).show()
        }
    }
}