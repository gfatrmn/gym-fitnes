package com.example.arenafitness

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ArenaFitness.db"
        private const val DATABASE_VERSION = 13 // Updated to 13 to sync with CashierTransaction

        // Tabel Users
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_LOGIN = "login"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PHONE = "phone"
        const val COLUMN_USER_ROLE = "role"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_BIRTHDATE = "birth_date"
        const val COLUMN_USER_GENDER = "gender"
        const val COLUMN_USER_ADDRESS = "address"
        const val COLUMN_USER_IS_MEMBER = "is_member" // 0: Non-member, 1: Active, 2: Pending

        // Tabel Member (Synced with website)
        const val TABLE_GYM_MEMBERS = "gym_members"
        const val COLUMN_MEMBER_ID = "id"
        const val COLUMN_MEMBER_FULL_NAME = "full_name"
        const val COLUMN_MEMBER_PHONE = "phone"
        const val COLUMN_MEMBER_EMAIL = "email"
        const val COLUMN_MEMBER_STATUS = "member_status"
        const val COLUMN_MEMBER_PLAN = "membership_plan"
        const val COLUMN_MEMBER_EXPIRES = "expires_at"
        const val COLUMN_MEMBER_CODE = "checkin_code"

        // Tabel Cashier Transactions (Sync with Laravel CashierTransaction model)
        const val TABLE_CASHIER_TRANSACTIONS = "cashier_transactions"
        const val COLUMN_CT_ID = "id"
        const val COLUMN_CT_INVOICE = "invoice"
        const val COLUMN_CT_MEMBER_ID = "gym_member_id"
        const val COLUMN_CT_PRODUCT_ID = "product_id"
        const val COLUMN_CT_CUSTOMER_NAME = "customer_name"
        const val COLUMN_CT_GROUP = "transaction_group"
        const val COLUMN_CT_TYPE = "transaction_type"
        const val COLUMN_CT_AMOUNT = "amount"
        const val COLUMN_CT_QUANTITY = "quantity"
        const val COLUMN_CT_METHOD = "payment_method"
        const val COLUMN_CT_STATUS = "payment_status"
        const val COLUMN_CT_RECEIPT_STATUS = "receipt_status"
        const val COLUMN_CT_DATE = "transaction_at"
        const val COLUMN_CT_NOTES = "notes"

        // Local UI state
        const val COLUMN_USER_IMAGE_URI = "profile_image_uri"
        const val COLUMN_USER_PICTURE_LOCKED = "is_picture_locked"
        const val COLUMN_USER_PICTURE_CHANGE_COUNT = "picture_change_count"

        // Table Plans
        const val TABLE_PLANS = "plans"
        const val COLUMN_PLAN_ID = "id"
        const val COLUMN_PLAN_NAME = "name"

        // Table User Memberships
        const val TABLE_USER_MEMBERSHIPS = "user_memberships"
        const val COLUMN_UM_ID = "id"
        const val COLUMN_UM_USER_ID = "user_id"
        const val COLUMN_UM_PLAN_ID = "plan_id"
        const val COLUMN_UM_START_DATE = "start_date"
        const val COLUMN_UM_END_DATE = "end_date"
        const val COLUMN_UM_STATUS = "status" // active, pending, expired
        const val COLUMN_UM_PAYMENT_PROOF = "payment_proof"

        // Table Check-In
        const val TABLE_CHECK_INS = "gym_checkins"
        const val COLUMN_CI_ID = "id"
        const val COLUMN_CI_USER_ID = "user_id"
        const val COLUMN_CI_MEMBER_ID = "gym_member_id"
        const val COLUMN_CI_DATE = "checkin_date"
        const val COLUMN_CI_TIME = "checked_in_at"
        const val COLUMN_CI_METHOD = "checkin_method"

        // Table Announcements
        const val TABLE_ANNOUNCEMENTS = "announcements"
        const val COLUMN_ANN_ID = "id"
        const val COLUMN_ANN_TITLE = "title"
        const val COLUMN_ANN_BODY = "body"
        const val COLUMN_ANN_CONTENT = "body"
        const val COLUMN_ANN_DATE = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = ("CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_USER_NAME TEXT, " +
                "$COLUMN_USER_LOGIN TEXT, " +
                "$COLUMN_USER_EMAIL TEXT, " +
                "$COLUMN_USER_PHONE TEXT, " +
                "$COLUMN_USER_ROLE TEXT, " +
                "$COLUMN_USER_PASSWORD TEXT, " +
                "$COLUMN_USER_BIRTHDATE TEXT, " +
                "$COLUMN_USER_GENDER TEXT, " +
                "$COLUMN_USER_ADDRESS TEXT, " +
                "$COLUMN_USER_IMAGE_URI TEXT, " +
                "$COLUMN_USER_IS_MEMBER INTEGER DEFAULT 0, " +
                "$COLUMN_USER_PICTURE_LOCKED INTEGER DEFAULT 0, " +
                "$COLUMN_USER_PICTURE_CHANGE_COUNT INTEGER DEFAULT 0)")
        
        val createMembersTable = ("CREATE TABLE $TABLE_GYM_MEMBERS (" +
                "$COLUMN_MEMBER_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_MEMBER_FULL_NAME TEXT, " +
                "$COLUMN_MEMBER_PHONE TEXT, " +
                "$COLUMN_MEMBER_EMAIL TEXT, " +
                "$COLUMN_MEMBER_STATUS TEXT, " +
                "$COLUMN_MEMBER_PLAN TEXT, " +
                "$COLUMN_MEMBER_CODE TEXT, " +
                "$COLUMN_MEMBER_EXPIRES TEXT)")

        val createCashierTransactionsTable = ("CREATE TABLE $TABLE_CASHIER_TRANSACTIONS (" +
                "$COLUMN_CT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CT_INVOICE TEXT, " +
                "$COLUMN_CT_MEMBER_ID INTEGER, " +
                "$COLUMN_CT_PRODUCT_ID INTEGER, " +
                "$COLUMN_CT_CUSTOMER_NAME TEXT, " +
                "$COLUMN_CT_GROUP TEXT, " +
                "$COLUMN_CT_TYPE TEXT, " +
                "$COLUMN_CT_AMOUNT INTEGER, " +
                "$COLUMN_CT_QUANTITY INTEGER, " +
                "$COLUMN_CT_METHOD TEXT, " +
                "$COLUMN_CT_STATUS TEXT, " +
                "$COLUMN_CT_RECEIPT_STATUS TEXT, " +
                "$COLUMN_CT_DATE TEXT, " +
                "$COLUMN_CT_NOTES TEXT)")

        val createPlansTable = ("CREATE TABLE $TABLE_PLANS (" +
                "$COLUMN_PLAN_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_PLAN_NAME TEXT)")

        val createUserMembershipsTable = ("CREATE TABLE $TABLE_USER_MEMBERSHIPS (" +
                "$COLUMN_UM_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_UM_USER_ID INTEGER, " +
                "$COLUMN_UM_PLAN_ID INTEGER, " +
                "$COLUMN_UM_START_DATE TEXT, " +
                "$COLUMN_UM_END_DATE TEXT, " +
                "$COLUMN_UM_STATUS TEXT, " +
                "$COLUMN_UM_PAYMENT_PROOF TEXT)")

        val createCheckInsTable = ("CREATE TABLE $TABLE_CHECK_INS (" +
                "$COLUMN_CI_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CI_USER_ID INTEGER, " +
                "$COLUMN_CI_MEMBER_ID INTEGER, " +
                "$COLUMN_CI_DATE TEXT, " +
                "$COLUMN_CI_TIME TEXT, " +
                "$COLUMN_CI_METHOD TEXT)")

        val createAnnouncementsTable = ("CREATE TABLE $TABLE_ANNOUNCEMENTS (" +
                "$COLUMN_ANN_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_ANN_TITLE TEXT, " +
                "$COLUMN_ANN_BODY TEXT, " +
                "$COLUMN_ANN_DATE TEXT)")

        db?.execSQL(createUsersTable)
        db?.execSQL(createMembersTable)
        db?.execSQL(createCashierTransactionsTable)
        db?.execSQL(createPlansTable)
        db?.execSQL(createUserMembershipsTable)
        db?.execSQL(createCheckInsTable)
        db?.execSQL(createAnnouncementsTable)

        // Seed Plans
        db?.execSQL("INSERT INTO $TABLE_PLANS ($COLUMN_PLAN_ID, $COLUMN_PLAN_NAME) VALUES (1, 'DAILY')")
        db?.execSQL("INSERT INTO $TABLE_PLANS ($COLUMN_PLAN_ID, $COLUMN_PLAN_NAME) VALUES (2, 'MONTHLY')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ANNOUNCEMENTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CHECK_INS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER_MEMBERSHIPS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PLANS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CASHIER_TRANSACTIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_GYM_MEMBERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }
}
