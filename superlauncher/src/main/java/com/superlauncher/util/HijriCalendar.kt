package com.superlauncher.util

object HijriCalendar {
    private val monthNames = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    fun getHijriDate(): String {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val now = java.time.chrono.HijrahDate.now()
                val day = now.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                val month = now.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
                val year = now.get(java.time.temporal.ChronoField.YEAR)
                return "$day ${monthNames.getOrElse(month - 1) { "" }} $year هـ"
            }
        } catch (_: Throwable) {}
        val cal = java.util.Calendar.getInstance()
        val jd = ((cal.get(java.util.Calendar.YEAR) + 4716L) * 365.25).toLong() +
                ((cal.get(java.util.Calendar.MONTH) + 1) * 30.6).toLong() +
                cal.get(java.util.Calendar.DAY_OF_MONTH) - 43
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631)
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val m = ((24 * l3) / 709)
        val d = l3 - ((709 * m) / 24)
        val y = (30 * n + j - 30)
        return "${d.toInt()} ${monthNames.getOrElse(m.toInt() - 1) { "" }} ${y.toInt()} هـ"
    }
}
