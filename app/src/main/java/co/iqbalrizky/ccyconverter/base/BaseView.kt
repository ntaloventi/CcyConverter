package co.iqbalrizky.ccyconverter.base

import android.view.View

interface BaseView  {

    fun onShowKeyboard(view: View)
    fun onHideKeyboard(view: View)
    fun onFailInternet()
    fun onShowLog(msg: String)
    fun onShowSnackBar(msg: String, type: Int)

}