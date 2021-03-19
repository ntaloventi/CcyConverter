package co.iqbalrizky.ccyconverter.mvp.splash.presenter

import co.iqbalrizky.ccyconverter.api.ApiService
import co.iqbalrizky.ccyconverter.base.BasePresenter
import co.iqbalrizky.ccyconverter.mvp.splash.view.SplashView
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.ResponseBody
import javax.inject.Inject

class SplashPresenter @Inject constructor() : BasePresenter(), Observer<ResponseBody> {

    private lateinit var mView:SplashView
    @Inject lateinit var apiService:ApiService

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: ResponseBody?) {

    }

    override fun onError(e: Throwable?) {

    }

    override fun onComplete() {

    }

    fun setView(view: SplashView){
        mView = view
    }
}