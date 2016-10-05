package com.yiyuanliu.currency

import android.content.Context
import android.util.Log
import java.util.*

/**
 * Created by yiyuan on 2016/10/2.
 */
object IconMap {
    private var names: Array<String>? = null
    private var icons: IntArray? = null

    fun getCountryNames(context: Context):Array<String> {
        if (names == null) {
            names = context.resources.getStringArray(R.array.country_names)
        }

        return names!!
    }

    fun getIcon(context: Context, country: String): Int {
        if (names == null) {
            names = context.resources.getStringArray(R.array.country_names)
        }

        if (icons == null) {
            val typedArray = context.resources.obtainTypedArray(R.array.icons)
            icons = IntArray(names!!.size)
            for (i in icons!!.indices) {
                icons!![i] = typedArray.getResourceId(i, 0)
            }
            typedArray.recycle()
        }

        for ((index, value) in names!!.withIndex()) {
            if (country == value){
                Log.d("text", "icon found for " + value + " " + icons!![index])
                return icons!![index]
            }
        }

        throw RuntimeException("no such icon in resource file")
    }
}