package org.khorum.oss.konstellation.dsl.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for the source-parsing companion object methods in [DomainConfig].
 * These are pure functions that don't require KSP mocks.
 */
class DomainConfigSourceParsingTest {

    // --- parseFunctionBody ---

    @Test
    fun `parseFunctionBody extracts expression body`() {
        val result = DomainConfig.parseFunctionBody("fun greet(): String = \"hello\"")
        assertEquals("\"hello\"", result)
    }

    @Test
    fun `parseFunctionBody extracts expression body with string interpolation`() {
        val result = DomainConfig.parseFunctionBody("fun designation(): String = \"\$name - \$rank\"")
        assertEquals("\"\$name - \$rank\"", result)
    }

    @Test
    fun `parseFunctionBody extracts block body`() {
        val result = DomainConfig.parseFunctionBody("fun greet(): String {\n    return name\n}")
        assertEquals("{\n    return name\n}", result)
    }

    @Test
    fun `parseFunctionBody returns null for empty source`() {
        assertNull(DomainConfig.parseFunctionBody(""))
    }

    @Test
    fun `parseFunctionBody returns null for incomplete function`() {
        assertNull(DomainConfig.parseFunctionBody("fun foo()"))
    }

    // --- findExpressionBodyStart ---

    @Test
    fun `findExpressionBodyStart finds equals after parens`() {
        val source = "fun foo(a: Int): String = \"hi\""
        val idx = DomainConfig.findExpressionBodyStart(source)
        assertNotNull(idx)
        assertEquals('=', source[idx!!])
    }

    @Test
    fun `findExpressionBodyStart returns null for block body`() {
        assertNull(DomainConfig.findExpressionBodyStart("fun foo() { return 1 }"))
    }

    @Test
    fun `findExpressionBodyStart skips equals inside parameter defaults`() {
        val source = "fun foo(a: Int = 5) = a + 1"
        val idx = DomainConfig.findExpressionBodyStart(source)!!
        // The returned index should point to the '=' after ')', not the one inside params
        assertTrue(idx > source.indexOf(')'))
    }

    @Test
    fun `findExpressionBodyStart ignores == operator`() {
        // Source has only == (equality) inside a block body, no expression body =
        assertNull(DomainConfig.findExpressionBodyStart("fun foo() { a == b }"))
    }

    @Test
    fun `findExpressionBodyStart returns null for source with no equals`() {
        assertNull(DomainConfig.findExpressionBodyStart("fun foo()"))
    }

    // --- extractExpressionBody ---

    @Test
    fun `extractExpressionBody extracts simple expression`() {
        val source = "fun foo() = 42"
        val eqIndex = source.indexOf(" = ") + 1
        assertEquals("42", DomainConfig.extractExpressionBody(source, eqIndex))
    }

    @Test
    fun `extractExpressionBody handles nested parens`() {
        val source = "fun foo() = bar(baz(1))"
        val eqIndex = source.indexOf(" = ") + 1
        assertEquals("bar(baz(1))", DomainConfig.extractExpressionBody(source, eqIndex))
    }

    @Test
    fun `extractExpressionBody returns null for empty expression`() {
        val source = "fun foo() ="
        val eqIndex = source.indexOf('=')
        assertNull(DomainConfig.extractExpressionBody(source, eqIndex))
    }

    @Test
    fun `extractExpressionBody stops at newline`() {
        val source = "fun foo() = 42\nfun bar() = 43"
        val eqIndex = source.indexOf(" = ") + 1
        assertEquals("42", DomainConfig.extractExpressionBody(source, eqIndex))
    }

    // --- extractBlockBody ---

    @Test
    fun `extractBlockBody extracts simple block`() {
        assertEquals("{ return 1 }", DomainConfig.extractBlockBody("fun foo() { return 1 }"))
    }

    @Test
    fun `extractBlockBody handles nested braces`() {
        assertEquals(
            "{ if (true) { 1 } else { 2 } }",
            DomainConfig.extractBlockBody("fun foo() { if (true) { 1 } else { 2 } }")
        )
    }

    @Test
    fun `extractBlockBody returns null when no braces`() {
        assertNull(DomainConfig.extractBlockBody("fun foo() = 42"))
    }

    @Test
    fun `extractBlockBody returns null for unbalanced braces`() {
        assertNull(DomainConfig.extractBlockBody("fun foo() { open"))
    }

    @Test
    fun `extractBlockBody handles multiline block`() {
        val source = "fun foo() {\n    val x = 1\n    return x\n}"
        val result = DomainConfig.extractBlockBody(source)
        assertNotNull(result)
        assertTrue(result!!.startsWith("{"))
        assertTrue(result.endsWith("}"))
        assertTrue(result.contains("val x = 1"))
    }

    // --- Branch coverage tests ---

    @Test
    fun `parseFunctionBody falls through to block body when expression body is empty`() {
        // "= " followed by newline yields null from extractExpressionBody, falls through to extractBlockBody
        val result = DomainConfig.parseFunctionBody("fun foo() = \n{ return 1 }")
        // extractExpressionBody returns null (whitespace only), then extractBlockBody finds the block
        assertEquals("{ return 1 }", result)
    }

    @Test
    fun `findExpressionBodyStart ignores brace inside parentheses`() {
        val source = "fun foo(body: () -> Unit = { println() }) = 42"
        val idx = DomainConfig.findExpressionBodyStart(source)!!
        // Should find the = before 42, not return null at the { inside parens
        assertTrue(idx > source.lastIndexOf(')'))
        assertEquals('=', source[idx])
    }

    @Test
    fun `extractExpressionBody stops at closing paren at depth 0`() {
        // The trailing ) at depth 0 is a terminator
        val source = "fun foo() = bar(1))"
        val eqIndex = source.indexOf(" = ") + 1
        assertEquals("bar(1)", DomainConfig.extractExpressionBody(source, eqIndex))
    }

    @Test
    fun `extractExpressionBody stops at closing brace at depth 0`() {
        val source = "fun foo() = 42}"
        val eqIndex = source.indexOf(" = ") + 1
        assertEquals("42", DomainConfig.extractExpressionBody(source, eqIndex))
    }

    @Test
    fun `extractExpressionBody handles nested braces in expression`() {
        val source = "fun foo() = mapOf(1 to { x -> x })\nnextLine"
        val eqIndex = source.indexOf(" = ") + 1
        // { increments depth, } decrements, \n at depth 0 terminates
        assertEquals("mapOf(1 to { x -> x })", DomainConfig.extractExpressionBody(source, eqIndex))
    }
}
