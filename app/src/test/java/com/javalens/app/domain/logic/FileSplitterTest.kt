package com.javalens.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class FileSplitterTest {

    private val splitter = FileSplitter()

    @Test
    fun `detect class name correctly`() {
        val code = "public class MyController {
    // some logic
}"
        val result = splitter.detectClassName(code)
        assertEquals("MyController", result)
    }

    @Test
    fun `detect interface name correctly`() {
        val code = "abstract interface NetworkApi {
    void send();
}"
        val result = splitter.detectClassName(code)
        assertEquals("NetworkApi", result)
    }

    @Test
    fun `generate correct file name`() {
        val className = "DatabaseHelper"
        val result = splitter.generateFileName(className)
        assertEquals("DatabaseHelper.java", result)
    }
}
