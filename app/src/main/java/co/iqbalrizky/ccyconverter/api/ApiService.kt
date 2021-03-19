package co.iqbalrizky.ccyconverter.api

import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {

    @GET
    fun onGetAction(@Url url:String): Observable<ResponseBody>

}