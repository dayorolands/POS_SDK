package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.appzonegroup.creditclub.pos.card.CardIsoMsg
import com.appzonegroup.creditclub.pos.card.cardTransactionType
import com.appzonegroup.creditclub.pos.card.maskPan
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg
import com.appzonegroup.creditclub.pos.util.Misc
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/25/2019.
 * Appzone Ltd
 */

@Entity
open class FinancialTransaction {
    @PrimaryKey(autoGenerate = true)
    open var id: Int? = null

    @delegate:Ignore
    @delegate:Transient
    private val localDateDf by lazy {
        DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm").withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("+0100"))
    }

    var stan = ""
    var isSettled = false
    var content: String = ""
    var date = ""
    var cardHolder = ""
    var cardType = ""
    var aid = ""
    var rrn = ""

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
        CardIsoMsg().apply {
            unpack(content)
        }
    }

    constructor()

    constructor(isoMsg: BaseIsoMsg) {
        val msg = isoMsg.convert(::CardIsoMsg)

        stan = msg.stan11 ?: ""
        date = msg.localTransactionDate13 ?: ""
        content = Misc.toHexString(msg.pack())
        type = cardTransactionType(msg).type
        pan = maskPan(msg.pan)
        rrn = msg.retrievalReferenceNumber37 ?: ""

        cardAcceptorNameLocation43 = msg.cardAcceptorNameLocation43 ?: ""
        merchantType18 = msg.merchantType18 ?: ""
        terminalId41 = msg.terminalId41 ?: ""
        cardExpirationDate14 = msg.cardExpirationDate14 ?: ""
        responseCode39 = msg.responseCode39 ?: ""
        additionalAmount = msg.additionalAmount ?: ""
        transactionAmount4 = msg.transactionAmount4 ?: ""
    }

    @delegate:Ignore
    @delegate:Transient
    val prettyTime: String by lazy {
        localDateDf.format(createdAt)
    }
}

@Dao
interface FinancialTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(financialTransactions: List<FinancialTransaction>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(financialTransaction: FinancialTransaction)

    @Update
    fun update(financialTransaction: FinancialTransaction)

    @Delete
    fun delete(financialTransaction: FinancialTransaction)

    @Query("DELETE FROM FinancialTransaction")
    fun deleteAll()

    @Query("SELECT * FROM FinancialTransaction")
    fun findAll(): LiveData<List<FinancialTransaction>>

    @Query("SELECT * FROM FinancialTransaction WHERE createdAt BETWEEN :from AND :to ORDER by datetime(createdAt)")
    fun eodByDate(from: Instant, to: Instant): List<FinancialTransaction>

    @Query("SELECT * FROM FinancialTransaction WHERE isSettled = 0")
    fun unsettledTransactions(): LiveData<List<FinancialTransaction>>

    @Query("SELECT * FROM FinancialTransaction ORDER BY id DESC LIMIT 1")
    fun lastTransaction(): FinancialTransaction?

    @Query("SELECT * FROM FinancialTransaction WHERE stan = :stan")
    fun byStan(stan: String): FinancialTransaction?

    @Query("SELECT * FROM FinancialTransaction WHERE rrn = :rrn")
    fun byRRN(rrn: String): FinancialTransaction?

    @Query("SELECT * FROM FinancialTransaction WHERE date = :date LIMIT 200")
    fun byDate(date: String): List<FinancialTransaction>
}