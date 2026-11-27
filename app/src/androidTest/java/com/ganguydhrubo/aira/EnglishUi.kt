package com.ganguydhrubo.aira

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.ganguydhrubo.aira.i18n.AppLanguage
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Pins Aira's screens to English in device tests, because the assertions are written in English. From Android 13 it
 * switches the system's per-app language to English for the test and restores it afterwards. Put it outermost
 * (`@get:Rule(order = Int.MIN_VALUE)`) so it runs before any screen starts. Android 12 and older have no per-app
 * language, so run those tests on a phone set to English.
 */
class EnglishUi : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return base.evaluate()
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val english = { context.resources.getString(R.string.ui_locale) == AppLanguage.English.tag }
            if (english()) return base.evaluate()
            val lm = context.getSystemService(LocaleManager::class.java)
            val before = lm.applicationLocales
            lm.applicationLocales = LocaleList.forLanguageTags(AppLanguage.English.tag)
            waitFor(english)
            try {
                base.evaluate()
            } finally {
                lm.applicationLocales = before
            }
        }
    }

    private fun waitFor(ok: () -> Boolean) {
        val until = SystemClock.uptimeMillis() + 5_000
        while (!ok() && SystemClock.uptimeMillis() < until) SystemClock.sleep(50)
    }
}
