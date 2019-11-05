package com.appzonegroup.creditclub.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 7/5/2019.
 * Appzone Ltd
 */
object EmvErrorMessage {
    operator fun get(code: Int): String {
        return when (code) {
            -1 -> "Return code Error" // ERR_EMVRSP
            -2 -> "Application is locked" // ERR_APPBLOCK
            -3 -> "No emv app in card" // ERR_NOAPP
            -4 -> "User cancel" // ERR_USERCANCEL
            -5 -> "Timeout " // ERR_TIMEOUT
            -6 -> "Card data error" // ERR_EMVDATA
            -7 -> "Deal not accepted" // ERR_NOTACCEPT
            -8 -> "Trade refusal" // ERR_EMVDENIAL
            -9 -> "Key Expiration" // ERR_KEYEXP
            -10 -> "No pinpad or pinpad can’t use" // ERR_NOPINPAD
            -11 -> "No pin" // ERR_NOPIN
            -12 -> "Authentication Center Key Checksum error" // ERR_CAPKCHECKSUM
            -13 -> "The specified data or element was not found" // ERR_NOTFOUND
            -14 -> "The specified data element has no data" // ERR_NODATA
            -15 -> "Memory overflow" // ERR_OVERFLOW
            -16 -> "No log " // ERR_NOTRANSLOG
            -17 -> "NORECORD" // ERR_NORECORD
            -18 -> "Log item error" // ERR_NOLOGITEM
            -19 -> "IC reset fail" // ERR_ICCRESET
            -20 -> "IC command fail" // ERR_ICCCMD
            -21 -> "IC LOCK" // ERR_ICCBLOCK
            -22 -> "IC NO record" // ERR_ICCNORECORD
            -23 -> "GEN ACcommand return 6985" // ERR_GENAC1_6985
            -24 -> "NFC FAIL" // ERR_USECONTACT
            -25 -> "qPBOC app Expired" // ERR_APPEXP
            -125 -> "qPBOCapp Expired,need online" // ERR_APPEXP_ONLINE
            -26 -> "qPBOC Blacklist" // ERR_BLACKLIST
            -27 -> "err from GPO" // ERR_GPORSP
            -28 -> "NFC fail" // ERR_USEMAG
            -29 -> "NFC Transaction over limit" // ERR_TRANSEXCEEDED
            -30 -> "qPBOC fDDA FAIL" // ERR_QPBOCFDDAFAIL
            -31 -> "Offline PIN verification has exceeded the number of input" // ERR_OFFLINE_PIN_VERIFY_LIMIT
            -32 -> "Offline PIN Authentication failed" // ERR_OFFLINE_PIN_VERIFY_ERROR
            -33 -> "Recovery Card's RSA key failed" // ERR_RSA_RECOVERY
            -34 -> "Verifying the RSA key signature error for the recovery card" // ERR_SSA_SIGNATURE
            -35 -> "To verify the RSA key hash error for the recovery card" // ERR_HASH_VERIFIED
            -36 -> "Card Return Data decoding error" // ERR_BER_DECODE
            -37 -> "Certificate length error for recovery card" // ERR_KEY_SIZE_MISMATCH
            -38 -> "Can’t find key" // ERR_KEY_NOT_FOUND
            -39 -> "The terminal does not have a key certificate associated with it" // ERR_TERM_N0_CAPK
            -40 -> "The card's RSA is inconsistent with the pan" // ERR_RSA_PAN_ERROR
            -41 -> "The card's RSA certificate expires" // ERR_RSA_EXPIRED_ERROR
            -44 -> "Quics the card reader power off and delay 1500 milliseconds before the test card" // ERR_QVSDC_POWEROFF
            -45 -> "QVSDC constant power without delay again detection card" // ERR_QVSDC_REPOLLINGCARD
            -60 -> "Quics the card reader power off and delay 1500 milliseconds before the test card" // ERR_QUICS_POWEROFF
            -99 -> "Invalid data" // ERR_INVALID
            -1003 -> "NFC timeout" // NFC_TIMEOUT
            -1004 -> "NFC BB Conflict" // NFC_COLLISION_BB
            -1007 -> "NFC AB Conflict" // NFC_COLLISION_AB
            -1008 -> "NFC AA  Conflict" // NFC_COLLISION_AA
            else -> "An error occurred"
        }
    }
}