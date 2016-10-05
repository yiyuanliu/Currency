package com.yiyuanliu.currency

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteCursorDriver
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQuery

/**
 * Created by yiyuan on 2016/10/3.
 */
class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        val DB_NAME = "currency"
        val TABLE_NAME = "currency_last"

        val COLUMN_CURRENCY = "currency"
        val COLUMN_RATE = "rate"

        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME ( $COLUMN_CURRENCY TEXT, $COLUMN_RATE REAL )"

    }

    override fun onCreate(p0: SQLiteDatabase) {
        p0.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(p0: SQLiteDatabase, p1: Int, p2: Int) {
        //do nothing
    }
}