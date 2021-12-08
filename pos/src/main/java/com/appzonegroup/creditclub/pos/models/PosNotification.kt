package com.appzonegroup.creditclub.pos.models

import androidx.room.*
import com.cluster.core.util.format
import com.cluster.pos.model.ConnectionInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/14/2019.
 * Appzone Ltd
 */

@Entity
@Serializable
data class PosNotification(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @SerialName("TransactionReference")
    var transactionReference: String? = null,

    @SerialName("Amount")
    var amount: Double? = 0.00,

    @SerialName("Reference")
    var reference: String? = "",

    @SerialName("Currency")
    var currency: String? = "NGN",

    @SerialName("Type")
    var type: String? = "Invoice",

    @SerialName("CardScheme")
    var cardScheme: String? = null,

    @SerialName("StatusCode")
    @Required
    var statusCode: String? = null,

    @SerialName("PaymentDate")
    var paymentDate: String? = null,

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = null,

    @SerialName("MaskedPAN")
    var maskedPAN: String? = null,

    @SerialName("Nuban")
    var nuban: String? = "",

    @SerialName("TerminalID")
    var terminalId: String? = null,

    @SerialName("CustomerName")
    var customerName: String? = null,

    @SerialName("StatusDescription")
    var statusDescription: String? = null,

    @SerialName("AdditionalInformation")
    var additionalInformation: String? = "",

    @SerialName("NodeName")
    var nodeName: String? = null,

    @kotlinx.serialization.Transient
    var connectionInfo: ConnectionInfo? = null,
) {


    companion object {
        fun create(trn: FinancialTransaction): PosNotification {
            val transactionReference = trn.isoMsg.retrievalReferenceNumber37
            return PosNotification(
                transactionReference = transactionReference,
                type = trn.type,
                amount = trn.isoMsg.transactionAmount4?.toDouble()?.div(100),
                reference = "${trn.type}-$transactionReference",
                currency = "NGN",
                cardScheme = trn.cardType,
                statusCode = trn.isoMsg.responseCode39,
                paymentDate = trn.createdAt.format("dd-MM-yyyy hh:mm:ss"),
                retrievalReferenceNumber = trn.isoMsg.retrievalReferenceNumber37,
                maskedPAN = trn.pan,
                nuban = "",
                customerName = trn.cardHolder,
                statusDescription = trn.isoMsg.responseMessage,
                additionalInformation = "",
            )
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
    fun allAsync(): Flow<List<PosNotification>>

    @Query("SELECT * FROM PosNotification")
    suspend fun all(): List<PosNotification>

    @Query("SELECT COUNT(*) FROM PosNotification")
    suspend fun count(): Long
}