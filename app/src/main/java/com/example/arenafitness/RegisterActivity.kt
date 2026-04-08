package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnRegister.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val pass = etPassword.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length < 11 || phone.length > 12) {
                Toast.makeText(this, "Phone number must be 11-12 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = dbHelper.writableDatabase
            val cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                "${DatabaseHelper.COLUMN_USER_EMAIL} = ?",
                arrayOf(email),
                null, null, null
            )

            if (cursor.count > 0) {
                Toast.makeText(this, "Email already exists!", Toast.LENGTH_SHORT).show()
                cursor.close()
            } else {
                cursor.close()
                val values = ContentValues().apply {
                    put(DatabaseHelper.COLUMN_USER_NAME, name)
                    put(DatabaseHelper.COLUMN_USER_PHONE, phone)
                    put(DatabaseHelper.COLUMN_USER_EMAIL, email)
                    put(DatabaseHelper.COLUMN_USER_PASSWORD, pass)
                    put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 1)
                }

                val newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values)

                if (newRowId != -1L) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startDate = Calendar.getInstance()
                    val endDate = Calendar.getInstance()
                    endDate.add(Calendar.DAY_OF_YEAR, 30)

                    val membershipValues = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_UM_USER_ID, newRowId.toInt())
                        put(DatabaseHelper.COLUMN_UM_PLAN_ID, 1)
                        put(DatabaseHelper.COLUMN_UM_START_DATE, sdf.format(startDate.time))
                        put(DatabaseHelper.COLUMN_UM_END_DATE, sdf.format(endDate.time))
                        put(DatabaseHelper.COLUMN_UM_STATUS, "active")
                    }
                    db.insert(DatabaseHelper.TABLE_USER_MEMBERSHIPS, null, membershipValues)

                    // PERBAIKAN: Setelah sukses registrasi, pindah ke LoginActivity
                    Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}