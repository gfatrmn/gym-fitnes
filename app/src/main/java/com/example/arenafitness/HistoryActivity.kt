package com.example.arenafitness

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = DatabaseHelper(this)
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("USER_ID", -1)

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)
        val tvCountMonth = findViewById<TextView>(R.id.tvCountMonth)
        val tvCountTotal = findViewById<TextView>(R.id.tvCountTotal)

        rvHistory.layoutManager = LinearLayoutManager(this)
        
        val historyList = getHistoryFromDatabase()
        
        // Update stats
        tvCountTotal.text = historyList.size.toString()
        tvCountMonth.text = getThisMonthCount(historyList).toString()

        if (historyList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
            rvHistory.adapter = HistoryAdapter(historyList)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_history

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeMemberActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_history -> true
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

    private fun getThisMonthCount(list: List<CheckInRecord>): Int {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        
        return list.count { 
            // format date biasanya YYYY-MM-DD
            try {
                val parts = it.date.split("-")
                if (parts.size >= 2) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    year == currentYear && month == currentMonth
                } else false
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun getHistoryFromDatabase(): List<CheckInRecord> {
        val list = mutableListOf<CheckInRecord>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CHECK_INS,
            null,
            "${DatabaseHelper.COLUMN_CI_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, "${DatabaseHelper.COLUMN_CI_DATE} DESC, ${DatabaseHelper.COLUMN_CI_TIME} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CI_ID))
                val uId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CI_USER_ID))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CI_DATE))
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CI_TIME))
                list.add(CheckInRecord(id, uId, date, time))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}