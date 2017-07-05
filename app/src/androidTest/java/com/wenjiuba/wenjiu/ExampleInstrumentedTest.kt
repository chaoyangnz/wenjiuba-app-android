package com.wenjiuba.wenjiu

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import com.wenjiuba.wenjiu.ui.PaymentInd

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.wenjiuba.wenjiu", appContext.packageName)

        val unpaid = Gson().toJson(PaymentInd.UNPAID)
        assertEquals("UNPAID", unpaid)

    }
}
