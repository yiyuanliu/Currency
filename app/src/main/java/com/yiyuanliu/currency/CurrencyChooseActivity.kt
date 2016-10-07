package com.yiyuanliu.currency

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_currency_chose.*

class CurrencyChooseActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context, currency: String): Intent {
            val intent = Intent(context, CurrencyChooseActivity::class.java)
            intent.putExtra("currency", currency)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_chose)

        val currency = intent.extras.getString("currency")
        toolbar.title = currency
        //toolbar.setNavigationIcon(IconMap.getIcon(this, currency))
        //toolbar.subtitle = "chose a currency to replace $currency"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        recycler_view.layoutManager = GridLayoutManager(this, 3)
        recycler_view.adapter = CurrencyAdapter(this, object : Listener{
            override fun onCurrencyChosen(currency: String) {
                val result = Intent()
                result.putExtra("currency", currency)
                setResult(RESULT_OK, result)
                finish()
            }
        })
    }

    class CurrencyAdapter(context: Context, val listener: Listener) : RecyclerView.Adapter<CurrencyItemView>() {

        val currencyList = IconMap.getCountryNames(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemView {
            val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_currency, parent, false)
            return CurrencyItemView(itemView, listener)
        }

        override fun onBindViewHolder(holder: CurrencyItemView, position: Int) {
            holder.bind(currencyList[position])
        }

        override fun getItemCount(): Int = currencyList.size
    }

    class CurrencyItemView(itemView: View, val listener: Listener) : RecyclerView.ViewHolder(itemView) {
        fun bind(currency: String) {
            val icon = itemView.findViewById(R.id.currency_icon) as ImageView
            val name = itemView.findViewById(R.id.currency_name) as TextView
            name.text = currency
            icon.setCountry(currency)
            itemView.setOnClickListener{ listener.onCurrencyChosen(currency) }
        }
    }

    interface Listener {
        fun onCurrencyChosen(currency: String)
    }
}
