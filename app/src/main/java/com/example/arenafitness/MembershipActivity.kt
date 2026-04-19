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
import androidx.lifecycle.lifecycleScope
import com.example.arenafitness.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MembershipActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var selectedProofUri: Uri? = null

    private lateinit var llCurrent: LinearLayout
    private lateinit var llSubscribe: LinearLayout
    private lateinit var tvPlanName: TextView
    private lateinit var tvPlanExpiry: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivPaymentProof: ImageView
    private lateinit var spinnerPayment: Spinner

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = saveImageToInternalStorage(it)
            if (internalUri != null) {
                selectedProofUri = internalUri
                ivPaymentProof.apply {
                    setImageURI(internalUri)
                    setPadding(0, 0, 0, 0)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    imageTintList = null
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_membership)

        dbHelper = DatabaseHelper(this)
        
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        if (userId == -1) {
            Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        llCurrent = findViewById(R.id.llCurrentMembership)
        llSubscribe = findViewById(R.id.llSubscribeSection)
        tvPlanName = findViewById(R.id.tvCurrentPlanName)
        tvPlanExpiry = findViewById(R.id.tvCurrentPlanExpiry)
        tvUserEmail = findViewById(R.id.tvUserEmailMembership)
        ivPaymentProof = findViewById(R.id.ivPaymentProof)
        spinnerPayment = findViewById(R.id.spinnerPayment)
        
        val btnStop = findViewById<Button>(R.id.btnStopMembership)
        val rgPlans = findViewById<RadioGroup>(R.id.rgPlans)
        val btnSubscribe = findViewById<Button>(R.id.btnSubscribe)
        val cvPaymentProof = findViewById<androidx.cardview.widget.CardView>(R.id.cvPaymentProof)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)

        refreshUI()

        cvPaymentProof.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubscribe.setOnClickListener {
            val selectedPlanId = rgPlans.checkedRadioButtonId
            if (selectedPlanId == -1) {
                Toast.makeText(this, "Silakan pilih membership terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Silakan setujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedProofUri == null) {
                Toast.makeText(this, "Silakan unggah bukti pembayaran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(selectedPlanId)
            val planText = selectedRadioButton?.text?.toString()?.split(" - ")?.get(0) ?: "Plan"

            val (planId, durationDays) = when (selectedPlanId) {
                R.id.rbDaily -> Pair(1, 1)
                R.id.rbMonthly -> Pair(2, 30)
                else -> Pair(2, 30)
            }

            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Langganan")
                .setMessage("Apakah Anda yakin ingin berlangganan paket $planText? Status Anda akan menjadi PENDING menunggu validasi kasir.")
                .setPositiveButton("YA, PROSES") { _, _ ->
                    processMembership(planId, durationDays, planText)
                }
                .setNegativeButton("BATAL", null)
                .show()
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

    private fun refreshUI() {
        loadUserEmail(tvUserEmail)
        loadMembershipStatus()
    }

    private fun loadUserEmail(tvEmail: TextView) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_USER_EMAIL),
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val email = cursor.getString(0)
            tvEmail.text = email ?: "No Email"
        }
        cursor?.close()
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "payment_proof_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadMembershipStatus() {
        val db = dbHelper.readableDatabase
        val query = "SELECT p.${DatabaseHelper.COLUMN_PLAN_NAME}, m.${DatabaseHelper.COLUMN_UM_END_DATE}, m.${DatabaseHelper.COLUMN_UM_STATUS} " +
                "FROM ${DatabaseHelper.TABLE_USER_MEMBERSHIPS} m " +
                "JOIN ${DatabaseHelper.TABLE_PLANS} p ON m.${DatabaseHelper.COLUMN_UM_PLAN_ID} = p.${DatabaseHelper.COLUMN_PLAN_ID} " +
                "WHERE m.${DatabaseHelper.COLUMN_UM_USER_ID} = ? " +
                "ORDER BY m.${DatabaseHelper.COLUMN_UM_ID} DESC LIMIT 1"
        
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor != null && cursor.moveToFirst()) {
            val name = cursor.getString(0) ?: "Plan"
            val expiry = cursor.getString(1) ?: "-"
            val status = cursor.getString(2) ?: "pending"
            
            llCurrent.visibility = View.VISIBLE
            llSubscribe.visibility = View.GONE
            tvPlanName.text = name
            
            if (status == "pending") {
                tvPlanExpiry.text = "MENUNGGU VALIDASI KASIR"
                tvPlanExpiry.setTextColor(getColor(android.R.color.holo_orange_dark))
                findViewById<Button>(R.id.btnStopMembership).visibility = View.GONE
            } else if (status == "active") {
                tvPlanExpiry.text = "Berakhir pada: $expiry"
                tvPlanExpiry.setTextColor(getColor(R.color.accent_copper))
                findViewById<Button>(R.id.btnStopMembership).visibility = View.VISIBLE
            } else {
                tvPlanExpiry.text = "Status: $status"
                findViewById<Button>(R.id.btnStopMembership).visibility = View.VISIBLE
            }
        } else {
            llCurrent.visibility = View.GONE
            llSubscribe.visibility = View.VISIBLE
        }
        cursor?.close()
    }

    private fun showStopMembershipDialog(expiryInfo: String) {
        AlertDialog.Builder(this)
            .setTitle("Berhenti Berlangganan?")
            .setMessage("Apakah Anda yakin ingin menghentikan membership ini?")
            .setPositiveButton("YA, BERHENTI") { _, _ ->
                stopMembership()
            }
            .setNegativeButton("BATAL", null)
            .show()
    }

    private fun stopMembership() {
        val db = dbHelper.writableDatabase
        
        db.delete(DatabaseHelper.TABLE_USER_MEMBERSHIPS, 
            "${DatabaseHelper.COLUMN_UM_USER_ID} = ?", 
            arrayOf(userId.toString()))

        db.delete(DatabaseHelper.TABLE_GYM_MEMBERS, 
            "${DatabaseHelper.COLUMN_MEMBER_EMAIL} = (SELECT email FROM users WHERE id = ?)", 
            arrayOf(userId.toString()))

        val uValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 0)
        }
        db.update(DatabaseHelper.TABLE_USERS, uValues, 
            "${DatabaseHelper.COLUMN_USER_ID} = ?", 
            arrayOf(userId.toString()))

        Toast.makeText(this, "Membership telah dihentikan.", Toast.LENGTH_SHORT).show()
        refreshUI()
    }

    private fun processMembership(planId: Int, duration: Int, planName: String) {
        val db = dbHelper.writableDatabase
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.DAY_OF_YEAR, duration)

        val transactionAt = sdf.format(now.time)
        val startDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.time)
        val endDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate.time)
        
        val invoiceNum = "INV-" + System.currentTimeMillis()
        val nominal = when(planId) {
            1 -> 10000
            2 -> 100000
            else -> 0
        }
        val method = spinnerPayment.selectedItem.toString()

        // Get User Data for gym_members table sync
        var userName = "User"
        var userPhone = ""
        var userEmail = ""
        val userCursor = db.query(DatabaseHelper.TABLE_USERS, arrayOf(DatabaseHelper.COLUMN_USER_NAME, DatabaseHelper.COLUMN_USER_PHONE, DatabaseHelper.COLUMN_USER_EMAIL), "id = ?", arrayOf(userId.toString()), null, null, null)
        if (userCursor != null && userCursor.moveToFirst()) {
            userName = userCursor.getString(0) ?: "User"
            userPhone = userCursor.getString(1) ?: ""
            userEmail = userCursor.getString(2) ?: ""
        }
        userCursor?.close()

        // 1. Insert into local SQLite - Table cashier_transactions
        val transactionValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_CT_INVOICE, invoiceNum)
            put(DatabaseHelper.COLUMN_CT_CUSTOMER_NAME, userName)
            put(DatabaseHelper.COLUMN_CT_AMOUNT, nominal)
            put(DatabaseHelper.COLUMN_CT_QUANTITY, 1)
            put(DatabaseHelper.COLUMN_CT_METHOD, method)
            put(DatabaseHelper.COLUMN_CT_STATUS, "pending")
            put(DatabaseHelper.COLUMN_CT_TYPE, "membership_subscription")
            put(DatabaseHelper.COLUMN_CT_DATE, transactionAt)
            put(DatabaseHelper.COLUMN_CT_NOTES, "Subscription for $planName")
            put(DatabaseHelper.COLUMN_CT_GROUP, "membership")
        }
        db.insert(DatabaseHelper.TABLE_CASHIER_TRANSACTIONS, null, transactionValues)

        // 2. Insert into local SQLite - Table user_memberships
        val membershipValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UM_USER_ID, userId)
            put(DatabaseHelper.COLUMN_UM_PLAN_ID, planId)
            put(DatabaseHelper.COLUMN_UM_START_DATE, startDateStr)
            put(DatabaseHelper.COLUMN_UM_END_DATE, endDateStr)
            put(DatabaseHelper.COLUMN_UM_STATUS, "pending")
            put(DatabaseHelper.COLUMN_UM_PAYMENT_PROOF, selectedProofUri.toString())
        }
        db.insert(DatabaseHelper.TABLE_USER_MEMBERSHIPS, null, membershipValues)

        // 3. Insert into local SQLite - Table gym_members (Simulating Laravel Table)
        val gymMemberValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_MEMBER_FULL_NAME, userName)
            put(DatabaseHelper.COLUMN_MEMBER_PHONE, userPhone)
            put(DatabaseHelper.COLUMN_MEMBER_EMAIL, userEmail)
            put(DatabaseHelper.COLUMN_MEMBER_STATUS, "pending")
            put(DatabaseHelper.COLUMN_MEMBER_PLAN, planName)
            put(DatabaseHelper.COLUMN_MEMBER_EXPIRES, endDateStr)
            put(DatabaseHelper.COLUMN_MEMBER_CODE, "ARENA-" + userId)
        }
        // Check if member already exists in gym_members
        val existingMemberCursor = db.query(DatabaseHelper.TABLE_GYM_MEMBERS, null, "${DatabaseHelper.COLUMN_MEMBER_EMAIL} = ?", arrayOf(userEmail), null, null, null)
        if (existingMemberCursor != null && existingMemberCursor.count > 0) {
            db.update(DatabaseHelper.TABLE_GYM_MEMBERS, gymMemberValues, "${DatabaseHelper.COLUMN_MEMBER_EMAIL} = ?", arrayOf(userEmail))
        } else {
            db.insert(DatabaseHelper.TABLE_GYM_MEMBERS, null, gymMemberValues)
        }
        existingMemberCursor?.close()

        // 4. Update user status to Pending (2)
        val userStatusValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 2)
        }
        db.update(DatabaseHelper.TABLE_USERS, userStatusValues, "${DatabaseHelper.COLUMN_USER_ID} = ?", arrayOf(userId.toString()))

        // 5. Upload to Remote Server (Laravel API)
        lifecycleScope.launch {
            try {
                val planIdBody = planId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val planNameBody = planName.toRequestBody("text/plain".toMediaTypeOrNull())
                val amountBody = nominal.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val methodBody = method.toRequestBody("text/plain".toMediaTypeOrNull())
                val invoiceBody = invoiceNum.toRequestBody("text/plain".toMediaTypeOrNull())
                val notesBody = "Subscription for $planName".toRequestBody("text/plain".toMediaTypeOrNull())

                var imagePart: MultipartBody.Part? = null
                selectedProofUri?.let { uri ->
                    val file = File(uri.path!!)
                    if (file.exists()) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData("payment_proof", file.name, requestFile)
                    }
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.subscribe(
                        userId, planIdBody, planNameBody, amountBody, methodBody, invoiceBody, notesBody, imagePart
                    )
                }

                if (response.isSuccessful) {
                    Toast.makeText(this@MembershipActivity, "Berhasil! Silakan tunggu konfirmasi kasir di website.", Toast.LENGTH_LONG).show()
                    refreshUI()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    android.util.Log.e("SUBS_ERROR", "Server error: $errorBody")
                    // Still refresh UI because local DB was updated
                    refreshUI()
                    Toast.makeText(this@MembershipActivity, "Gagal sinkron server: $errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("SUBS_ERROR", "Exception: ${e.message}")
                refreshUI()
                Toast.makeText(this@MembershipActivity, "Error koneksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
