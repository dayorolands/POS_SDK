package com.creditclub.core.type


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 20/09/2019.
 * Appzone Ltd
 */
abstract class TransactionGroup(entries: List<TransactionType>) : List<TransactionType> by entries