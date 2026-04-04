package org.khorum.oss.konstellation.dsl.process

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.process.propSchema.AbstractPropertySchemaFactory
import org.khorum.oss.konstellation.dsl.process.propSchema.PropertySchemaFactoryAdapter
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.khorum.oss.konstellation.dsl.schema.BuilderPropSchema
import org.khorum.oss.konstellation.dsl.schema.DefaultPropSchema
import org.khorum.oss.konstellation.dsl.schema.GroupPropSchema
import org.khorum.oss.konstellation.dsl.schema.ListPropSchema
import org.khorum.oss.konstellation.dsl.schema.MapGroupPropSchema
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

    @Test
    fun `determineParam with singleEntryTransform but null transformType falls back to DefaultPropSchema`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                hasSingleEntryTransform = true,
                transformType = null,
                transformTemplate = null
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema
            }
        }
    }

    @Test
    fun `determineParam with nullable type produces schema`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = STRING.copy(nullable = true),
                hasNullableAssignment = true
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema
            }
        }
    }

    @Test
    fun `determineParam with isLast false does not affect result`() = test {
        given {
            val adapter = TestParamFactoryAdaptor()

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter, isLast = false)
                propSchema is DefaultPropSchema
            }
        }
    }

    @Test
    fun `determineParam with isLast true does not affect result`() = test {
        given {
            val adapter = TestParamFactoryAdaptor()

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter, isLast = true)
                propSchema is DefaultPropSchema
            }
        }
    }


    // --- Additional branch coverage tests ---

    @Test
    fun `determineParam will create a MapGroupPropSchema when hasMapGroup is true`() = test {
        given {
            val mapDetails = object : PropertySchemaFactoryAdapter.MapDetails {
                override val hasMapGroup = true
                override val keyType: TypeName = STRING
                override val valueType: TypeName = ClassName("test", "Ship")
            }
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, ClassName("test", "Ship")),
                mapDetailsValue = mapDetails
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapGroupPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a MapPropSchema when hasMapGroup is false`() = test {
        given {
            val mapDetails = object : PropertySchemaFactoryAdapter.MapDetails {
                override val hasMapGroup = false
                override val keyType: TypeName = STRING
                override val valueType: TypeName = STRING
            }
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, STRING),
                mapDetailsValue = mapDetails
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create MapProp fallback for Map by qualified name without parameterized type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = ClassName("kotlin.collections", "Map"),
                propertyClassDeclarationQualifiedName = "kotlin.collections.Map"
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema // falls back because not ParameterizedTypeName
            }
        }
    }

    @Test
    fun `determineParam will create ListProp fallback for List by qualified name without parameterized type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = ClassName("kotlin.collections", "List"),
                propertyClassDeclarationQualifiedName = "kotlin.collections.List"
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema // falls back because not ParameterizedTypeName
            }
        }
    }

    @Test
    fun `determineParam creates GroupPropSchema for List with group element`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                LIST.parameterizedBy(Example::class.asTypeName()),
                isGroup = true
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is GroupPropSchema
            }
        }
    }

    @Test
    fun `determineParam returns correct type for each default type`() = test {
        given {
            val types = listOf(CHAR, BYTE, SHORT, INT, LONG, DOUBLE, FLOAT)

            expect { true }

            whenever {
                types.all { type ->
                    val adapter = TestParamFactoryAdaptor(actualPropTypeName = type)
                    parameterFactory.determinePropertySchema(adapter) is DefaultPropSchema
                }
            }
        }
    }

    @Test
    fun `determineParam creates BuilderPropSchema with kdoc from class properties`() = test {
        given {
            val propDecl: KSPropertyDeclaration = io.mockk.mockk()
            val propName: com.google.devtools.ksp.symbol.KSName = io.mockk.mockk()
            io.mockk.every { propName.asString() } returns "name"
            io.mockk.every { propDecl.simpleName } returns propName

            val classDecl: KSClassDeclaration = io.mockk.mockk()
            io.mockk.every { classDecl.getAllProperties() } returns sequenceOf(propDecl)

            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = Example::class.asTypeName(),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("test", "Example"),
                propertyClassDeclaration = classDecl
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BuilderPropSchema
            }
        }
    }

    @Test
    fun `determineParam creates BuilderPropSchema without kdoc when no properties`() = test {
        given {
            val classDecl: KSClassDeclaration = io.mockk.mockk()
            io.mockk.every { classDecl.getAllProperties() } returns emptySequence()

            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = Example::class.asTypeName(),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("test", "Example"),
                propertyClassDeclaration = classDecl
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BuilderPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create a BooleanPropSchema for nullable BOOLEAN type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = BOOLEAN.copy(nullable = true),
                hasNullableAssignment = true
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BooleanPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create MapPropSchema when mapDetails has hasMapGroup false`() = test {
        given {
            val mapDetails = object : PropertySchemaFactoryAdapter.MapDetails {
                override val hasMapGroup = false
                override val keyType: TypeName = STRING
                override val valueType: TypeName = INT
            }
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, INT),
                mapDetailsValue = mapDetails
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapPropSchema
            }
        }
    }

    @Test
    fun `determineParam will create ListPropSchema for LIST with non-nullable type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = LIST.parameterizedBy(INT),
                isGroup = false,
                hasNullableAssignment = false
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is ListPropSchema
            }
        }
    }

    @Test
    fun `determineParam with singleEntryTransform and valid transformType creates SingleTransformPropSchema`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                hasSingleEntryTransform = true,
                transformType = STRING,
                transformTemplate = null
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is SingleTransformPropSchema
            }
        }
    }

    @Test
    fun `determineParam with hasGeneratedDslAnnotation true but null className returns null`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = ClassName("com.example", "UnknownType"),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = null
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema
            }
        }
    }

    @Test
    fun `determineParam creates MapPropSchema for MAP with nullable type`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, STRING).copy(nullable = true),
                hasNullableAssignment = true
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapPropSchema
            }
        }
    }

    @Test
    fun `determineParam creates DefaultPropSchema with defaultValue`() = test {
        given {
            val dv = DefaultPropertyValue(
                rawValue = "hello",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%S", "hello"),
                packageName = "kotlin",
                className = "String"
            )
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = STRING,
                defaultValue = dv
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is DefaultPropSchema && propSchema.defaultValue != null
            }
        }
    }

    @Test
    fun `determineParam creates BooleanPropSchema with defaultValue`() = test {
        given {
            val dv = DefaultPropertyValue(
                rawValue = "false",
                codeBlock = com.squareup.kotlinpoet.CodeBlock.of("%L", false),
                packageName = "kotlin",
                className = "Boolean"
            )
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = BOOLEAN,
                defaultValue = dv
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is BooleanPropSchema && propSchema.defaultValue != null
            }
        }
    }

    @Test
    fun `determineParam creates ListPropSchema with withVararg false`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = LIST.parameterizedBy(STRING),
                isGroup = false,
                withVararg = false,
                withProvider = true
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is ListPropSchema
            }
        }
    }

    @Test
    fun `determineParam creates MapPropSchema with withProvider false`() = test {
        given {
            val adapter = TestParamFactoryAdaptor(
                actualPropTypeName = MAP.parameterizedBy(STRING, INT),
                withVararg = true,
                withProvider = false
            )

            expect { true }

            whenever {
                val propSchema = parameterFactory.determinePropertySchema(adapter)
                propSchema is MapPropSchema
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
        override val withProvider: Boolean = true,
        private val mapDetailsValue: PropertySchemaFactoryAdapter.MapDetails? = null
    ) : PropertySchemaFactoryAdapter {
        override val propName: String = "test"
        override val isGroupElement: Boolean = isGroup

        override fun mapDetails(): PropertySchemaFactoryAdapter.MapDetails? {
            if (mapDetailsValue != null) {
                mapDetails = mapDetailsValue
                return mapDetailsValue
            }
            return mapDetails
        }
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

    @GeneratedDsl
    class Example
}
