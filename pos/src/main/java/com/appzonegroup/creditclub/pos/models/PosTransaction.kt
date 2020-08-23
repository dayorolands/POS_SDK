package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.creditclub.core.serializer.TimeInstantSerializer
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.KoinComponent
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 31/01/2020.
 * Appzone Ltd
 */
@Entity
@Serializable
class PosTransaction {
    @PrimaryKey(autoGenerate = true)
    @SerialName("ID")
    var id = 0

    @SerialName("BankName")
    var bankName: String? = null

    @SerialName("AgentName")
    var agentName: String? = null

    @SerialName("AgentCode")
    var agentCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("PAN")
    var pan: String? = null

    @SerialName("TerminalID")
    var terminalId: String? = null

    @SerialName("TransactionType")
    var transactionType: String? = null

    @SerialName("STAN")
    var stan: String? = null

    @SerialName("Amount")
    var amount: String? = null

    @SerialName("CardType")
    var cardType: String? = null

    @SerialName("ExpiryDate")
    var expiryDate: String? = null

    @SerialName("ResponseCode")
    var responseCode: String? = null

    @SerialName("RetrievalReferenceNumber")
    var retrievalReferenceNumber: String? = null

    @SerialName("AppName")
    var appName: String? = null

    @SerialName("PTSP")
    var ptsp: String? = null

    @SerialName("Website")
    var website: String? = null

    @SerialName("MerchantDetails")
    var merchantDetails: String? = null

    @SerialName("MerchantID")
    var merchantId: String? = null

    @SerialName("CardHolder")
    var cardHolder: String? = null

    @SerialName("DateTime")
    @Serializable(with = TimeInstantSerializer::class)
    var dateTime: Instant? = Instant.now()

    @SerialName("IsASystemChange")
    var isASystemChange = true

    @SerialName("NodeName")
    var nodeName: String? = null

    @kotlinx.serialization.Transient
    var connectionInfo: ConnectionInfo? = null

    companion object : KoinComponent
}

@Dao
interface PosTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(posTransactions: List<PosTransaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(posTransaction: PosTransaction)

    @Update
    fun update(posTransaction: PosTransaction)

    @Delete
    fun delete(posTransaction: PosTransaction)

    @Query("DELETE FROM PosTransaction")
    fun deleteAll()

    @Query("Delete from PosTransaction where id >= :start AND ID <= :end")
    fun deleteRange(start: Int, end: Int)

    @Query("Delete from PosTransaction where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM PosTransaction")
    fun allAsync(): LiveData<List<PosTransaction>>

    @Query("SELECT * FROM PosTransaction")
    fun all(): List<PosTransaction>
}