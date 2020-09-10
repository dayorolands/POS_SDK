package com.creditclub.pos.card


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/21/2019.
 * Appzone Ltd
 */
enum class CardReaderEvent(val code: Int, val message: String) {
    MAG_STRIPE(0, "MAG Card Detected"),
    CHIP(1, "IC Card Detected"),
    NFC(2, "NFC Card Detected"),
    HYBRID(3, "Hybrid Card Detected"),
    REMOVED(-1, "Card Removed"),
    CANCELLED(-2, "Cancelled"),
    CHIP_FAILURE(-3, "ICC card failure"),
    HYBRID_FAILURE(-4, "ICC card failure")
}