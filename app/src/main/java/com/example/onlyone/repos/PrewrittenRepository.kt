package com.example.onlyone.prewritten

import android.content.Context
import androidx.annotation.ArrayRes
import com.example.onlyone.R
import java.util.Locale
import kotlin.math.min

enum class PreMsgCategory(val key: String, val labelRes: Int, @ArrayRes val arrayRes: Int) {
    GENERIC("generic", R.string.pre_msg_generic_tab, R.array.pre_msg_generic),
    EMPATHETIC("empathetic", R.string.pre_msg_empathetic_tab, R.array.pre_msg_empathetic),
    ENCOURAGING("encouraging", R.string.pre_msg_encouraging_tab, R.array.pre_msg_encouraging),
    CHEERFUL("cheerful", R.string.pre_msg_cheerful_tab, R.array.pre_msg_cheerful),
}

object PrewrittenRepository {

    fun categories(): List<PreMsgCategory> =
        listOf(
            PreMsgCategory.GENERIC,
            PreMsgCategory.EMPATHETIC,
            PreMsgCategory.ENCOURAGING,
            PreMsgCategory.CHEERFUL
        )

    /**
     * Load the messages for a category from strings.xml.
     * - Interpolates {name} (optional).
     * - Trims to maxLength (adds … if trimmed).
     * - Returns distinct results.
     */
    fun messagesFor(
        context: Context,
        category: PreMsgCategory,
        receiverName: String? = null,
        maxLength: Int = Int.MAX_VALUE
    ): List<String> {
        val arr = context.resources.getStringArray(category.arrayRes).toList()
        return arr.map { interpolateName(it, receiverName) }
            .map { trimToMax(it, maxLength) }
            .distinct()
    }

    private fun interpolateName(template: String, receiverName: String?): String {
        val name = receiverName?.takeIf { it.isNotBlank() } ?: ""
        return template.replace("{name}", name).trim()
    }

    private fun trimToMax(text: String, max: Int): String {
        if (text.length <= max) return text
        if (max < 1) return ""
        val cut = min(max, text.length)
        return if (cut >= 1) text.take(cut - 1) + "…" else ""
    }
}
