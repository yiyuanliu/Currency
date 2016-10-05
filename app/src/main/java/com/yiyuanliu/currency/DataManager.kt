package com.yiyuanliu.currency

import android.content.ContentValues
import android.content.Context
import android.icu.util.Currency
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by yiyuan on 2016/10/3.
 */
class DataManager(context: Context) {
    companion object {
        var instance: DataManager? = null
        fun getInstance(context: Context): DataManager {
            if (instance == null)
                instance = DataManager(context)

            return instance!!
        }
    }

    val dbHelper = DbHelper(context)
    val dataBase = dbHelper.writableDatabase
    val listeners = ArrayList<Listener>()

    val map = HashMap<String, Float>()

    val callback = object : Callback<CurrencyData>{
        override fun onFailure(call: Call<CurrencyData>?, t: Throwable?) {
            listeners.forEach { it.onDataUpdate(false) }
        }

        override fun onResponse(call: Call<CurrencyData>, response: Response<CurrencyData>) {
            val currencyData = response.body()
            for (item in currencyData.list.resources) {
                map.put(item.resource.fields.symbol.replace("=X", ""),
                        item.resource.fields.price.toFloat())
            }
            writeToSql(currencyData)
            listeners.forEach { it.onDataUpdate() }
        }
    }

    init {
        map.put("USD", 1f)
        loadFromDb()
        loadFromNetwork()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun convert(from: String, to: String, money: Float): Float {
        val fromRate = map[from]
        val toRate = map[to]

        if (fromRate == null || toRate == null) {
            return 0f
        } else {
            return money.div(fromRate).times(toRate)
        }
    }

    fun shouldUpdate(): Boolean {
        return true
    }

    fun loadFromNetwork() {
        CurrencyData.load(callback)
    }

    fun loadFromDb() {
        val cursor = dataBase.query(DbHelper.TABLE_NAME,
                arrayOf(DbHelper.COLUMN_CURRENCY, DbHelper.COLUMN_RATE),
                null, null, null, null, null)

        var hasNext = cursor.moveToNext()
        while (hasNext) {
            map.put(cursor.getString(0), cursor.getFloat(1))
            hasNext = cursor.moveToNext()
        }
        cursor.close()
        listeners.forEach { it.onDataUpdate() }
    }

    fun writeToSql(currencyData: CurrencyData) {
        dataBase.beginTransaction()
        dataBase.delete(DbHelper.TABLE_NAME, null, null)
        for (item in currencyData.list.resources) {
            val contentValue = ContentValues()
            contentValue.put(DbHelper.COLUMN_CURRENCY, item.resource.fields.symbol.replace("=X",""))
            contentValue.put(DbHelper.COLUMN_RATE, item.resource.fields.price)
            dataBase.insert(DbHelper.TABLE_NAME, null, contentValue)
        }
        dataBase.setTransactionSuccessful()
        dataBase.endTransaction()
    }

    interface Listener {
        fun onDataUpdate(state:Boolean = true)
    }

}