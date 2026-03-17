package org.khorum.oss.konstellation.dsl.process

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.process.propSchema.AbstractPropertySchemaFactory
import org.khorum.oss.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.khorum.oss.konstellation.dsl.schema.BuilderPropSchema
import org.khorum.oss.konstellation.dsl.schema.DefaultPropSchema
import org.khorum.oss.konstellation.dsl.schema.ListPropSchema
import org.khorum.oss.konstellation.dsl.schema.MapPropSchema
import org.khorum.oss.konstellation.dsl.schema.SingleTransformPropSchema
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.junit.jupiter.api.Test

class ParameterFactoryTest : UnitSim() {
    val parameterFactory = object : AbstractPropertySchemaFactory<TestParamFactoryAdaptor, TestPropDomain>() {
        override fun createPropertySchemaFactoryAdapter(propertyAdapter: TestPropDomain): TestParamFactoryAdaptor {
            return TestParamFactoryAdaptor(propertyAdapter.type, propertyAdapter.isGroup)
        }
    }

    @Test
    fun `determineParam will create a default param`() = test {
        given {
            val adapter = TestParamFactoryAdaptor()

            expect {
                TestResponse(
                    "public var test: kotlin.String? = null\n",
                    ""
                )
            }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)

                TestResponse(
                    propSchema.toPropertySpec().toString(),
                    propSchema.accessors().firstOrNull()?.toString() ?: ""
                )
            }
        }
    }

    @Test
    fun `determineParam will create a group param`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                LIST.parameterizedBy(Example::class.asTypeName()),
                isGroup = true
            )

            expect {
                TestResponse(
                    "protected var test: kotlin.collections.List<test.Example>? = null\n",
                    """
                        |public fun test(block: test.ExampleDslBuilder.Group.() -> kotlin.Unit) {
                        |  this.test = test.ExampleDslBuilder.Group().apply(block).items()
                        |}
                        |
                    """.trimMargin()
                )
            }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)

                TestResponse(
                    propSchema.toPropertySpec().toString(),
                    propSchema.accessors().firstOrNull()?.toString() ?: ""
                )
            }
        }
    }

    @Test
    fun `determineParam will create a BooleanPropSchema for BOOLEAN type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(actualPropTypeName = BOOLEAN)

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BooleanPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a MapPropSchema for MAP type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, INT)
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a BuilderPropSchema when hasGeneratedDslAnnotation is true`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = Example::class.asTypeName(),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("test", "Example")
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BuilderPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a SingleTransformPropSchema when hasSingleEntryTransform is true`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                hasSingleEntryTransform = true,
                transformType = INT,
                transformTemplate = "MyType(%N)"
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is SingleTransformPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a ListPropSchema for LIST without group`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = LIST.parameterizedBy(STRING),
                isGroup = false
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is ListPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a DefaultPropSchema as fallback for unknown types`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = ClassName("com.example", "UnknownType")
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema
            }
        }
    }

    @Suppress("LongParameterList")
    class TestParamFactoryAdaptor(
        override val actualPropTypeName: TypeName = STRING,
        val isGroup: Boolean = false,
        override val defaultValue: DefaultPropertyValue? = null,
        override val hasSingleEntryTransform: Boolean = false,
        override val transformTemplate: String? = null,
        override val transformType: TypeName? = null,
        override val hasNullableAssignment: Boolean = false,
        override val propertyNonNullableClassName: ClassName? = null,
        override val hasGeneratedDslAnnotation: Boolean = false,
        override val propertyClassDeclarationQualifiedName: String? = null,
        override val propertyClassDeclaration: KSClassDeclaration? = null,
        override val groupElementClassName: ClassName? = ClassName("test", "Example"),
        override val groupElementClassDeclaration: KSClassDeclaration? = null,
        override var mapDetails: PropertySchemaFactoryAdapter.MapDetails? = null,
        override val mapValueClassDeclaration: KSClassDeclaration? = null,
        override val withVararg: Boolean = true,
        override val withProvider: Boolean = true
    ) : PropertySchemaFactoryAdapter {
        override val propName: String = "test"
        override val isGroupElement: Boolean = isGroup
    }

    class TestPropDomain(
        override val type: TypeName = STRING,
        val isGroup: Boolean = false
    ) : DomainProperty {
        override fun simpleName(): String = "test"
        override fun continueBranch(): Boolean = false
        override fun singleEntryTransformString(): String? = null
    }

    data class TestResponse(
        val propertyContent: String,
        val functionContent: String
    )

    @GeneratedDsl(
        withListGroup = true
    )
    class Example
}
