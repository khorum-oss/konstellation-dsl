package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim

class DefaultPropertyAnnotationExtractorTest : UnitSim() {

    companion object {
        private fun mockKSName(value: String): KSName {
            val name: KSName = mockk()
            every { name.asString() } returns value
            every { name.getShortName() } returns value
            return name
        }

        private fun mockAnnotation(
            simpleName: String,
            args: List<Pair<String, Any?>> = emptyList()
        ): KSAnnotation {
            val ann: KSAnnotation = mockk()
            every { ann.shortName } returns mockKSName(simpleName)
            every { ann.arguments } returns args.map { (k, v) ->
                val argName: KSName = mockk()
                every { argName.asString() } returns k
                val arg: KSValueArgument = mockk()
                every { arg.name } returns argName
                every { arg.value } returns v
                arg
            }
            return ann
        }
    }

    @Test
    fun `extract with no annotations returns default metadata`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { false }
            whenever { result.isTransient }
        }
    }

    @Test
    fun `extract with no annotations returns null description`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { null }
            whenever { result.description }
        }
    }

    @Test
    fun `extract with no annotations returns empty aliases`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { emptyList<String>() }
            whenever { result.aliases }
        }
    }

    @Test
    fun `extract TransientDsl with reason`() = test {
        given {
            val ann = mockAnnotation("TransientDsl", listOf("reason" to "internal only"))
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.isTransient && result.transientReason == "internal only" }
        }
    }

    @Test
    fun `extract TransientDsl with blank reason returns null reason`() = test {
        given {
            val ann = mockAnnotation("TransientDsl", listOf("reason" to "  "))
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.isTransient && result.transientReason == null }
        }
    }

    @Test
    fun `extract DslDescription with value`() = test {
        given {
            val ann = mockAnnotation("DslDescription", listOf("value" to "Ship name"))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { "Ship name" }
            whenever { extractor.extract(sequenceOf(ann)).description }
        }
    }

    @Test
    fun `extract DslDescription with blank value returns null`() = test {
        given {
            val ann = mockAnnotation("DslDescription", listOf("value" to ""))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { null }
            whenever { extractor.extract(sequenceOf(ann)).description }
        }
    }

    @Test
    fun `extract DslAlias with single string value`() = test {
        given {
            val ann = mockAnnotation("DslAlias", listOf("names" to "alias1"))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { listOf("alias1") }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    @Test
    fun `extract DslAlias with blank string returns empty list`() = test {
        given {
            val ann = mockAnnotation("DslAlias", listOf("names" to ""))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { emptyList<String>() }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    @Test
    fun `extract DslAlias with list value`() = test {
        given {
            val ann = mockAnnotation("DslAlias", listOf("names" to listOf("a", "b")))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { listOf("a", "b") }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    @Test
    fun `extract DslAlias with list containing blanks filters them`() = test {
        given {
            val ann = mockAnnotation("DslAlias", listOf("names" to listOf("a", "", "b")))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { listOf("a", "b") }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    @Test
    fun `extract DslAlias with unexpected type returns empty list`() = test {
        given {
            val ann = mockAnnotation("DslAlias", listOf("names" to 42))
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { emptyList<String>() }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    @Test
    fun `extract DeprecatedDsl with message and replaceWith`() = test {
        given {
            val ann = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to "Use newProp", "replaceWith" to "newProp")
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.deprecatedMessage == "Use newProp" && result.deprecatedReplaceWith == "newProp"
            }
        }
    }

    @Test
    fun `extract DeprecatedDsl with blank values returns nulls`() = test {
        given {
            val ann = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to " ", "replaceWith" to "")
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.deprecatedMessage == null && result.deprecatedReplaceWith == null }
        }
    }

    @Test
    fun `extract ValidateDsl with expression and message`() = test {
        given {
            val ann = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "it > 0", "message" to "Must be positive")
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.validateExpression == "it > 0" && result.validateMessage == "Must be positive"
            }
        }
    }

    @Test
    fun `extract ValidateDsl with blank values returns nulls`() = test {
        given {
            val ann = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "", "message" to "")
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.validateExpression == null && result.validateMessage == null }
        }
    }

    @Test
    fun `extract all annotations from combined sequence`() = test {
        given {
            val transient = mockAnnotation("TransientDsl", listOf("reason" to "skip"))
            val desc = mockAnnotation("DslDescription", listOf("value" to "desc"))
            val alias = mockAnnotation("DslAlias", listOf("names" to listOf("x")))
            val deprecated = mockAnnotation(
                "DeprecatedDsl",
                listOf("message" to "old", "replaceWith" to "new")
            )
            val validate = mockAnnotation(
                "ValidateDsl",
                listOf("expression" to "it > 0", "message" to "positive")
            )

            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(
                sequenceOf(transient, desc, alias, deprecated, validate)
            )

            expect { true }
            whenever {
                result.isTransient &&
                    result.transientReason == "skip" &&
                    result.description == "desc" &&
                    result.aliases == listOf("x") &&
                    result.deprecatedMessage == "old" &&
                    result.deprecatedReplaceWith == "new" &&
                    result.validateExpression == "it > 0" &&
                    result.validateMessage == "positive"
            }
        }
    }

    @Test
    fun `extract TransientDsl without reason arg returns null reason`() = test {
        given {
            val ann = mockAnnotation("TransientDsl", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.isTransient && result.transientReason == null }
        }
    }

    @Test
    fun `extract DeprecatedDsl with only message`() = test {
        given {
            val ann = mockAnnotation("DeprecatedDsl", listOf("message" to "deprecated"))
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.deprecatedMessage == "deprecated" && result.deprecatedReplaceWith == null
            }
        }
    }
}
