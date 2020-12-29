package com.appzonegroup.creditclub.pos.util

import com.creditclub.pos.DukptConfig
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.RequeryConfig

enum class PosMode(
    override val id: String,
    override val label: String,
    override val key1: String,
    override val key2: String,
    override val ip: String,
    override val port: Int,
    override val ssl: Boolean = true,
    override val dukptConfig: DukptConfig? = null,
    override val timeout: Int = 60,
    override val nodeName: String? = null,
    override val requeryConfig: RequeryConfig? = null,
) : RemoteConnectionInfo {

    EPMS(
        "EPMS",
        "EPMS LIVE",
        "3DFB3802940E8A546B3D38610852BA7A",
        "0234E39861D3405E7A6B3185BA675873",
        "196.6.103.73",
        5043
    ),

    EPMS_TEST(
        "EPMS_TEST",
        "EPMS Test",
        "5D25072F04832A2329D93E4F91BA23A2",
        "86CBCDE3B0A22354853E04521686863D",
        "196.6.103.72",
        55533
    ),

    POSVAS(
        "POSVAS",
        "POS VAS Live",
        "C48A1564CBFB3213A485BCE9195D321C",
        "FE169BD37C34B06215E919C4D3F75E79",
        "196.6.103.18",
        5023
    ),

    POSVAS_TEST(
        "POSVAS_TEST",
        "POS VAS Test",
        "C48A1564CBFB3213A485BCE9195D321C",
        "FE169BD37C34B06215E919C4D3F75E79",
        "196.6.103.10",
        55533
    )
}