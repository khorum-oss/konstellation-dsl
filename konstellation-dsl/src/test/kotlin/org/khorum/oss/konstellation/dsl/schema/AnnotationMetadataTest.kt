package org.khorum.oss.konstellation.dsl.schema

import com.squareup.kotlinpoet.STRING
import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata

/**
 * Tests for annotation metadata features: @DslDescription, @DslAlias,
 * @DeprecatedDsl, @ValidateDsl, and @TransientDsl support.
 */
class AnnotationMetadataTest : UnitSim() {

    // --- @DslDescription KDoc generation ---

    @Test
    fun `toPropertySpec includes KDoc from DslDescription`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(description = "The server hostname")
            val param = DefaultPropSchema("host", STRING, annotationMetadata = metadata)
            expect { true }
            whenever { param.toPropertySpec().toString().contains("The server hostname") }
        }
    }

    @Test
    fun `toPropertySpec without DslDescription has no KDoc`() = test {
        given {
            val param = DefaultPropSchema("host", STRING)
            expect { false }
            whenever { param.toPropertySpec().toString().contains("/**") }
        }
    }

    // --- @ValidateDsl validation ---

    @Test
    fun `validationStatement returns require expression from ValidateDsl`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(
                validateExpression = "it > 0",
                validateMessage = "capacity must be positive"
            )
            val param = DefaultPropSchema("capacity", STRING, annotationMetadata = metadata)
            expect { "require(capacity > 0) { \"capacity must be positive\" }" }
            whenever { param.validationStatement() }
        }
    }

    @Test
    fun `validationStatement returns null when no ValidateDsl`() = test {
        given {
            val param = DefaultPropSchema("name", STRING)
            expect { null }
            whenever { param.validationStatement() }
        }
    }

    @Test
    fun `validationStatement uses default message when message is null`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(validateExpression = "it > 0")
            val param = DefaultPropSchema("port", STRING, annotationMetadata = metadata)
            expect { "require(port > 0) { \"Validation failed for property 'port'\" }" }
            whenever { param.validationStatement() }
        }
    }

    // --- @DeprecatedDsl ---

    @Test
    fun `buildDeprecatedAnnotation returns annotation with message`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(deprecatedMessage = "Use 'endpoint' instead")
            val param = DefaultPropSchema("url", STRING, annotationMetadata = metadata)
            expect { true }
            whenever { param.buildDeprecatedAnnotation() != null }
        }
    }

    @Test
    fun `buildDeprecatedAnnotation returns null when no DeprecatedDsl`() = test {
        given {
            val param = DefaultPropSchema("name", STRING)
            expect { null }
            whenever { param.buildDeprecatedAnnotation() }
        }
    }

    @Test
    fun `buildDeprecatedAnnotation includes replaceWith when provided`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(
                deprecatedMessage = "Use 'endpoint' instead",
                deprecatedReplaceWith = "endpoint"
            )
            val param = DefaultPropSchema("url", STRING, annotationMetadata = metadata)
            expect { true }
            whenever { param.buildDeprecatedAnnotation().toString().contains("ReplaceWith") }
        }
    }

    // --- @DslAlias - allAccessors ---

    @Test
    fun `allAccessors adds alias functions from DslAlias`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(aliases = listOf("flag"))
            val param = BooleanPropSchema("activated", annotationMetadata = metadata)
            // base accessors: 1 (boolean setter), aliases: 1 function * 1 alias = 1
            expect { 2 }
            whenever { param.allAccessors().size }
        }
    }

    @Test
    fun `allAccessors with no aliases returns only base accessors`() = test {
        given {
            val param = BooleanPropSchema("activated")
            expect { 1 }
            whenever { param.allAccessors().size }
        }
    }

    @Test
    fun `allAccessors applies DeprecatedDsl to both base and alias accessors`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(
                deprecatedMessage = "Use newProp instead",
                aliases = listOf("oldName")
            )
            val param = BooleanPropSchema("oldProp", annotationMetadata = metadata)
            val accessors = param.allAccessors()
            // All accessors should have @Deprecated
            expect { true }
            whenever {
                accessors.all { it.toString().contains("Deprecated") }
            }
        }
    }

    @Test
    fun `allAccessors alias function has the alias name`() = test {
        given {
            val metadata = PropertyAnnotationMetadata(aliases = listOf("desc"))
            val param = BooleanPropSchema("description", annotationMetadata = metadata)
            val aliasAccessor = param.allAccessors().last()
            expect { "desc" }
            whenever { aliasAccessor.name }
        }
    }

    // --- annotationMetadata default ---

    @Test
    fun `DslPropSchema default annotationMetadata is empty`() = test {
        given {
            val param = DefaultPropSchema("x", STRING)
            expect { PropertyAnnotationMetadata() }
            whenever { param.annotationMetadata }
        }
    }

    // --- PropertyAnnotationMetadata data class ---

    @Test
    fun `PropertyAnnotationMetadata isTransient defaults to false`() = test {
        given {
            expect { false }
            whenever { PropertyAnnotationMetadata().isTransient }
        }
    }

    @Test
    fun `PropertyAnnotationMetadata with isTransient true`() = test {
        given {
            expect { true }
            whenever { PropertyAnnotationMetadata(isTransient = true).isTransient }
        }
    }
}
