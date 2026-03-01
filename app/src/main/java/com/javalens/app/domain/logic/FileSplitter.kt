package com.javalens.app.domain.logic

class FileSplitter {
    // Regex identifies: [modifier] (class|interface|enum) [Name]
    private val classPattern = Regex("""(?:public\s+|abstract\s+|final\s+)*(class|interface|enum)\s+([a-zA-Z0-9_]+)""")

    fun detectClassName(code: String): String? {
        val matchResult = classPattern.find(code)
        // Group 2 is the actual class/interface/enum name
        return matchResult?.groupValues?.get(2)
    }

    fun generateFileName(className: String?): String {
        return if (className != null) {
            "$className.java"
        } else {
            "Untitled_Scan_${System.currentTimeMillis() / 1000}.java"
        }
    }
}
