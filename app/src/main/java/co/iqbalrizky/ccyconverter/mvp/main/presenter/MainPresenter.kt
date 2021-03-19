package co.iqbalrizky.ccyconverter.mvp.main.presenter

import android.util.Log
import co.iqbalrizky.ccyconverter.api.ApiService
import co.iqbalrizky.ccyconverter.base.BasePresenter
import co.iqbalrizky.ccyconverter.mvp.main.view.MainView
import co.iqbalrizky.ccyconverter.myutils.Reused
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


private const val TAG = "Ace_MainPresenter"

class MainPresenter @Inject constructor() : BasePresenter(), Observer<ResponseBody>  {

    private val apiKey: String = "3c902870856760d1429a8cea"
    private lateinit var mView: MainView
    @Inject lateinit var apiService: ApiService

    override fun onSubscribe(d: Disposable?) {

    }

    override fun onNext(t: ResponseBody?) {
        onHandleResponse(t)
    }

    override fun onComplete() {

    }

    override fun onError(e: Throwable?) {

    }

    fun setView(view: MainView) {
        mView = view
    }

    /*request func*/
    fun grabApiData(pairParam: String) {
        val url: String = apiKey + pairParam
        val response: Observable<ResponseBody> = apiService.onGetAction(url)
        mSubscribe(response, this)
    }

    /*handle response*/
    private fun onHandleResponse(responseBody: ResponseBody?) {
        val rawString: String = responseBody?.string() ?: ""
        try {
            val joResp = JSONObject(rawString)
            val result: String = joResp.getString("result")
            if (result.equals("success", true)){
                joResp.remove("result")
                joResp.remove("documentation")
                joResp.remove("terms_of_use")

                val mapData: Map<String, Any>? = Reused.jsonObjectToMapObj(joResp)
                if (mapData != null){
                    mView.updatePairData(mapData)
                }
            } else if (result.equals("error", true)){
                val error: String = joResp.getString("error-type")
                //mView.onShowSnackBar(error, -1)
                mView.unSupportedConversion(error)
            }
        } catch (e: JSONException){
            e.printStackTrace()
        }

    }
}