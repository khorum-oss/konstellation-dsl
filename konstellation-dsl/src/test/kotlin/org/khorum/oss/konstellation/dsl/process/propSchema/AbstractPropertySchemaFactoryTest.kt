package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.DomainProperty
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.schema.BooleanPropSchema
import org.khorum.oss.konstellation.dsl.schema.BuilderPropSchema
import org.khorum.oss.konstellation.dsl.schema.DefaultPropSchema
import org.khorum.oss.konstellation.dsl.schema.GroupPropSchema
import org.khorum.oss.konstellation.dsl.schema.ListPropSchema
import org.khorum.oss.konstellation.dsl.schema.MapGroupPropSchema
import org.khorum.oss.konstellation.dsl.schema.MapPropSchema
import org.khorum.oss.konstellation.dsl.schema.SingleTransformPropSchema
import org.khorum.oss.konstellation.metaDsl.annotation.MapGroupType

/**
 * Test-only factory that uses the interface type to allow nullable mocking.
 */
private class TestPropertySchemaFactory :
    AbstractPropertySchemaFactory<PropertySchemaFactoryAdapter, DomainProperty>() {
    override fun createPropertySchemaFactoryAdapter(propertyAdapter: DomainProperty): PropertySchemaFactoryAdapter {
        throw UnsupportedOperationException("not used in tests")
    }
}

/**
 * Simple test adapter that implements the interface directly.
 */
private class TestAdapter(
    override val propName: String = "testProp",
    override val actualPropTypeName: TypeName = STRING,
    override val hasSingleEntryTransform: Boolean = false,
    override val transformTemplate: String? = null,
    override val transformType: TypeName? = null,
    override val hasNullableAssignment: Boolean = false,
    override val propertyNonNullableClassName: ClassName? = null,
    override val hasGeneratedDslAnnotation: Boolean = false,
    override val propertyClassDeclarationQualifiedName: String? = null,
    override val propertyClassDeclaration: KSClassDeclaration? = null,
    override val isGroupElement: Boolean = false,
    override val groupElementClassName: ClassName? = null,
    override val groupElementClassDeclaration: KSClassDeclaration? = null,
    override var mapDetails: PropertySchemaFactoryAdapter.MapDetails? = null,
    private val mapDetailsResult: PropertySchemaFactoryAdapter.MapDetails? = null,
    override val mapValueClassDeclaration: KSClassDeclaration? = null,
    override val withVararg: Boolean = true,
    override val withProvider: Boolean = true,
    override val defaultValue: DefaultPropertyValue? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata()
) : PropertySchemaFactoryAdapter {
    override fun mapDetails(): PropertySchemaFactoryAdapter.MapDetails? = mapDetailsResult
}

/**
 * Tests for [AbstractPropertySchemaFactory.determinePropertySchema] branch coverage.
 */
class AbstractPropertySchemaFactoryTest : UnitSim() {

    private val factory = TestPropertySchemaFactory()

    // --- Boolean branch ---

    @Test
    fun `determinePropertySchema returns BooleanPropSchema for Boolean type`() = test {
        given {
            val adapter = TestAdapter(propName = "enabled", actualPropTypeName = BOOLEAN)
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is BooleanPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema returns BooleanPropSchema for nullable Boolean`() = test {
        given {
            val adapter = TestAdapter(
                propName = "flag",
                actualPropTypeName = BOOLEAN.copy(nullable = true),
                hasNullableAssignment = true
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is BooleanPropSchema }
        }
    }

    // --- Default type branch ---

    @Test
    fun `determinePropertySchema returns DefaultPropSchema for String type`() = test {
        given {
            val adapter = TestAdapter(propName = "name", actualPropTypeName = STRING)
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema returns DefaultPropSchema for Int type`() = test {
        given {
            val adapter = TestAdapter(propName = "count", actualPropTypeName = INT)
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    // --- Map branch ---

    @Test
    fun `determinePropertySchema returns MapPropSchema for parameterized Map type`() = test {
        given {
            val mapType = MAP.parameterizedBy(STRING, STRING)
            val adapter = TestAdapter(
                propName = "metadata",
                actualPropTypeName = mapType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.Map"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is MapPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema returns MapGroupPropSchema when mapGroupType is SINGLE`() = test {
        given {
            val mapType = MAP.parameterizedBy(STRING, ClassName("org.test", "Ship"))
            val details: PropertySchemaFactoryAdapter.MapDetails = mockk()
            every { details.mapGroupType } returns MapGroupType.SINGLE
            every { details.keyType } returns STRING
            every { details.valueType } returns ClassName("org.test", "Ship")
            val adapter = TestAdapter(
                propName = "ships",
                actualPropTypeName = mapType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.Map",
                mapDetails = details,
                mapDetailsResult = details
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is MapGroupPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema falls back to DefaultPropSchema for non-parameterized Map qualified name`() = test {
        given {
            val adapter = TestAdapter(
                propName = "rawMap",
                actualPropTypeName = ClassName("kotlin.collections", "Map"),
                propertyClassDeclarationQualifiedName = "kotlin.collections.Map"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    // --- List branch ---

    @Test
    fun `determinePropertySchema returns ListPropSchema for parameterized List type`() = test {
        given {
            val listType = LIST.parameterizedBy(STRING)
            val adapter = TestAdapter(
                propName = "names",
                actualPropTypeName = listType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.List"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is ListPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema returns GroupPropSchema for List with group element`() = test {
        given {
            val listType = LIST.parameterizedBy(ClassName("org.test", "Ship"))
            val adapter = TestAdapter(
                propName = "ships",
                actualPropTypeName = listType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.List",
                isGroupElement = true,
                groupElementClassName = ClassName("org.test", "Ship")
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is GroupPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema falls back to DefaultPropSchema for non-parameterized List qualified name`() = test {
        given {
            val adapter = TestAdapter(
                propName = "rawList",
                actualPropTypeName = ClassName("kotlin.collections", "List"),
                propertyClassDeclarationQualifiedName = "kotlin.collections.List"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    // --- SingleEntryTransform branch ---

    @Test
    fun `determinePropertySchema returns SingleTransformPropSchema when hasSingleEntryTransform`() = test {
        given {
            val adapter = TestAdapter(
                propName = "value",
                hasSingleEntryTransform = true,
                transformTemplate = "MyType(%N)",
                transformType = INT
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is SingleTransformPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema returns DefaultPropSchema when transform has null transformType`() = test {
        given {
            val adapter = TestAdapter(
                propName = "badTransform",
                hasSingleEntryTransform = true,
                transformTemplate = "X(%N)",
                transformType = null
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    // --- Builder branch ---

    @Test
    fun `determinePropertySchema returns BuilderPropSchema for GeneratedDsl annotated type`() = test {
        given {
            val classDecl: KSClassDeclaration = mockk()
            every { classDecl.getAllProperties() } returns emptySequence()
            val adapter = TestAdapter(
                propName = "inner",
                actualPropTypeName = ClassName("org.test", "Inner"),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("org.test", "Inner"),
                propertyClassDeclaration = classDecl
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is BuilderPropSchema }
        }
    }

    @Test
    fun `determinePropertySchema BuilderPropSchema with properties generates accessors with kdoc`() = test {
        given {
            val propDecl: com.google.devtools.ksp.symbol.KSPropertyDeclaration = mockk()
            val propNameMock: com.google.devtools.ksp.symbol.KSName = mockk()
            every { propNameMock.asString() } returns "warpSpeed"
            every { propDecl.simpleName } returns propNameMock

            val classDecl: KSClassDeclaration = mockk()
            every { classDecl.getAllProperties() } returns sequenceOf(propDecl)

            val adapter = TestAdapter(
                propName = "engine",
                actualPropTypeName = ClassName("org.test", "Engine"),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("org.test", "Engine"),
                propertyClassDeclaration = classDecl
            )
            expect { true }
            whenever {
                val result = factory.determinePropertySchema(adapter) as BuilderPropSchema
                result.accessors().first().toString().contains("warpSpeed")
            }
        }
    }

    // --- Fallback (else) branch ---

    @Test
    fun `determinePropertySchema returns DefaultPropSchema as fallback for unknown type`() = test {
        given {
            val unknownType = ClassName("com.custom", "WeirdType")
            val adapter = TestAdapter(
                propName = "weird",
                actualPropTypeName = unknownType,
                propertyClassDeclarationQualifiedName = "com.custom.WeirdType"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }

    // --- isLast parameter ---

    @Test
    fun `determinePropertySchema with isLast true`() = test {
        given {
            val adapter = TestAdapter(propName = "last", actualPropTypeName = STRING)
            expect { true }
            whenever { factory.determinePropertySchema(adapter, isLast = true) is DefaultPropSchema }
        }
    }

    // --- BuilderProp with null declaration (builderDoc null branch) ---

    @Test
    fun `determinePropertySchema BuilderPropSchema with null declaration has no kdoc`() = test {
        given {
            val adapter = TestAdapter(
                propName = "engine",
                actualPropTypeName = ClassName("org.test", "Engine"),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = ClassName("org.test", "Engine"),
                propertyClassDeclaration = null
            )
            expect { true }
            whenever {
                val result = factory.determinePropertySchema(adapter) as BuilderPropSchema
                result.accessors().first().kdoc.isEmpty()
            }
        }
    }

    // --- MapGroupPropSchema path with explicit mapDetails set ---

    @Test
    fun `determinePropertySchema returns MapPropSchema when mapGroupType is NONE`() = test {
        given {
            val mapType = MAP.parameterizedBy(STRING, ClassName("org.test", "Ship"))
            val details: PropertySchemaFactoryAdapter.MapDetails = mockk()
            every { details.mapGroupType } returns MapGroupType.NONE
            val adapter = TestAdapter(
                propName = "ships",
                actualPropTypeName = mapType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.Map",
                mapDetails = null,
                mapDetailsResult = details
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is MapPropSchema }
        }
    }

    // --- List group element with null className triggers require ---

    @Test
    fun `determinePropertySchema List with isGroupElement false returns ListPropSchema`() = test {
        given {
            val listType = LIST.parameterizedBy(ClassName("org.test", "Item"))
            val adapter = TestAdapter(
                propName = "items",
                actualPropTypeName = listType,
                propertyClassDeclarationQualifiedName = "kotlin.collections.List",
                isGroupElement = false
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is ListPropSchema }
        }
    }

    // --- Map via raw type match only (not qualified name) ---

    @Test
    fun `determinePropertySchema Map matched via raw type not qualified name`() = test {
        given {
            val mapType = MAP.parameterizedBy(STRING, INT)
            val adapter = TestAdapter(
                propName = "scores",
                actualPropTypeName = mapType,
                propertyClassDeclarationQualifiedName = null  // no qualified name match
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is MapPropSchema }
        }
    }

    // --- List via raw type match only ---

    @Test
    fun `determinePropertySchema List matched via raw type not qualified name`() = test {
        given {
            val listType = LIST.parameterizedBy(INT)
            val adapter = TestAdapter(
                propName = "numbers",
                actualPropTypeName = listType,
                propertyClassDeclarationQualifiedName = null
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is ListPropSchema }
        }
    }

    // --- hasGeneratedDslAnnotation true but propertyNonNullableClassName null ---

    @Test
    fun `determinePropertySchema falls through when hasGeneratedDsl but no className`() = test {
        given {
            val adapter = TestAdapter(
                propName = "broken",
                actualPropTypeName = ClassName("org.test", "Broken"),
                hasGeneratedDslAnnotation = true,
                propertyNonNullableClassName = null,
                propertyClassDeclarationQualifiedName = "org.test.Broken"
            )
            expect { true }
            whenever { factory.determinePropertySchema(adapter) is DefaultPropSchema }
        }
    }
}
