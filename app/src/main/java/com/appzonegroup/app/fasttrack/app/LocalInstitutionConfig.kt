package com.appzonegroup.app.fasttrack.app

import com.creditclub.core.config.FlowConfig
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.type.TransactionType

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 17/10/2019.
 * Appzone Ltd
 */
object LocalInstitutionConfig : IInstitutionConfig {

    override var hasOnlineFunctions: Boolean = false

    override var transactionTypes: List<TransactionType> = TransactionType.values().toList()

    override var flows: FlowConfig = FlowConfig()
}