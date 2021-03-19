package co.iqbalrizky.ccyconverter.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import co.iqbalrizky.ccyconverter.R
import co.iqbalrizky.ccyconverter.application.App
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var imm:InputMethodManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        onBindingView()
        onViewReady(savedInstanceState, intent)
    }

    @CallSuper
    protected open fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        onInjectDagger()
    }

    protected abstract fun onBindingView()
    protected abstract fun onInjectDagger()
    protected abstract fun onApplyFragment(fragId: Int, stack: Boolean)

    protected fun showKeyboard(view: View){
        imm.showSoftInput(view, 0)
    }

    protected fun hideKeyboard(view: View){
        try {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: NullPointerException){
            e.printStackTrace()
        }
    }

    protected fun showLog(tag: String, message: String) {
        val mLog: Boolean = (application as App).mLog
        if (mLog) {
            var dTag = "Ace_$tag"
            if (dTag.length >= 23) {
                dTag = dTag.substring(0, 20) + "..."
            }
            try {
                Log.d(dTag, message)
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    protected fun showSnackBar(view: View, message: String, type: Int) {
        try {
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            val snackView = snackbar.view
            val snackText = snackView.findViewById<TextView>(R.id.snackbar_text)
            var bgColor = ContextCompat.getColor(this, R.color.main)
            if (type == -1) {
                bgColor = ContextCompat.getColor(this, R.color.red)
            } else if (type == 1) {
                bgColor = ContextCompat.getColor(this, R.color.green)
            }
            snackView.setBackgroundColor(bgColor)
            snackText.setTextColor(resources.getColor(R.color.white))
            snackbar.show()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

}