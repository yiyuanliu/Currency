package com.yiyuanliu.currency

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.yiyuanliu.currency.theme.ColorTheme
import com.yiyuanliu.currency.theme.RedTheme
import org.jetbrains.anko.setContentView

/**
 * Created by yiyuan on 2016/10/3.
 */
class MainActivity: AppCompatActivity(), DataManager.Listener {

    companion object {
        val HINT_MONEY = 100f
    }

    lateinit var state: State
    lateinit var dataManager: DataManager
    lateinit var ui: MainActivityUi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = MainActivityUi(this)
        ui.setContentView(this)

        dataManager = DataManager.getInstance(this)
        dataManager.addListener(this)

        if (savedInstanceState != null) {
            // restore state here
            state = savedInstanceState.getParcelable("state")
        } else {
            loadState()
        }

        // init view here
        ui.updateCurrency(state)
        ui.updateSelection(state)
        ui.applyColorTheme(ColorTheme.getTheme(getAppTheme()))
        updateMoney()
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

    override fun onDataUpdate(state: Boolean) {
        if (state) updateMoney()
        else
            Toast.makeText(this, "load failed", Toast.LENGTH_SHORT).show()
        ui.showRefresh(false)
    }

    fun onItemSelected(position: Int) {
        if (state.selected == position) {
            return
        }
        state.selected = position
        ui.updateSelection(state)

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

        ui.updateCurrency(state)
        updateMoney()
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
        } else if (after.length > 10) {
            // set max input length to 10
            return
        } else if (after.lastIndexOf('.') >= 0 && after.length - after.lastIndexOf('.') > 3) {
            // num such as 0.123 is not allowed
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
        ui.updateMoney(state)
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