package com.yiyuanliu.currency

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.text.Layout
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewManager
import android.view.animation.LinearInterpolator
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.yiyuanliu.currency.theme.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.onMenuItemClick
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.custom.ankoView

/**
 * Created by yiyuan on 2016/10/6.
 */
class MainActivityUi(val context: MainActivity) : AnkoComponent<MainActivity> {

    lateinit private var toolbar: Toolbar
    lateinit private var displayLayout: LinearLayout
    lateinit private var digits: Array<AppCompatButton>
    lateinit private var dividers: Array<View>
    lateinit private var select1: View
    lateinit private var refresh: View
    lateinit private var select2: View
    lateinit private var currency1: TextView
    lateinit private var currency2: TextView
    lateinit private var currency1_icon: ImageView
    lateinit private var currency2_icon: ImageView
    lateinit private var money1: TextView
    lateinit private var money2: TextView

    private var anim: AnimatorSet? = null
    private var theme: ColorTheme = BlueTheme

    private val menuListener = {menu: MenuItem ->
        val theme = when(menu.itemId) {
            R.id.theme_red -> 0
            R.id.theme_green -> 1
            R.id.theme_blue -> 2
            R.id.theme_teal -> 3
            R.id.theme_orange -> 4
            R.id.theme_gray -> 5
            R.id.refresh -> -1
            R.id.view_all_theme -> -2
            else -> -3
        }
        if (theme in 0..5) {
            animToColor(ColorTheme.getTheme(theme))
            toolbar.context.setAppTheme(theme)
            demoAnim.pause()
        } else if (theme == -2) {
            toggleDemoAnim()
        } else if (theme == -1) {
            showRefresh(true)
            context.dataManager.loadFromNetwork()
        }
        true
    }

    private val demoAnim = ValueAnimator.ofInt(0, 5)
    private val demoAnimListener = ValueAnimator.AnimatorUpdateListener { anim ->
        val theme = ColorTheme.getTheme(anim.animatedValue as Int)
        if (theme != this.theme) {
            animToColor(theme)
        }
    }

    init {
        demoAnim.addUpdateListener(demoAnimListener)
        demoAnim.duration = 4000
        demoAnim.interpolator = LinearInterpolator()
    }

    fun applyColorTheme(toTheme: ColorTheme = theme) {
        theme = toTheme
        applyColorAccent()
        applyColorPrimary()
        applyColorPrimaryDark()
    }

    private fun applyColorPrimary(color: Int = theme.colorPrimary) {
        toolbar.backgroundColor = color
        displayLayout.backgroundColor = color
    }

    private fun applyColorPrimaryDark(color: Int = theme.colorPrimaryDark) {
        dividers.forEach { it.backgroundColor = color }
    }

    private fun applyColorAccent(color: Int = theme.colorAccent) {
        digits.forEach { it.textColor = color }
    }

    private fun toggleDemoAnim() {
        if (demoAnim.isRunning) {
            demoAnim.pause()
            toolbar.context.setAppTheme(demoAnim.animatedValue as Int)
        } else {
            demoAnim.start()
        }
    }

    fun animToColor(toTheme: ColorTheme) {
        anim?.end()
        val animPrimary = ValueAnimator
                .ofArgb(theme.colorPrimary, toTheme.colorPrimary)
                .setDuration(300)
        val animPrimaryDark = ValueAnimator
                .ofArgb(theme.colorPrimaryDark, toTheme.colorPrimaryDark).setDuration(300)
        val animAccent = ValueAnimator
                .ofArgb(theme.colorAccent, toTheme.colorAccent).setDuration(300)

        animPrimary.addUpdateListener { anim -> applyColorPrimary(anim.animatedValue as Int) }
        animPrimaryDark.addUpdateListener { anim -> applyColorPrimaryDark(anim.animatedValue as Int) }
        animAccent.addUpdateListener { anim -> applyColorAccent(anim.animatedValue as Int) }

        theme = toTheme
        anim = AnimatorSet()
        anim?.playTogether(animAccent, animPrimary, animPrimaryDark)
        anim?.start()
    }

    fun updateCurrency(state: MainActivity.State) {
        val currency_1 = if (state.selected == 1) state.fromCurrency else state.toCurrency
        val currency_2 = if (state.selected == 2) state.fromCurrency else state.toCurrency
        currency1.text = currency_1
        currency2.text = currency_2
        currency1_icon.setCountry(currency_1)
        currency2_icon.setCountry(currency_2)
    }

    fun updateSelection(state: MainActivity.State) {
        if (state.selected == 1) {
            select2.scaleX = 0f
            select2.scaleY = 0f
            select2.visibility = View.INVISIBLE
            select1.visibility = View.VISIBLE
            select1.animate().scaleX(1f).scaleY(1f).start()
        } else {
            select1.scaleX = 0f
            select1.scaleY = 0f
            select1.visibility = View.INVISIBLE
            select2.visibility = View.VISIBLE
            select2.animate().scaleX(1f).scaleY(1f).start()
        }
    }

    fun updateMoney(state: MainActivity.State) {
        if (state.input.isBlank()) {
            // set hint
            money1.text = ""
            money2.text = ""
            money1.hint = if (state.selected == 1) state.fromMoney.toDisplayStr()
            else state.toMoney.toDisplayStr()
            money2.hint = if (state.selected == 1) state.toMoney.toDisplayStr()
            else state.fromMoney.toDisplayStr()
        } else {
            // set text
            money1.text = if (state.selected == 1) state.input else state.toMoney.toDisplayStr()
            money2.text = if (state.selected == 1) state.toMoney.toDisplayStr() else state.input
        }
    }

    fun showRefresh(show: Boolean) {
        if (show) {
            refresh.visibility = View.VISIBLE
        } else {
            refresh.visibility = View.INVISIBLE
        }
    }

    fun selectableBackground(context: Context): Drawable {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()

        return drawable
    }

    fun selectableBackgroundBorderless(context: Context): Drawable {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackgroundBorderless))
        val drawable = typedArray.getDrawable(0)
        typedArray.recycle()

        return drawable
    }

    override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
        verticalLayout {
            toolbar = toolbar(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                title = "CURRENCY"
                titleMarginStart = dip(24)
                popupTheme = R.style.ThemeOverlay_AppCompat_Light
                inflateMenu(R.menu.menu_main)
                fitsSystemWindows = true
                setOnMenuItemClickListener(menuListener)
                setTitleTextAppearance(context, R.style.TitleAppearance)

                refresh = progressBar {
                    visibility = View.INVISIBLE
                    indeterminateDrawable?.setTint(0xffffff.opaque)
                }.lparams {
                    width = dip(24)
                    height = dip(24)
                }

            }.lparams {
                width = matchParent
            }

            val divider1 = view{}.lparams { width = matchParent; height = 1 }
            displayLayout = verticalLayout {
                linearLayout {
                    verticalPadding = dip(24)
                    rightPadding = dip(16)
                    background = selectableBackground(context)
                    fitsSystemWindows = true
                    onClick { ui.owner.onItemSelected(1); Log.d("test", "selected 1")}

                    select1 = imageView {
                        imageResource = R.drawable.selected_item
                        scaleType = ImageView.ScaleType.CENTER
                    }.lparams {
                        height = matchParent
                        width = dip(24)
                    }

                    currency1_icon = imageView { initIcon(1,  ui) }
                            .lparams { width = dip(48); height = dip(48); gravity = Gravity.CENTER_VERTICAL }
                    currency1 = appCompatText { initCurrency() }
                            .lparams{ leftMargin = dip(16); gravity = Gravity.CENTER_VERTICAL }
                    money1 = appCompatText { initMoney() }
                            .lparams{
                                leftMargin = dip(16)
                                gravity = Gravity.CENTER_VERTICAL
                                width = matchParent
                            }
                }.lparams {
                    width = matchParent
                }

                linearLayout {
                    verticalPadding = dip(24)
                    rightPadding = dip(24)
                    background = selectableBackground(context)
                    onClick { ui.owner.onItemSelected(2); Log.d("test", "selected 2") }

                    select2 = imageView {
                        imageResource = R.drawable.selected_item
                        scaleType = ImageView.ScaleType.CENTER
                    }.lparams {
                        height = matchParent
                        width = dip(24)
                    }

                    currency2_icon = imageView { initIcon(2, ui) }
                            .lparams { width = dip(48); height = dip(48); gravity = Gravity.CENTER_VERTICAL }
                    currency2 = appCompatText { initCurrency() }
                            .lparams{ leftMargin = dip(16); gravity = Gravity.CENTER_VERTICAL }
                    money2 = appCompatText { initMoney() }
                            .lparams{
                                leftMargin = dip(16)
                                gravity = Gravity.CENTER_VERTICAL
                                width = matchParent
                            }
                }.lparams {
                    width = matchParent
                }

            }.lparams {
                width = matchParent
            }

            val divider3 = view{}.lparams { width = matchParent; height = 1 }

            gridLayout{
                rowCount = 4
                columnCount = 3

                val digit7 = appCompatBt { init(7, ui) }.lparams { init(0, 0) }
                val digit8 = appCompatBt { init(8, ui) }.lparams { init(1, 0) }
                val digit9 = appCompatBt { init(9, ui) }.lparams { init(2, 0) }
                val digit4 = appCompatBt { init(6, ui) }.lparams { init(0, 1) }
                val digit5 = appCompatBt { init(5, ui) }.lparams { init(1, 1) }
                val digit6 = appCompatBt { init(4, ui) }.lparams { init(2, 1) }
                val digit1 = appCompatBt { init(1, ui) }.lparams { init(0, 2) }
                val digit2 = appCompatBt { init(2, ui) }.lparams { init(1, 2) }
                val digit3 = appCompatBt { init(3, ui) }.lparams { init(2, 2) }
                val digitPoint = appCompatBt { init(-1, ui) }.lparams { init(0, 3) }
                val digit0 = appCompatBt { init(0, ui) }.lparams { init(1, 3) }

                digits = arrayOf<AppCompatButton>(digit0, digit1, digit2, digit3,
                        digit4, digit5, digit6,
                        digit7, digit8, digit9, digitPoint)

                val del = imageButton {
                    imageResource = R.drawable.ic_keyboard_backspace_black_24dp
                    background = selectableBackgroundBorderless(context)
                }.lparams { init(2, 3) }

                del.onClick { ui.owner.onInput(-2) }
                del.onLongClick { ui.owner.onInput(-3); true }
            }.lparams {
                width = matchParent
                height = 0
                weight = 1f
            }
            dividers = arrayOf(divider1, divider3)
        }
    }

    private fun AppCompatButton.init(num: Int, ui: AnkoContext<MainActivity>) {
        background = selectableBackgroundBorderless(context)
        typeface = Typeface.create("serif-monospace", Typeface.NORMAL)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen(R.dimen.btTextSize).toFloat())
        textColor = 0xffffff.opaque
        onClick { ui.owner.onInput(num) }
        when(num) {
            in 0..9 -> text = num.toString()
            -1 -> text = "."
        }
    }

    private fun ImageView.initIcon(position: Int, ui: AnkoContext<MainActivity>) {
        background = selectableBackgroundBorderless(context)
        scaleType = ImageView.ScaleType.CENTER
        onClick { ui.owner.chooseCurrency(position) }
    }

    private fun AppCompatTextView.initMoney() {
        typeface = Typeface.create("serif-monospace", Typeface.NORMAL)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen(R.dimen.moneySize).toFloat())
        textColor = 0xffffff.opaque
        maxLines = 1
        textAlignment = AppCompatTextView.TEXT_ALIGNMENT_VIEW_END
        gravity = Gravity.END
    }

    private fun AppCompatTextView.initCurrency() {
        typeface = Typeface.create("serif-monospace", Typeface.NORMAL)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen(R.dimen.currencyNameSize).toFloat())
        textColor = 0xffffff.withAlpha(0xa0)
    }

    private fun GridLayout.LayoutParams.init(column: Int, row: Int) {
        height = 0
        width = 0
        columnSpec = GridLayout.spec(column, 1, 1f)
        rowSpec = GridLayout.spec(row, 1, 1f)
    }

    private inline fun ViewManager.appCompatBt(init: AppCompatButton.() -> Unit): AppCompatButton = ankoView(::AppCompatButton, 0, init)
    private inline fun ViewManager.appCompatText(init: AppCompatTextView.() -> Unit) = ankoView(::AppCompatTextView, 0, init)
}