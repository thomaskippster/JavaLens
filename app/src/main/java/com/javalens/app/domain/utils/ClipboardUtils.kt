package com.javalens.app.domain.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtils {
    fun copyToClipboard(context: Context, text: String, label: String = "JavaLens Code") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Code in Zwischenablage kopiert!", Toast.LENGTH_SHORT).show()
    }
}
