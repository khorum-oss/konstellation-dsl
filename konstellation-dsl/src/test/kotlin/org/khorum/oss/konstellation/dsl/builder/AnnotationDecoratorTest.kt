package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.ClassName
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Test

class AnnotationDecoratorTest : UnitSim() {
    private val decorator = AnnotationDecorator()

    @Test
    fun `createDslMarkerIfAvailable returns null when input is null`() = test {
        given {
            expect { null }

            whenever { decorator.createDslMarkerIfAvailable(null) }
        }
    }

    @Test
    fun `createDslMarkerIfAvailable returns ClassName for fully qualified class`() = test {
        given {
            expect { ClassName("com.example", "MyMarker") }

            whenever { decorator.createDslMarkerIfAvailable("com.example.MyMarker") }
        }
    }

    @Test
    fun `createDslMarkerIfAvailable handles single segment class name`() = test {
        given {
            expect { ClassName("", "MyMarker") }

            whenever { decorator.createDslMarkerIfAvailable("MyMarker") }
        }
    }
}
