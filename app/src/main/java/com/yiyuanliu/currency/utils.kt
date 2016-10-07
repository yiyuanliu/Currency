package com.yiyuanliu.currency

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView

/**
 * Created by yiyuan on 2016/10/3.
 */
fun Float.toDisplayStr(): String {
    return String.format("%.2f", this)
}

fun ImageView.setCountry(country: String) {
    this.setImageResource(IconMap.getIcon(this.context, country))
}

fun Context.getAppTheme(): Int {
    val preference = this.getSharedPreferences("com.yiyuanliu.currency.theme", Context.MODE_PRIVATE)
    return preference.getInt("theme", 0)
}

fun Context.setAppTheme(theme: Int) {
    val preference = this.getSharedPreferences("com.yiyuanliu.currency.theme", Context.MODE_PRIVATE)
    preference.edit().putInt("theme", theme).apply()
}