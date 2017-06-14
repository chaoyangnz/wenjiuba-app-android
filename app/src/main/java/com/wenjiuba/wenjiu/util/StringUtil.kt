package com.wenjiuba.wenjiu.util

import org.jsoup.Jsoup



/**
 * Created by richard on 3/11/17.
 */

object StringUtil {

    @JvmOverloads fun trim(str: String, maxLengh: Int = DEFAULT_TRIM_LENGTH): String {
        if (str.length <= maxLengh) return str

        return str.substring(0, maxLengh - 2) + ".."
    }

    val DEFAULT_TRIM_LENGTH = 30

    fun html2text(html: String): String {
        return Jsoup.parse(html).text()
    }
}
