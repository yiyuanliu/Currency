package com.yiyuanliu.currency.theme

/**
 * Created by yiyuan on 2016/10/6.
 */
interface ColorTheme {

    companion object {
        fun getTheme(theme: Int): ColorTheme = when(theme) {
            0 -> RedTheme
            1 -> GreenTheme
            2 -> BlueTheme
            3 -> TealTheme
            4 -> OrangeTheme
            5 -> GrayTheme
            else -> throw RuntimeException()
        }
    }
    val colorPrimary: Int
    val colorPrimaryDark: Int
    val colorAccent: Int
}