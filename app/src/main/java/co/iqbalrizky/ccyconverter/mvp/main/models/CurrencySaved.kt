package co.iqbalrizky.ccyconverter.mvp.main.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class CurrencySaved(

    @Id var id: Long = 0,
    var timeLastUpdateUnix: Int? = null,
    var timeNextUpdateUnix: Int? = null,
    var conversionRate: Double? = null,
    var baseCode: String? = null,
    var targetCode: String? = null,
    var timeLastUpdateUtc: String? = null,
    var timeNextUpdateUtc: String? = null,
    var userSrcAmount: String? = null,
    var userDestAmount: String? = null,
    var userTimeSaved: Int? = null

)
