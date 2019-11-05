package com.appzonegroup.creditclub.pos.command

import com.appzonegroup.creditclub.pos.contract.Logger


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
abstract class PosCommand : Runnable, Logger {
    override val tag: String = "PosCommand"
}