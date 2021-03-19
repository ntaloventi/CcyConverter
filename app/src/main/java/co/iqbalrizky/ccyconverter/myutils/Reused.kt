package co.iqbalrizky.ccyconverter.myutils

import org.json.JSONException
import org.json.JSONObject
import java.util.*

object Reused {

    fun getCurrencies(): Array<String> {
        return arrayOf("USD", "IDR", "EUR", "GBP", "JPY")
    }

    fun jsonObjectToMapObj(jsonObject: JSONObject): Map<String, Any>? {
        val mapData: MutableMap<String, Any> = HashMap()
        try {
            val keys: Iterator<*> = jsonObject.keys()
            while (keys.hasNext()) {
                // loop to get the dynamic key
                val currentDynamicKey = keys.next() as String
                // get the value of the dynamic key
                val currentDynamicValue = jsonObject[currentDynamicKey]
                // do something here with the value...
                mapData[currentDynamicKey] = currentDynamicValue
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return mapData
    }

    fun swapResult(resultStr: String): String {
        val arrData: Array<String> = resultStr.split(" ").toTypedArray()
        var sourceAmo = arrData[0]
        val sourceCcy = arrData[1]
        var destAmo = arrData[3]
        val destCcy = arrData[4]

        //return "$destAmo $destCcy = $sourceAmo $sourceCcy"
        return destAmo.plus(" ").plus(destCcy).plus(" = ").plus(sourceAmo).plus(" ").plus(sourceCcy)
    }
}