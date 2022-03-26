package com.cluster.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cluster.core.util.mask
import com.cluster.pos.card.cardTransactionType
import com.cluster.pos.extension.*
import com.cluster.pos.extensions.hexBytes
import com.cluster.pos.model.ConnectionInfo
import com.cluster.pos.util.ISO87Packager
import com.cluster.pos.util.hexString
import org.jpos.iso.ISOMsg
import java.time.Instant

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/25/2019.
 * Appzone Ltd
 */

@Entity
open class FinancialTransaction {
    @PrimaryKey(autoGenerate = true)
    open var id: Int? = null

    var stan = ""
    var isSettled = false
    var content: String = ""
    var date = ""
    var cardHolder = ""
    var cardType = ""
    var aid = ""
    var rrn = ""
    var nodeName: String? = null
    var connectionInfo: ConnectionInfo? = null

    @Ignore
    @Transient
    var cardAcceptorNameLocation43 = ""

    @Ignore
    @Transient
    var merchantType18 = ""

    @Ignore
    @Transient
    var terminalId41 = ""

    @Ignore
    @Transient
    var cardExpirationDate14 = ""

    @Ignore
    @Transient
    var responseCode39 = ""

    @Ignore
    @Transient
    var additionalAmount = ""

    @Ignore
    @Transient
    var transactionAmount4 = ""

    @Ignore
    @Transient
    var cardAcceptorIdCode42 = ""

    var type = ""

    var pan = ""

    var createdAt: Instant = Instant.now()

    @delegate:Ignore
    @delegate:Transient
    val isoMsg by lazy {
        ISOMsg().apply {
            packager = ISO87Packager()
            unpack(content.replace(" ", "").hexBytes)
        }
    }

    constructor()

    constructor(msg: ISOMsg, cardType: String) {
        stan = msg.stan11 ?: ""
        date = msg.localTransactionDate13 ?: ""
        content = msg.pack().hexString
        type = cardTransactionType(msg).type
        pan = msg.pan.mask(6, 4)
        rrn = msg.retrievalReferenceNumber37 ?: ""

        cardAcceptorNameLocation43 = msg.cardAcceptorNameLocation43 ?: ""
        merchantType18 = msg.merchantType18 ?: ""
        terminalId41 = msg.terminalId41 ?: ""
        cardExpirationDate14 = msg.cardExpirationDate14 ?: ""
        responseCode39 = msg.responseCode39 ?: ""
        additionalAmount = msg.additionalAmounts54 ?: ""
        transactionAmount4 = msg.transactionAmount4 ?: ""
        this.cardType = cardType
    }
}

@Dao
interface FinancialTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(financialTransaction: FinancialTransaction)

    @Delete
    fun delete(financialTransaction: FinancialTransaction)

    @Query("DELETE FROM FinancialTransaction")
    fun deleteAll()

    @Query("SELECT * FROM FinancialTransaction WHERE createdAt BETWEEN :from AND :to ORDER by datetime(createdAt)")
    fun findAllInRange(from: Instant, to: Instant): List<FinancialTransaction>

    @Query("SELECT * FROM FinancialTransaction WHERE isSettled = 0")
    fun findAllUnsettled(): LiveData<List<FinancialTransaction>>

    @Query("SELECT * FROM FinancialTransaction ORDER BY id DESC LIMIT 1")
    fun last(): FinancialTransaction?

    @Query("SELECT * FROM FinancialTransaction WHERE stan = :stan")
    fun findByStan(stan: String): FinancialTransaction?

    @Query("SELECT * FROM FinancialTransaction WHERE rrn = :rrn")
    fun findByReference(rrn: String): FinancialTransaction?
}