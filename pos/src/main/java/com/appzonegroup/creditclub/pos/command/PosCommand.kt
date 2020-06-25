package com.appzonegroup.creditclub.pos.command

import com.appzonegroup.creditclub.pos.contract.Logger
import org.koin.core.KoinComponent


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
abstract class PosCommand : Runnable, Logger, KoinComponent {
    override val tag: String = "PosCommand"
}