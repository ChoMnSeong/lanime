package com.ensnif.lanime.global.util

object LocaleUtils {
    fun parse(acceptLanguage: String?): String =
        acceptLanguage
            ?.split(",")
            ?.firstOrNull()
            ?.split("-", ";")
            ?.firstOrNull()
            ?.lowercase()
            ?.trim()
            ?: "ja"
}
