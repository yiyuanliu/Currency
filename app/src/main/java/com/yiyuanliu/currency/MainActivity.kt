package com.yiyuanliu.currency

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.style.TypefaceSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_display.*
import kotlinx.android.synthetic.main.layout_pad.*

/**
 * Created by yiyuan on 2016/10/3.
 */
class MainActivity: AppCompatActivity(), DataManager.Listener, Toolbar.OnMenuItemClickListener {

    companion object {
        val HINT_MONEY = 100f
    }

    lateinit var state: State
    lateinit var dataManager: DataManager

    val padListener = View.OnClickListener { v ->
        val num: Int = when(v.id) {
            R.id.digit_0 -> 0
            R.id.digit_1 -> 1
            R.id.digit_2 -> 2
            R.id.digit_3 -> 3
            R.id.digit_4 -> 4
            R.id.digit_5 -> 5
            R.id.digit_6 -> 6
            R.id.digit_7 -> 7
            R.id.digit_8 -> 8
            R.id.digit_9 -> 9
            R.id.digit_point -> -1
            R.id.op_del -> -2
            else -> throw RuntimeException()
        }
        onInput(num)
    }

    val deleteAllListener = View.OnLongClickListener { v ->
        onInput(-3)
        true
    }

    val selectListener = View.OnClickListener { v ->
        val position = when(v.id) {
            R.id.item1 -> 1
            R.id.item2 -> 2
            else -> throw RuntimeException()
        }
        onItemSelected(position)
    }

    val choseCurrencyListener = View.OnClickListener { v ->
        val which = when(v.id) {
            R.id.item1_icon -> 1
            R.id.item2_icon -> 2
            else -> throw RuntimeException()
        }
        chooseCurrency(which)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataManager = DataManager.getInstance(this)
        dataManager.addListener(this)

        //set listener to views
        val pad: Array<View> = arrayOf(digit_0, digit_1, digit_2, digit_3, digit_4,
                digit_5, digit_6, digit_7, digit_8, digit_9, digit_point, op_del)
        pad.forEach { it.setOnClickListener(padListener) }
        op_del.setOnLongClickListener(deleteAllListener)
        arrayOf(item1, item2).forEach { it.setOnClickListener(selectListener) }
        arrayOf(item1_icon, item2_icon).forEach { it.setOnClickListener(choseCurrencyListener) }

        if (savedInstanceState != null) {
            // restore state here
            state = savedInstanceState.getParcelable("state")
        } else {
            loadState()
        }

        // init view here
        updateCurrency()
        updateItemSelection(state.selected)
        updateMoney()

        toolbar.getChildAt(0).scaleX = 0.8f
        toolbar.inflateMenu(R.menu.menu_main)
        toolbar.getChildAt(toolbar.childCount - 1).backgroundTintList
        toolbar.setOnMenuItemClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        toolbar.getChildAt(0).animate().scaleX(1.0f).setDuration(500).setStartDelay(200).start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("state", state)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK)
            onCurrencyChosen(requestCode, data!!.getStringExtra("currency"))
    }

    override fun onStop() {
        super.onStop()
        saveState()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.refresh -> dataManager.loadFromNetwork()
            R.id.theme_blue -> setAppTheme(0)
            R.id.theme_red -> setAppTheme(1)
            R.id.theme_green -> setAppTheme(2)
            R.id.theme_teal -> setAppTheme(3)
        }

        return true
    }

    override fun onDataUpdate(state: Boolean) {
        if (state) updateMoney()
        else
            Toast.makeText(this, "load failed", Toast.LENGTH_SHORT).show()
    }

    fun onItemSelected(position: Int) {
        if (state.selected == position) {
            return
        }
        updateItemSelection(position)

        state.selected = position
        if (state.input.isNotBlank())
            state.input = state.toMoney.toDisplayStr()
        val temp = state.fromCurrency
        state.fromCurrency = state.toCurrency
        state.toCurrency = temp
        updateMoney()
    }

    fun chooseCurrency(position: Int) {
        val currency = if ((position == 1 && state.selected == 1) ||
                                (position == 2 && state.selected == 2)) {
                            state.fromCurrency
                        } else {
                            state.toCurrency
                        }
        val intent = CurrencyChooseActivity.getIntent(this, currency)
        startActivityForResult(intent, position)
    }

    fun onCurrencyChosen(position: Int, currency: String) {
        if ((position == 1 && state.selected == 1) ||
                (position == 2 && state.selected == 2)) {
            state.fromCurrency = currency
        } else {
            state.toCurrency = currency
        }

        updateCurrency()
    }

    fun onInput(num: Int) {
        Log.d("test", "input " + num)
        val after = when(num) {
            in 0..9 -> state.input.plus(num)
            -1 -> if (state.input.contains(".") || state.input == "") state.input
                else state.input.plus(".")
            -2 -> if (state.input.length > 0) state.input.dropLast(1)
                else state.input
            -3 -> ""
            else -> throw RuntimeException()
        }

        if (after == state.input) {
            return
        }
        if (after.isEmpty()) {
            // is all deleted
        } else if (after.length > 12) {
            // set max input length to 12
            return
        } else if (after.lastIndexOf('.') >= 0 && after.length - after.lastIndexOf('.') > 5) {
            return
        } else {
            try {
                after.toFloat()
            } catch (ex: Exception) {
                return
            }
        }

        state.input = after
        updateMoney()
    }

    fun updateCurrency() {
        val country1 = if (state.selected == 1) state.fromCurrency else state.toCurrency
        val country2 = if (state.selected == 2) state.fromCurrency else state.toCurrency
        item1_country.text = country1
        item2_country.text = country2
        item1_icon.setCountry(country1)
        item2_icon.setCountry(country2)
    }

    fun updateItemSelection(position: Int) {
        if (position == 1) {
            select2.scaleX = 0f
            select2.scaleY = 0f
            select1.animate().scaleX(1f).scaleY(1f).start()
        } else {
            select1.scaleX = 0f
            select1.scaleY = 0f
            select2.animate().scaleX(1f).scaleY(1f).start()
        }
    }

    fun updateMoney() {
        if (state.input.isNotBlank()) {
            // set data to real data
            state.fromMoney = state.input.toFloat()
        } else {
            // set data to hint data
            state.fromMoney = HINT_MONEY
        }

        // convert
        state.toMoney = dataManager.convert(state.fromCurrency, state.toCurrency, state.fromMoney)

        if (state.input.isBlank()) {
            // set hint
            item1_money.text = ""
            item2_money.text = ""
            item1_money.hint = if (state.selected == 1) state.fromMoney.toDisplayStr()
            else state.toMoney.toDisplayStr()
            item2_money.hint = if (state.selected == 1) state.toMoney.toDisplayStr()
            else state.fromMoney.toDisplayStr()
        } else {
            // set text
            item1_money.text = if (state.selected == 1) state.input else state.toMoney.toDisplayStr()
            item2_money.text = if (state.selected == 1) state.toMoney.toDisplayStr() else state.input
        }
    }

    data class State(var input: String, var selected: Int, var fromCurrency: String,
                     var fromMoney: Float, var toCurrency: String, var toMoney: Float): Parcelable {

        companion object {
            val CREATOR = object : Parcelable.Creator<State> {
                override fun newArray(size: Int): Array<out State?> {
                    return kotlin.arrayOfNulls<State>(size)
                }

                override fun createFromParcel(source: Parcel): State {
                    return State(
                        source.readString(),
                        source.readInt(),
                        source.readString(),
                        source.readFloat(),
                        source.readString(),
                        source.readFloat()
                    )
                }
            }
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(input)
            dest.writeInt(selected)
            dest.writeString(fromCurrency)
            dest.writeFloat(fromMoney)
            dest.writeString(toCurrency)
            dest.writeFloat(toMoney)
        }

        override fun describeContents(): Int {
            return 0
        }

        fun copy(state: State) {
            input = state.input
            selected = state.selected

        }
    }

    fun saveState() {
        val state = this.state
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString("input", state.input)
            putInt("selected", state.selected)
            putString("from", state.fromCurrency)
            putFloat("fromMoney", state.fromMoney)
            putString("to", state.toCurrency)
            putFloat("toMoney", state.toMoney)
            apply()
        }
    }

    fun loadState() {
        getPreferences(Context.MODE_PRIVATE).apply {
            val input = getString("input", "")
            val selected = getInt("selected", 1)
            val from = getString("from", "USD")
            val fromMoney = getFloat("fromMoney", 100F)
            val to = getString("to", "CNY")
            val toMoney = getFloat("toMoney", 100F)
            this@MainActivity.state = State(input, selected, from, fromMoney, to, toMoney)
        }
    }
}