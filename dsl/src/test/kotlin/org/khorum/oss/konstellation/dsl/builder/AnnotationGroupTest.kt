package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class AnnotationGroupTest : UnitSim() {

    @Test
    fun `annotation with packageName and simpleName adds ClassName`() = test {
        given {
            val group = AnnotationGroup()

            expect { listOf(ClassName("com.example", "MyAnnotation")) }

            whenever {
                group.annotation("com.example", "MyAnnotation")
                group.annotationNames.toList()
            }
        }
    }

    @Test
    fun `annotation with ClassName adds it directly`() = test {
        given {
            val group = AnnotationGroup()
            val className = ClassName("com.example", "Direct")

            expect { listOf(className) }

            whenever {
                group.annotation(className)
                group.annotationNames.toList()
            }
        }
    }

    @Test
    fun `annotation with provider adds ClassName when non-null`() = test {
        given {
            val group = AnnotationGroup()
            val className = ClassName("com.example", "Provided")

            expect { listOf(className) }

            whenever {
                group.annotation { className }
                group.annotationNames.toList()
            }
        }
    }

    @Test
    fun `annotation with provider skips when null`() = test {
        given {
            val group = AnnotationGroup()

            expect { emptyList<ClassName>() }

            whenever {
                group.annotation { null }
                group.annotationNames.toList()
            }
        }
    }

    @Test
    fun `multiple annotations accumulate in list`() = test {
        given {
            val group = AnnotationGroup()
            val first = ClassName("com.example", "First")
            val second = ClassName("com.example", "Second")

            expect { listOf(first, second) }

            whenever {
                group.annotation(first)
                group.annotation("com.example", "Second")
                group.annotationNames.toList()
            }
        }
    }
}
