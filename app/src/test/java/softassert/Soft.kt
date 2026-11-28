package softassert

// TEMPORARY probe: records assertion mismatches instead of throwing, so one CI run lists them all.

private fun where(): String {
    val st = Throwable().stackTrace
    val e = st.firstOrNull { !it.className.startsWith("softassert") } ?: return "?"
    val t = st.lastOrNull { it.className.startsWith("com.ganguydhrubo") && (it.fileName ?: "").endsWith("Test.kt") } ?: e
    return "${e.fileName}:${e.lineNumber}|${t.className.substringAfterLast('.')}.${t.methodName}"
}

private fun esc(v: Any?): String = v.toString().replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r")

private fun log(kind: String, msg: String?, expected: Any?, actual: Any?) {
    println("SOFT|${where()}|$kind|msg=${esc(msg)}|expected=${esc(expected)}|actual=${esc(actual)}")
}

fun assertEquals(expected: Any?, actual: Any?) { if (expected != actual) log("eq", null, expected, actual) }
fun assertEquals(message: String?, expected: Any?, actual: Any?) { if (expected != actual) log("eq", message, expected, actual) }
fun assertEquals(expected: Long, actual: Long) { if (expected != actual) log("eq", null, expected, actual) }
fun assertEquals(message: String?, expected: Long, actual: Long) { if (expected != actual) log("eq", message, expected, actual) }
fun assertEquals(expected: Double, actual: Double, delta: Double) = org.junit.Assert.assertEquals(expected, actual, delta)
fun assertEquals(message: String?, expected: Double, actual: Double, delta: Double) = org.junit.Assert.assertEquals(message, expected, actual, delta)
fun assertEquals(expected: Float, actual: Float, delta: Float) = org.junit.Assert.assertEquals(expected, actual, delta)
fun assertEquals(message: String?, expected: Float, actual: Float, delta: Float) = org.junit.Assert.assertEquals(message, expected, actual, delta)
fun assertTrue(condition: Boolean) { if (!condition) log("true", null, true, false) }
fun assertTrue(message: String?, condition: Boolean) { if (!condition) log("true", message, true, false) }
fun assertFalse(condition: Boolean) { if (condition) log("false", null, false, true) }
fun assertFalse(message: String?, condition: Boolean) { if (condition) log("false", message, false, true) }
fun assertNull(obj: Any?) { if (obj != null) log("null", null, null, obj) }
fun assertNull(message: String?, obj: Any?) { if (obj != null) log("null", message, null, obj) }

class SoftProbeTest {
    @org.junit.Test fun alwaysFailsSoReportsUpload() { org.junit.Assert.fail("soft-assert probe run") }
}
