package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/14/2019.
 * Appzone Ltd
 */

@Entity
@Serializable
class IsoRequestLog {
    @delegate:Transient
    @delegate:Ignore
    private val paymentDateDf by lazy {
        DateTimeFormatter.ofPattern("dd-MM-YYYY hh:mm:ss").withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"))
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @SerializedName("Guid")
    var guid: String? = "2058HK58-00022224148-1031170618"

    @SerializedName("Amount")
    var amount: Double? = 0.00

    @SerializedName("Reference")
    var reference: String? = ""

    @SerializedName("Currency")
    var currency: String? = "NGN"

    @SerializedName("Type")
    var type: String? = "Invoice"

    @SerializedName("CardScheme")
    var cardScheme: String? = "Debit MasterCard"

    @SerializedName("StatusCode")
    var statusCode: String? = "00"

    @SerializedName("PaymentDate")
    var paymentDate: String? = "2018-10-31 17:06:18"

    @SerializedName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = "000220000148"

    @SerializedName("MaskedPAN")
    var maskedPAN: String? = "539983******9569"

    @SerializedName("Nuban")
    var nuban: String? = ""

    @SerializedName("CustomerName")
    var customerName: String? = "SAIDU/M"

    @SerializedName("StatusDescription")
    var statusDescription: String? = "Approved or completed successfully"

    @SerializedName("AdditionalInformation")
    var additionalInformation: String? = ""

    fun paymentDate(instant: Instant): String? {
        return paymentDateDf.format(instant)
    }
}

@Dao
interface IsoRequestLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(requestLog: List<IsoRequestLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(posNotification: IsoRequestLog)

    @Update
    fun update(posNotification: IsoRequestLog)

    @Delete
    fun delete(posNotification: IsoRequestLog)

    @Query("DELETE FROM IsoRequestLog")
    fun deleteAll()

    @Query("Delete from IsoRequestLog where id >= :start AND ID <= :end")
    fun deleteRange(start: Int, end: Int)

    @Query("Delete from IsoRequestLog where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM IsoRequestLog")
    fun allAsync(): LiveData<List<IsoRequestLog>>

    @Query("SELECT * FROM IsoRequestLog")
    fun all(): List<IsoRequestLog>
}