package com.example.arenafitness

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var selectedImageUri: Uri? = null
    private var isPictureLocked: Boolean = false
    private var userPassword: String? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = saveImageToInternalStorage(it)
            if (internalUri != null) {
                selectedImageUri = internalUri
                findViewById<ImageView>(R.id.ivEditProfilePicture).setImageURI(internalUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        dbHelper = DatabaseHelper(this)
        
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val etEditFullName = findViewById<EditText>(R.id.etEditFullName)
        val etEditEmail = findViewById<EditText>(R.id.etEditEmail)
        val etEditBirthDate = findViewById<EditText>(R.id.etEditBirthDate)
        val spinnerEditGender = findViewById<Spinner>(R.id.spinnerEditGender)
        val etEditAddress = findViewById<EditText>(R.id.etEditAddress)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)
        val cvEditProfile = findViewById<androidx.cardview.widget.CardView>(R.id.cvEditProfile)
        val ivEditProfilePicture = findViewById<ImageView>(R.id.ivEditProfilePicture)

        // Setup Gender Spinner
        val genders = arrayOf("Laki-laki", "Perempuan")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, genders)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerEditGender.adapter = adapter

        // Setup Date Picker
        etEditBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                etEditBirthDate.setText(date)
            }, year, month, day).show()
        }

        // Load Current Data
        loadUserData(etEditFullName, etEditEmail, etEditBirthDate, spinnerEditGender, etEditAddress, ivEditProfilePicture, genders)

        // Image Picking Logic
        cvEditProfile.setOnClickListener {
            if (isPictureLocked) {
                Toast.makeText(this, "Foto profil hanya dapat diganti satu kali saja", Toast.LENGTH_SHORT).show()
            } else {
                pickImageLauncher.launch("image/*")
            }
        }

        btnSaveProfile.setOnClickListener {
            val name = etEditFullName.text.toString().trim()
            val email = etEditEmail.text.toString().trim().lowercase()
            val birthDate = etEditBirthDate.text.toString()
            val gender = spinnerEditGender.selectedItem.toString()
            val address = etEditAddress.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                updateProfile(name, email, birthDate, gender, address)
            } else {
                Toast.makeText(this, "Nama dan Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_picture_${System.currentTimeMillis()}.jpg")
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

    private fun loadUserData(
        etName: EditText, 
        etEmail: EditText, 
        etBirth: EditText, 
        spinner: Spinner, 
        etAddr: EditText,
        ivPicture: ImageView,
        genders: Array<String>
    ) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)))
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)))
            etBirth.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BIRTHDATE)))
            etAddr.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ADDRESS)))
            
            userPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD))

            val imageUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IMAGE_URI))
            if (!imageUriStr.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(imageUriStr)
                    contentResolver.openInputStream(uri)?.close()
                    ivPicture.setImageURI(uri)
                } catch (e: Exception) {
                    ivPicture.setImageDrawable(null)
                }
            }

            isPictureLocked = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PICTURE_LOCKED)) == 1
            
            val gender = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GENDER))
            val displayGender = if (gender == "Male") "Laki-laki" else if (gender == "Female") "Perempuan" else gender
            val genderIndex = genders.indexOf(displayGender)
            if (genderIndex >= 0) spinner.setSelection(genderIndex)
        }
        cursor.close()
    }

    private fun updateProfile(name: String, email: String, birthDate: String, gender: String, address: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, name)
            put(DatabaseHelper.COLUMN_USER_EMAIL, email)
            put(DatabaseHelper.COLUMN_USER_BIRTHDATE, birthDate)
            put(DatabaseHelper.COLUMN_USER_GENDER, gender)
            put(DatabaseHelper.COLUMN_USER_ADDRESS, address)
            
            selectedImageUri?.let {
                put(DatabaseHelper.COLUMN_USER_IMAGE_URI, it.toString())
                put(DatabaseHelper.COLUMN_USER_PICTURE_LOCKED, 1)
            }
        }

        val rowsAffected = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString())
        )

        if (rowsAffected > 0) {
            Toast.makeText(this, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
        }
    }
}