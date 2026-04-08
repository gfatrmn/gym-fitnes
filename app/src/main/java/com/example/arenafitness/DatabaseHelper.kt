package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ArenaFitness.db"
        private const val DATABASE_VERSION = 5 // Naik ke versi 5 untuk fitur foto profil sekali ganti

        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "full_name"
        const val COLUMN_USER_PHONE = "phone"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_BIRTHDATE = "birthdate"
        const val COLUMN_USER_GENDER = "gender"
        const val COLUMN_USER_ADDRESS = "address"
        const val COLUMN_USER_IS_MEMBER = "is_member"
        const val COLUMN_USER_IMAGE_URI = "profile_image_uri" // Kolom baru untuk URI foto
        const val COLUMN_USER_PICTURE_LOCKED = "is_picture_locked" // Flag: 0 = bisa ganti, 1 = terkunci

        const val TABLE_PLANS = "membership_plans"
        const val COLUMN_PLAN_ID = "id"
        const val COLUMN_PLAN_NAME = "name"
        const val COLUMN_PLAN_PRICE = "price"
        const val COLUMN_PLAN_DURATION = "duration_days"

        const val TABLE_USER_MEMBERSHIPS = "user_memberships"
        const val COLUMN_UM_ID = "id"
        const val COLUMN_UM_USER_ID = "user_id"
        const val COLUMN_UM_PLAN_ID = "plan_id"
        const val COLUMN_UM_START_DATE = "start_date"
        const val COLUMN_UM_END_DATE = "end_date"
        const val COLUMN_UM_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_NAME TEXT, " +
                "$COLUMN_USER_PHONE TEXT, " +
                "$COLUMN_USER_EMAIL TEXT UNIQUE, " +
                "$COLUMN_USER_PASSWORD TEXT, " +
                "$COLUMN_USER_BIRTHDATE TEXT, " +
                "$COLUMN_USER_GENDER TEXT, " +
                "$COLUMN_USER_ADDRESS TEXT, " +
                "$COLUMN_USER_IMAGE_URI TEXT, " +
                "$COLUMN_USER_PICTURE_LOCKED INTEGER DEFAULT 0, " +
                "$COLUMN_USER_IS_MEMBER INTEGER DEFAULT 0)")
        
        val createPlansTable = ("CREATE TABLE $TABLE_PLANS (" +
                "$COLUMN_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_PLAN_NAME TEXT, " +
                "$COLUMN_PLAN_PRICE REAL, " +
                "$COLUMN_PLAN_DURATION INTEGER)")

        val createUserMembershipsTable = ("CREATE TABLE $TABLE_USER_MEMBERSHIPS (" +
                "$COLUMN_UM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_UM_USER_ID INTEGER, " +
                "$COLUMN_UM_PLAN_ID INTEGER, " +
                "$COLUMN_UM_START_DATE TEXT, " +
                "$COLUMN_UM_END_DATE TEXT, " +
                "$COLUMN_UM_STATUS TEXT, " +
                "FOREIGN KEY($COLUMN_UM_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID), " +
                "FOREIGN KEY($COLUMN_UM_PLAN_ID) REFERENCES $TABLE_PLANS($COLUMN_PLAN_ID))")

        db?.execSQL(createUsersTable)
        db?.execSQL(createPlansTable)
        db?.execSQL(createUserMembershipsTable)
        
        seedPlans(db)
        seedAdmin(db)
    }

    private fun seedPlans(db: SQLiteDatabase?) {
        val plans = arrayOf(
            arrayOf("Basic", 150000.0, 30),
            arrayOf("Premium", 400000.0, 90),
            arrayOf("VIP", 1200000.0, 365)
        )
        for (plan in plans) {
            val values = ContentValues().apply {
                put(COLUMN_PLAN_NAME, plan[0] as String)
                put(COLUMN_PLAN_PRICE, plan[1] as Double)
                put(COLUMN_PLAN_DURATION, plan[2] as Int)
            }
            db?.insert(TABLE_PLANS, null, values)
        }
    }

    private fun seedAdmin(db: SQLiteDatabase?) {
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, "Administrator")
            put(COLUMN_USER_PHONE, "081234567890")
            put(COLUMN_USER_EMAIL, "admin@arenafitness.com")
            put(COLUMN_USER_PASSWORD, "admin")
            put(COLUMN_USER_BIRTHDATE, "1990-01-01")
            put(COLUMN_USER_GENDER, "Laki-laki")
            put(COLUMN_USER_ADDRESS, "Arena Fitness HQ")
            put(COLUMN_USER_IS_MEMBER, 1)
            put(COLUMN_USER_PICTURE_LOCKED, 0)
        }
        db?.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER_MEMBERSHIPS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLANS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}