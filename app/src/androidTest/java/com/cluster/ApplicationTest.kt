package com.cluster

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cluster.pos.R as PosR
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationTest {
    private val appContext get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun useAppContext() {
        assertEquals(BuildConfig.APPLICATION_ID + ".test", appContext.packageName)
    }

    @Test
    fun appOverridesPosResources() {
        assertEquals("Card Withdrawal", appContext.getString(PosR.string.pos_card_purchase))
    }
}