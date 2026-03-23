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

    @Test
    fun `extract DslDescription with no value argument returns null`() = test {
        given {
            val ann = mockAnnotation("DslDescription", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { null }
            whenever { extractor.extract(sequenceOf(ann)).description }
        }
    }

    @Test
    fun `extract DeprecatedDsl with no arguments returns nulls`() = test {
        given {
            val ann = mockAnnotation("DeprecatedDsl", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.deprecatedMessage == null && result.deprecatedReplaceWith == null }
        }
    }

    @Test
    fun `extract ValidateDsl with no arguments returns nulls`() = test {
        given {
            val ann = mockAnnotation("ValidateDsl", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.validateExpression == null && result.validateMessage == null }
        }
    }

    @Test
    fun `extract ValidateDsl with only expression returns null message`() = test {
        given {
            val ann = mockAnnotation("ValidateDsl", listOf("expression" to "it > 0"))
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever { result.validateExpression == "it > 0" && result.validateMessage == null }
        }
    }

    @Test
    fun `extract DslAlias with no names argument returns empty list`() = test {
        given {
            val ann = mockAnnotation("DslAlias", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()

            expect { emptyList<String>() }
            whenever { extractor.extract(sequenceOf(ann)).aliases }
        }
    }

    // --- ListDsl extraction ---

    @Test
    fun `extract ListDsl with all arguments`() = test {
        given {
            val ann = mockAnnotation(
                "ListDsl",
                listOf(
                    "minSize" to 1,
                    "maxSize" to 10,
                    "uniqueElements" to true,
                    "sorted" to true,
                    "withVararg" to false,
                    "withProvider" to false
                )
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasListDsl &&
                    result.listDslMinSize == 1 &&
                    result.listDslMaxSize == 10 &&
                    result.listDslUniqueElements &&
                    result.listDslSorted &&
                    result.listDslWithVararg == false &&
                    result.listDslWithProvider == false
            }
        }
    }

    @Test
    fun `extract ListDsl with negative sizes returns null sizes`() = test {
        given {
            val ann = mockAnnotation(
                "ListDsl",
                listOf(
                    "minSize" to -1,
                    "maxSize" to -1,
                    "uniqueElements" to false,
                    "sorted" to false,
                    "withVararg" to true,
                    "withProvider" to true
                )
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasListDsl &&
                    result.listDslMinSize == null &&
                    result.listDslMaxSize == null &&
                    !result.listDslUniqueElements &&
                    !result.listDslSorted
            }
        }
    }

    @Test
    fun `extract ListDsl with zero sizes returns zero`() = test {
        given {
            val ann = mockAnnotation(
                "ListDsl",
                listOf("minSize" to 0, "maxSize" to 0)
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasListDsl &&
                    result.listDslMinSize == 0 &&
                    result.listDslMaxSize == 0
            }
        }
    }

    @Test
    fun `extract ListDsl with no arguments uses defaults`() = test {
        given {
            val ann = mockAnnotation("ListDsl", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasListDsl &&
                    result.listDslMinSize == null &&
                    result.listDslMaxSize == null &&
                    !result.listDslUniqueElements &&
                    !result.listDslSorted &&
                    result.listDslWithVararg == true &&
                    result.listDslWithProvider == true
            }
        }
    }

    @Test
    fun `extract with no ListDsl annotation returns hasListDsl false`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { false }
            whenever { result.hasListDsl }
        }
    }

    @Test
    fun `extract with no ListDsl annotation returns null listDsl fields`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { true }
            whenever {
                result.listDslMinSize == null &&
                    result.listDslMaxSize == null &&
                    result.listDslWithVararg == null &&
                    result.listDslWithProvider == null
            }
        }
    }

    // --- MapDsl extraction ---

    @Test
    fun `extract MapDsl with all arguments`() = test {
        given {
            val ann = mockAnnotation(
                "MapDsl",
                listOf(
                    "minSize" to 2,
                    "maxSize" to 5,
                    "withVararg" to false,
                    "withProvider" to true
                )
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasMapDsl &&
                    result.mapDslMinSize == 2 &&
                    result.mapDslMaxSize == 5 &&
                    result.mapDslWithVararg == false &&
                    result.mapDslWithProvider == true
            }
        }
    }

    @Test
    fun `extract MapDsl with negative sizes returns null sizes`() = test {
        given {
            val ann = mockAnnotation(
                "MapDsl",
                listOf(
                    "minSize" to -1,
                    "maxSize" to -1,
                    "withVararg" to true,
                    "withProvider" to true
                )
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasMapDsl &&
                    result.mapDslMinSize == null &&
                    result.mapDslMaxSize == null
            }
        }
    }

    @Test
    fun `extract MapDsl with no arguments uses defaults`() = test {
        given {
            val ann = mockAnnotation("MapDsl", emptyList())
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasMapDsl &&
                    result.mapDslMinSize == null &&
                    result.mapDslMaxSize == null &&
                    result.mapDslWithVararg == true &&
                    result.mapDslWithProvider == true
            }
        }
    }

    @Test
    fun `extract with no MapDsl annotation returns hasMapDsl false`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { false }
            whenever { result.hasMapDsl }
        }
    }

    @Test
    fun `extract with no MapDsl annotation returns null mapDsl fields`() = test {
        given {
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(emptySequence())

            expect { true }
            whenever {
                result.mapDslMinSize == null &&
                    result.mapDslMaxSize == null &&
                    result.mapDslWithVararg == null &&
                    result.mapDslWithProvider == null
            }
        }
    }

    @Test
    fun `extract MapDsl with zero sizes returns zero`() = test {
        given {
            val ann = mockAnnotation(
                "MapDsl",
                listOf("minSize" to 0, "maxSize" to 0)
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(ann))

            expect { true }
            whenever {
                result.hasMapDsl &&
                    result.mapDslMinSize == 0 &&
                    result.mapDslMaxSize == 0
            }
        }
    }

    @Test
    fun `extract both ListDsl and MapDsl annotations`() = test {
        given {
            val listAnn = mockAnnotation(
                "ListDsl",
                listOf("minSize" to 1, "uniqueElements" to true)
            )
            val mapAnn = mockAnnotation(
                "MapDsl",
                listOf("maxSize" to 5)
            )
            val extractor = DefaultPropertyAnnotationExtractor()
            val result = extractor.extract(sequenceOf(listAnn, mapAnn))

            expect { true }
            whenever {
                result.hasListDsl && result.hasMapDsl &&
                    result.listDslMinSize == 1 &&
                    result.listDslUniqueElements &&
                    result.mapDslMaxSize == 5
            }
        }
    }
}
