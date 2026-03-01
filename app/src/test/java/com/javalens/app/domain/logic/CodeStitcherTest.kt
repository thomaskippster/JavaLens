package com.javalens.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class CodeStitcherTest {

    private val stitcher = CodeStitcher()

    @Test
    fun `stitch overlapping frames correctly`() {
        val frame1 = "public class Main {
    public static void main(String[] args) {
        System.out.println("Hello");"
        val frame2 = "        System.out.println("Hello");
    }
}"
        
        val expected = "public class Main {
    public static void main(String[] args) {
        System.out.println("Hello");
    }
}"
        val result = stitcher.stitch(frame1, frame2)
        
        assertEquals(expected, result)
    }

    @Test
    fun `stitch frames with no overlap correctly`() {
        val frame1 = "public class A {}"
        val frame2 = "public class B {}"
        
        val expected = "public class A {}
public class B {}"
        val result = stitcher.stitch(frame1, frame2)
        
        assertEquals(expected, result)
    }
}
