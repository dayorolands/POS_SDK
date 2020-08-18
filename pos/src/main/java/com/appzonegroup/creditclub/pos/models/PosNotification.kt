package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.creditclub.core.util.format
import com.creditclub.pos.model.ConnectionInfo
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/14/2019.
 * Appzone Ltd
 */

@Entity
class PosNotification {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @SerializedName("TransactionReference")
    var transactionReference: String? = "2058HK58-00022224148-1031170618"

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

    @SerialName("NodeName")
    var nodeName: String? = null

    @kotlinx.serialization.Transient
    var connectionInfo: ConnectionInfo? = null

    companion object {
        fun create(trn: FinancialTransaction): PosNotification {
            return PosNotification().apply {
                id = trn.stan.toInt()
                transactionReference = trn.isoMsg.retrievalReferenceNumber37
                type = trn.type
                amount = trn.isoMsg.transactionAmount4?.toDouble()?.div(100)
                reference = "$type-$transactionReference"
                currency = "NGN"
                cardScheme = trn.cardType
                statusCode = trn.isoMsg.responseCode39
                paymentDate = trn.createdAt.format("dd-MM-YYYY hh:mm:ss")
                retrievalReferenceNumber = trn.isoMsg.retrievalReferenceNumber37
                maskedPAN = trn.pan
                nuban = ""
                customerName = trn.cardHolder
                statusDescription = trn.isoMsg.responseMessage
                additionalInformation = ""
            }
        }
    }
}

@Dao
interface PosNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(posNotifications: List<PosNotification>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(posNotification: PosNotification)

    @Update
    fun update(posNotification: PosNotification)

    @Delete
    fun delete(posNotification: PosNotification)

    @Query("DELETE FROM PosNotification")
    fun deleteAll()

    @Query("Delete from PosNotification where id >= :start AND ID <= :end")
    fun deleteRange(start: Int, end: Int)

    @Query("Delete from PosNotification where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM PosNotification")
    fun allAsync(): LiveData<List<PosNotification>>

    @Query("SELECT * FROM PosNotification")
    fun all(): List<PosNotification>
}