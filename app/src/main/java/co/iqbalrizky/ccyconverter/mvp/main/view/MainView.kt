package co.iqbalrizky.ccyconverter.mvp.main.view

import co.iqbalrizky.ccyconverter.base.BaseView

interface MainView : BaseView {

    fun updatePairData(mapData: Map<String, Any>)
    fun unSupportedConversion(error: String)

}