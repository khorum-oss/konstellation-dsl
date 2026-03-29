package org.khorum.oss.konstellation.dsl.utils

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSValueArgument
import io.mockk.mockk
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.junit.jupiter.api.Test

class AnnotationLookupTest : UnitSim() {

    private fun mockAnnotation(name: String, args: Map<String, Any?> = emptyMap()): KSAnnotation {
        val ann: KSAnnotation = mockk()
        val shortName: KSName = mockk()
        io.mockk.every { shortName.asString() } returns name
        io.mockk.every { ann.shortName } returns shortName
        io.mockk.every { ann.arguments } returns args.map { (k, v) ->
            val argName: KSName = mockk()
            io.mockk.every { argName.asString() } returns k
            val arg: KSValueArgument = mockk()
            io.mockk.every { arg.name } returns argName
            io.mockk.every { arg.value } returns v
            arg
        }
        return ann
    }

    private fun mockArgumentWithNullName(value: Any?): KSValueArgument {
        val arg: KSValueArgument = mockk()
        io.mockk.every { arg.name } returns null
        io.mockk.every { arg.value } returns value
        return arg
    }

    // ===== findAnnotation =====

    @Test
    fun `findAnnotation returns annotation when match is found`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")
            val annotations = sequenceOf(ann)

            expect { ann }
            whenever { AnnotationLookup.findAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `findAnnotation returns first matching annotation`() = test {
        given {
            val ann1 = mockAnnotation("GeneratedDsl", mapOf("name" to "first"))
            val ann2 = mockAnnotation("GeneratedDsl", mapOf("name" to "second"))
            val annotations = sequenceOf(ann1, ann2)

            expect { ann1 }
            whenever { AnnotationLookup.findAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `findAnnotation returns null when no match is found`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation")
            val annotations = sequenceOf(ann)

            expect { null }
            whenever { AnnotationLookup.findAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `findAnnotation returns null for empty sequence`() = test {
        given {
            val annotations = emptySequence<KSAnnotation>()

            expect { null }
            whenever { AnnotationLookup.findAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `findAnnotation returns null when annotationClass has null simpleName`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")
            val annotations = sequenceOf(ann)
            val anonClass = object {}::class

            expect { null }
            whenever { AnnotationLookup.findAnnotation(annotations, anonClass) }
        }
    }

    // ===== filterAnnotations =====

    @Test
    fun `filterAnnotations returns all matching annotations`() = test {
        given {
            val ann1 = mockAnnotation("GeneratedDsl", mapOf("name" to "first"))
            val ann2 = mockAnnotation("OtherAnnotation")
            val ann3 = mockAnnotation("GeneratedDsl", mapOf("name" to "second"))
            val annotations = sequenceOf(ann1, ann2, ann3)

            expect { listOf(ann1, ann3) }
            whenever { AnnotationLookup.filterAnnotations(annotations, GeneratedDsl::class).toList() }
        }
    }

    @Test
    fun `filterAnnotations returns empty sequence when no match`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation")
            val annotations = sequenceOf(ann)

            expect { emptyList<KSAnnotation>() }
            whenever { AnnotationLookup.filterAnnotations(annotations, GeneratedDsl::class).toList() }
        }
    }

    @Test
    fun `filterAnnotations returns empty sequence for empty input`() = test {
        given {
            val annotations = emptySequence<KSAnnotation>()

            expect { emptyList<KSAnnotation>() }
            whenever { AnnotationLookup.filterAnnotations(annotations, GeneratedDsl::class).toList() }
        }
    }

    @Test
    fun `filterAnnotations returns empty sequence when annotationClass has null simpleName`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")
            val annotations = sequenceOf(ann)
            val anonClass = object {}::class

            expect { emptyList<KSAnnotation>() }
            whenever { AnnotationLookup.filterAnnotations(annotations, anonClass).toList() }
        }
    }

    // ===== hasAnnotation =====

    @Test
    fun `hasAnnotation returns true when annotation exists`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")
            val annotations = sequenceOf(ann)

            expect { true }
            whenever { AnnotationLookup.hasAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `hasAnnotation returns false when annotation does not exist`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation")
            val annotations = sequenceOf(ann)

            expect { false }
            whenever { AnnotationLookup.hasAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `hasAnnotation returns false for empty sequence`() = test {
        given {
            val annotations = emptySequence<KSAnnotation>()

            expect { false }
            whenever { AnnotationLookup.hasAnnotation(annotations, GeneratedDsl::class) }
        }
    }

    @Test
    fun `hasAnnotation returns false when annotationClass has null simpleName`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")
            val annotations = sequenceOf(ann)
            val anonClass = object {}::class

            expect { false }
            whenever { AnnotationLookup.hasAnnotation(annotations, anonClass) }
        }
    }

    // ===== findArgument =====

    @Test
    fun `findArgument returns argument when found`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))
            val expectedArg = ann.arguments.first()

            expect { expectedArg }
            whenever { AnnotationLookup.findArgument(ann, "withListGroup") }
        }
    }

    @Test
    fun `findArgument returns null when argument name does not match`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))

            expect { null }
            whenever { AnnotationLookup.findArgument(ann, "nonExistent") }
        }
    }

    @Test
    fun `findArgument returns null when annotation is null`() = test {
        given {
            expect { null }
            whenever { AnnotationLookup.findArgument(null, "withListGroup") }
        }
    }

    @Test
    fun `findArgument returns null when annotation has no arguments`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl")

            expect { null }
            whenever { AnnotationLookup.findArgument(ann, "withListGroup") }
        }
    }

    @Test
    fun `findArgument skips arguments with null name`() = test {
        given {
            val ann: KSAnnotation = mockk()
            val shortName: KSName = mockk()
            io.mockk.every { shortName.asString() } returns "GeneratedDsl"
            io.mockk.every { ann.shortName } returns shortName
            val nullNameArg = mockArgumentWithNullName("someValue")
            io.mockk.every { ann.arguments } returns listOf(nullNameArg)

            expect { null }
            whenever { AnnotationLookup.findArgument(ann, "withListGroup") }
        }
    }

    // ===== findArgumentValue =====

    @Test
    fun `findArgumentValue returns correctly typed value`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))

            expect { true }
            whenever { AnnotationLookup.findArgumentValue<Boolean>(ann, "withListGroup") }
        }
    }

    @Test
    fun `findArgumentValue returns string value`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("name" to "testName"))

            expect { "testName" }
            whenever { AnnotationLookup.findArgumentValue<String>(ann, "name") }
        }
    }

    @Test
    fun `findArgumentValue returns null when argument not found`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))

            expect { null }
            whenever { AnnotationLookup.findArgumentValue<String>(ann, "nonExistent") }
        }
    }

    @Test
    fun `findArgumentValue returns null when annotation is null`() = test {
        given {
            expect { null }
            whenever { AnnotationLookup.findArgumentValue<Boolean>(null, "withListGroup") }
        }
    }

    @Test
    fun `findArgumentValue returns value even when type mismatches due to type erasure`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))

            // Due to type erasure, as? T becomes as? Any at runtime, so cast always succeeds
            expect { true }
            whenever { AnnotationLookup.findArgumentValue<String>(ann, "withListGroup") }
        }
    }

    @Test
    fun `findArgumentValue returns integer value`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("count" to 42))

            expect { 42 }
            whenever { AnnotationLookup.findArgumentValue<Int>(ann, "count") }
        }
    }

    @Test
    fun `findArgumentValue returns null when argument value is null`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("name" to null))

            expect { null }
            whenever { AnnotationLookup.findArgumentValue<String>(ann, "name") }
        }
    }

    // ===== anyAnnotationArgMatches =====

    @Test
    fun `anyAnnotationArgMatches returns true when predicate matches`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))
            val annotations = sequenceOf(ann)

            expect { true }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches returns false when predicate does not match`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to false))
            val annotations = sequenceOf(ann)

            expect { false }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches returns false when annotation class does not match`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation", mapOf("withListGroup" to true))
            val annotations = sequenceOf(ann)

            expect { false }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches returns false when argument name does not match`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("otherArg" to true))
            val annotations = sequenceOf(ann)

            expect { false }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches returns false for empty sequence`() = test {
        given {
            val annotations = emptySequence<KSAnnotation>()

            expect { false }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches matches on any of multiple annotations`() = test {
        given {
            val ann1 = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to false))
            val ann2 = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))
            val annotations = sequenceOf(ann1, ann2)

            expect { true }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches returns false when annotationClass has null simpleName`() = test {
        given {
            val ann = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))
            val annotations = sequenceOf(ann)
            val anonClass = object {}::class

            expect { false }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, anonClass, "withListGroup"
                ) { it == true }
            }
        }
    }

    @Test
    fun `anyAnnotationArgMatches ignores non-matching annotations in mixed sequence`() = test {
        given {
            val ann1 = mockAnnotation("OtherAnnotation", mapOf("withListGroup" to true))
            val ann2 = mockAnnotation("GeneratedDsl", mapOf("withListGroup" to true))
            val annotations = sequenceOf(ann1, ann2)

            expect { true }
            whenever {
                AnnotationLookup.anyAnnotationArgMatches(
                    annotations, GeneratedDsl::class, "withListGroup"
                ) { it == true }
            }
        }
    }

    // ===== findAnnotationByName =====

    @Test
    fun `findAnnotationByName returns annotation when match is found`() = test {
        given {
            val ann = mockAnnotation("TransientDsl")
            val annotations = sequenceOf(ann)

            expect { ann }
            whenever { AnnotationLookup.findAnnotationByName(annotations, "TransientDsl") }
        }
    }

    @Test
    fun `findAnnotationByName returns null when no match`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation")
            val annotations = sequenceOf(ann)

            expect { null }
            whenever { AnnotationLookup.findAnnotationByName(annotations, "TransientDsl") }
        }
    }

    @Test
    fun `findAnnotationByName returns null for empty sequence`() = test {
        given {
            expect { null }
            whenever { AnnotationLookup.findAnnotationByName(emptySequence(), "TransientDsl") }
        }
    }

    // ===== hasAnnotationByName =====

    @Test
    fun `hasAnnotationByName returns true when annotation exists`() = test {
        given {
            val ann = mockAnnotation("DslDescription")
            val annotations = sequenceOf(ann)

            expect { true }
            whenever { AnnotationLookup.hasAnnotationByName(annotations, "DslDescription") }
        }
    }

    @Test
    fun `hasAnnotationByName returns false when annotation does not exist`() = test {
        given {
            val ann = mockAnnotation("OtherAnnotation")
            val annotations = sequenceOf(ann)

            expect { false }
            whenever { AnnotationLookup.hasAnnotationByName(annotations, "DslDescription") }
        }
    }

    @Test
    fun `hasAnnotationByName returns false for empty sequence`() = test {
        given {
            expect { false }
            whenever { AnnotationLookup.hasAnnotationByName(emptySequence(), "DslDescription") }
        }
    }
}
