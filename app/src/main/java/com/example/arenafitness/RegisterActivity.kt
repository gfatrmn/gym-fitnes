package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.arenafitness.network.RetrofitClient
import kotlinx.coroutines.launch

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

            if (phone.length < 11 || phone.length > 13) {
                Toast.makeText(this, "Phone number must be 11-13 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            lifecycleScope.launch {
                try {
                    Log.d("RegisterActivity", "Registering with: $name, $phone, $email")
                    
                    val response = RetrofitClient.instance.register(name, phone, email, pass)

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status?.lowercase() == "success") {
                            // Jika registrasi di server sukses, simpan ke database lokal
                            saveToLocalDatabase(name, phone, email, pass)

                            Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMsg = body?.message ?: "Registration Failed"
                            Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("RegisterActivity", "Response failed. Code: ${response.code()}, Error: $errorBody")
                        Toast.makeText(this@RegisterActivity, "Server Error: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("RegisterActivity", "Exception during register", e)
                    Toast.makeText(this@RegisterActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnRegister.isEnabled = true
                }
            }
        }

        tvLoginLink.setOnClickListener { finish() }
    }

    private fun saveToLocalDatabase(name: String, phone: String, email: String, pass: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, name)
            put(DatabaseHelper.COLUMN_USER_PHONE, phone)
            put(DatabaseHelper.COLUMN_USER_EMAIL, email)
            put(DatabaseHelper.COLUMN_USER_PASSWORD, pass)
            put(DatabaseHelper.COLUMN_USER_ROLE, "member")
            put(DatabaseHelper.COLUMN_USER_LOGIN, email.split("@")[0])
            put(DatabaseHelper.COLUMN_USER_IS_MEMBER, 0)
        }

        db.insert(DatabaseHelper.TABLE_USERS, null, values)
    }
}
