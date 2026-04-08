package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase() // Gunakan trim dan lowercase
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val db = dbHelper.readableDatabase
                
                val cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    arrayOf(DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USER_IS_MEMBER),
                    "${DatabaseHelper.COLUMN_USER_EMAIL} = ? AND ${DatabaseHelper.COLUMN_USER_PASSWORD} = ?",
                    arrayOf(email, password),
                    null, null, null
                )

                if (cursor.moveToFirst()) {
                    val isMember = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_MEMBER))
                    val userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
                    
                    val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("USER_ID", userId)
                        apply()
                    }

                    Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                    
                    val intent = if (isMember == 1) {
                        Intent(this, HomeMemberActivity::class.java)
                    } else {
                        Intent(this, HomeNonMemberActivity::class.java)
                    }
                    
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                }
                cursor.close()
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}