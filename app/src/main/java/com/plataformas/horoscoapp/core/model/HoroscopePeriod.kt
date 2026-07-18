package com.plataformas.horoscoapp.core.model

enum class HoroscopePeriod(
    val apiValue: String,
    val displayName: String,
) {
    Daily("daily", "Daily"),
    Weekly("weekly", "Weekly"),
    Monthly("monthly", "Monthly");

    companion object {
        fun from(value: String?): HoroscopePeriod? {
            return entries.firstOrNull { period ->
                period.apiValue.equals(value, ignoreCase = true) ||
                    period.displayName.equals(value, ignoreCase = true)
            }
        }
    }
}
