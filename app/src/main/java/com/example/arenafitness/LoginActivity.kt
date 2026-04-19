package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.arenafitness.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegisterLink = findViewById<TextView>(R.id.tvRegisterLink)
        val etEmail = findViewById<AutoCompleteTextView>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        setupEmailSuggestions(etEmail)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                btnLogin.isEnabled = false
                
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.instance.login(email, password)

                        if (response.isSuccessful && response.body()?.status == "success") {
                            val user = response.body()?.user
                            val userId = user?.id ?: -1
                            
                            // Simpan/Update data user ke SQLite lokal
                            user?.let {
                                saveUserToLocalDb(it)
                            }

                            saveEmailToSuggestions(email)

                            val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putInt("USER_ID", userId)
                                putString("USER_NAME", user?.name)
                                putString("USER_EMAIL", user?.email)
                                putBoolean("IS_LOGGED_IN", true)
                                apply()
                            }

                            Toast.makeText(this@LoginActivity, "Login Success!", Toast.LENGTH_SHORT).show()
                            
                            val intent = Intent(this@LoginActivity, HomeMemberActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            val msg = response.body()?.message ?: "Invalid Email or Password"
                            Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@LoginActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        btnLogin.isEnabled = true
                    }
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveUserToLocalDb(user: com.example.arenafitness.network.User) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_LOGIN, user.login)
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_PHONE, user.phone)
            put(DatabaseHelper.COLUMN_USER_ROLE, user.role)
            put(DatabaseHelper.COLUMN_USER_BIRTHDATE, user.birthDate)
            put(DatabaseHelper.COLUMN_USER_GENDER, user.gender)
            put(DatabaseHelper.COLUMN_USER_ADDRESS, user.address)
            put(DatabaseHelper.COLUMN_USER_IS_MEMBER, user.isMember)
        }
        
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_USERS, 
            values, 
            "${DatabaseHelper.COLUMN_USER_ID} = ?", 
            arrayOf(user.id.toString())
        )

        if (rowsAffected == 0) {
            values.put(DatabaseHelper.COLUMN_USER_ID, user.id)
            db.insert(DatabaseHelper.TABLE_USERS, null, values)
        }
    }

    private fun setupEmailSuggestions(etEmail: AutoCompleteTextView) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val savedEmails = sharedPref.getStringSet("SAVED_EMAILS", emptySet()) ?: emptySet()
        
        if (savedEmails.isNotEmpty()) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, savedEmails.toList())
            etEmail.setAdapter(adapter)
        }
    }

    private fun saveEmailToSuggestions(email: String) {
        val sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val savedEmails = sharedPref.getStringSet("SAVED_EMAILS", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        if (!savedEmails.contains(email)) {
            savedEmails.add(email)
            with(sharedPref.edit()) {
                putStringSet("SAVED_EMAILS", savedEmails)
                apply()
            }
        }
    }
}
