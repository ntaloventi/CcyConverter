package co.iqbalrizky.ccyconverter.myutils

import android.content.Context
import co.iqbalrizky.ccyconverter.mvp.main.models.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox {

    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}