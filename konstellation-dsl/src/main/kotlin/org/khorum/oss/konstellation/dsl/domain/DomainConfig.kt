package org.khorum.oss.konstellation.dsl.domain

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import java.io.File

/**
 * Configuration for a domain in the DSL.
 * This class holds the necessary information to generate the DSL builder and related files.
 *
 * @param builderConfig The configuration for the DSL builder.
 * @param singleEntryTransformByClassName A map of class names to their corresponding
 *                                       KSClassDeclaration for single entry transformations.
 * @param domain The KSClassDeclaration representing the domain.
 * @property dslBuilderPostfix The postfix to be used for the DSL builder class name.
 * @property dslBuildFilePostfix The postfix to be used for the DSL build file class name.
 * @property packageName The package name of the domain.
 * @property typeName The simple name of the domain class.
 * @property domainClassName The ClassName of the domain class.
 * @property builderName The name of the DSL builder class.
 * @property builderClassName The ClassName of the DSL builder class.
 * @property dslBuilderInterface The ClassName of the DSL builder interface.
 * @property parameterizedDslBuilder The parameterized type name for the DSL builder interface with the domain class.
 * @property fileClassName The ClassName for the DSL build file.
 * @property dependencies The dependencies for the generated files, including the domain's source file.
 * @constructor Creates a DomainConfig instance with the specified parameters.
 */
open class DomainConfig(
    val builderConfig: BuilderConfig,
    val singleEntryTransformByClassName: Map<String, KSClassDeclaration>,
    var domain: KSClassDeclaration,
    val debug: Boolean
) {
    open val dslBuilderPostfix: String = "DslBuilder"
    open val dslBuildFilePostfix: String = "Dsl"
    val packageName = domain.packageName.asString()
    val typeName = domain.simpleName.asString()
    val domainClassName: ClassName = domain.toClassName()

    /**
     * The custom name override from `@GeneratedDsl(name = "...")`.
     * If provided and non-empty, this overrides the default builder name.
     */
    val customName: String? = resolveCustomName()

    /**
     * The base name used for the builder. If [customName] is provided, uses that;
     * otherwise falls back to the class type name.
     */
    val builderBaseName: String = customName ?: typeName

    val builderName = "${builderBaseName}$dslBuilderPostfix"
    val builderClassName = ClassName(packageName, builderName)
    val dslBuilderInterface = ClassName(builderConfig.dslBuilderClasspath, dslBuilderPostfix)
    val parameterizedDslBuilder = dslBuilderInterface.parameterizedBy(domainClassName)

    open val fileClassName = ClassName(packageName, "${builderBaseName}$dslBuildFilePostfix")
    val dependencies = Dependencies(aggregating = false, sources = listOfNotNull(domain.containingFile).toTypedArray())

    /**
     * Methods annotated with `@InjectDslMethod` on the domain class body.
     * These are copied into the generated builder.
     */
    val injectedMethods: List<InjectedMethod> = resolveInjectedMethods()

    private fun resolveCustomName(): String? {
        val annotation = AnnotationLookup.findAnnotation(domain.annotations, GeneratedDsl::class)
            ?: return null
        val name = AnnotationLookup.findArgumentValue<String>(annotation, GeneratedDsl::name.name)
        return name?.takeIf { it.isNotBlank() }
    }

    private fun resolveInjectedMethods(): List<InjectedMethod> {
        return domain.getDeclaredFunctions()
            .filter { AnnotationLookup.hasAnnotationByName(it.annotations, "InjectDslMethod") }
            .mapNotNull { fn -> extractInjectedMethod(fn) }
            .toList()
    }

    private fun extractInjectedMethod(fn: KSFunctionDeclaration): InjectedMethod? {
        val name = fn.simpleName.asString()
        val returnType = fn.returnType?.toTypeName() ?: return null
        val parameters = fn.parameters.mapNotNull { param ->
            val paramName = param.name?.asString() ?: return@mapNotNull null
            val paramType = param.type.toTypeName()
            InjectedMethodParameter(paramName, paramType)
        }
        val body = extractFunctionBody(fn) ?: return null

        return InjectedMethod(name, parameters, returnType, body)
    }

    @Suppress("ReturnCount")
    private fun extractFunctionBody(fn: KSFunctionDeclaration): String? {
        val location = fn.location as? FileLocation ?: return null
        val filePath = location.filePath
        val startLine = location.lineNumber // 1-based

        val sourceLines = try {
            File(filePath).readLines()
        } catch (_: Exception) {
            return null
        }

        if (startLine < 1 || startLine > sourceLines.size) return null

        // Find the function declaration line and extract its body.
        // Handles both expression-body ("= expr") and block-body ("{ ... }").
        val fnLines = sourceLines.subList(startLine - 1, sourceLines.size)
        val joined = fnLines.joinToString("\n")

        // Expression body: fun name(): Type = expr
        val eqIndex = findExpressionBodyStart(joined)
        if (eqIndex != null) {
            val exprBody = extractExpressionBody(joined, eqIndex)
            if (exprBody != null) return exprBody.trim()
        }

        // Block body: fun name(): Type { ... }
        return extractBlockBody(joined)?.trim()
    }

    /**
     * Finds the `=` that starts an expression body, skipping any `=` inside the parameter list.
     */
    private fun findExpressionBodyStart(source: String): Int? {
        var parenDepth = 0
        var i = 0
        // Skip past the fun keyword and parameter list
        while (i < source.length) {
            when (source[i]) {
                '(' -> parenDepth++
                ')' -> parenDepth--
                '=' -> if (parenDepth == 0 && i + 1 < source.length && source[i + 1] != '=') return i
                '{' -> if (parenDepth == 0) return null // block body, not expression
            }
            i++
        }
        return null
    }

    private fun extractExpressionBody(source: String, eqIndex: Int): String? {
        val afterEq = source.substring(eqIndex + 1)
        // The expression ends at the end of the logical statement.
        // For simple expressions, take until we hit an unbalanced newline or end of class body.
        var depth = 0
        val result = StringBuilder()
        for (ch in afterEq) {
            when (ch) {
                '(', '{' -> { depth++; result.append(ch) }
                ')', '}' -> {
                    if (depth == 0) break
                    depth--; result.append(ch)
                }
                '\n' -> {
                    if (depth == 0) break
                    result.append(ch)
                }
                else -> result.append(ch)
            }
        }
        val body = result.toString().trim()
        return body.ifEmpty { null }
    }

    private fun extractBlockBody(source: String): String? {
        val braceStart = source.indexOf('{')
        if (braceStart == -1) return null

        var depth = 0
        var i = braceStart
        while (i < source.length) {
            when (source[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return source.substring(braceStart, i + 1)
                    }
                }
            }
            i++
        }
        return null
    }
}
