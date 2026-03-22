package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.khorum.oss.konstellation.dsl.domain.DefaultDomainProperty
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.utils.AnnotationLookup
import org.khorum.oss.konstellation.metaDsl.annotation.DslProperty
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.MapGroupType
import org.khorum.oss.konstellation.metaDsl.annotation.SingleEntryTransformDsl

/**
 * Adapter for property schema factory, providing details about a property in the DSL.
 * This class extracts annotation metadata from KSP declarations and exposes it
 * through the [PropertySchemaFactoryAdapter] interface.
 */
class DefaultPropertySchemaFactoryAdapter(
    prop: KSPropertyDeclaration,
    singleEntryTransform: KSClassDeclaration?,
    override val defaultValue: DefaultPropertyValue? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata(),
) : PropertySchemaFactoryAdapter {
    override val propName: String = prop.simpleName.asString()
    override val actualPropTypeName: TypeName = prop.type.toTypeName()
    override val hasSingleEntryTransform: Boolean = singleEntryTransform != null

    // DslProperty annotation for controlling list/map accessor generation
    private val dslPropertyAnnotation = AnnotationLookup.findAnnotation(prop.annotations, DslProperty::class)

    override val withVararg: Boolean =
        AnnotationLookup.findArgumentValue<Boolean>(dslPropertyAnnotation, DslProperty::withVararg.name) ?: true

    override val withProvider: Boolean =
        AnnotationLookup.findArgumentValue<Boolean>(dslPropertyAnnotation, DslProperty::withProvider.name) ?: true

    constructor(propertyAdapter: DefaultDomainProperty) : this(
        propertyAdapter.prop,
        propertyAdapter.singleEntryTransform(),
        propertyAdapter.defaultValue,
        propertyAdapter.annotationMetadata
    )

    private val singleEntryTransformAnnotation = singleEntryTransform?.let {
        AnnotationLookup.findAnnotation(it.annotations, SingleEntryTransformDsl::class)
    }

    override val transformTemplate: String? =
        AnnotationLookup.findArgumentValue<String>(
            singleEntryTransformAnnotation, SingleEntryTransformDsl<*>::transformTemplate.name
        )?.takeIf { it.isNotBlank() }

    override val transformType: TypeName? =
        AnnotationLookup.findArgumentValue<KSType>(
            singleEntryTransformAnnotation, SingleEntryTransformDsl<*>::inputType.name
        )?.toTypeName()

    private val resolvedPropKSType: KSType = prop.type.resolve()

    override val hasNullableAssignment: Boolean = resolvedPropKSType.isMarkedNullable

    private val classDeclarationInternal = resolvedPropKSType.declaration as? KSClassDeclaration

    override val propertyNonNullableClassName: ClassName? = classDeclarationInternal?.toClassName()

    override val hasGeneratedDslAnnotation: Boolean = classDeclarationInternal?.let {
        AnnotationLookup.hasAnnotation(it.annotations, GeneratedDsl::class)
    } ?: false

    override val propertyClassDeclarationQualifiedName: String? = classDeclarationInternal?.qualifiedName?.asString()
    override val propertyClassDeclaration: KSClassDeclaration? = classDeclarationInternal

    // list only
    private val collectionFirstElementClassDecl = resolvedPropKSType
        .arguments
        .firstOrNull()
        ?.type
        ?.resolve()
        ?.declaration as? KSClassDeclaration

    // value in map
    private val collectionSecondElementClassDecl = resolvedPropKSType
        .arguments
        .lastOrNull()
        ?.type
        ?.resolve()
        ?.declaration as? KSClassDeclaration

    override val isGroupElement: Boolean = collectionFirstElementClassDecl?.let {
        AnnotationLookup.anyAnnotationArgMatches(
            it.annotations, GeneratedDsl::class, GeneratedDsl::withListGroup.name
        ) { value -> value == true }
    } ?: false

    override val groupElementClassName: ClassName? = collectionFirstElementClassDecl?.toClassName()
    override val groupElementClassDeclaration: KSClassDeclaration? = collectionFirstElementClassDecl

    private val dslAnnotation = collectionSecondElementClassDecl?.let {
        AnnotationLookup.findAnnotation(it.annotations, GeneratedDsl::class)
    }

    private fun mapGroupType(): MapGroupType? {
        val mapGroupValue = AnnotationLookup.findArgument(dslAnnotation, GeneratedDsl::withMapGroup.name)
            ?: return null
        return MapGroupType.valueOf(mapGroupValue.value.toString().uppercase())
    }

    override var mapDetails: PropertySchemaFactoryAdapter.MapDetails? = null
    override val mapValueClassDeclaration: KSClassDeclaration? = collectionSecondElementClassDecl

    override fun mapDetails(): PropertySchemaFactoryAdapter.MapDetails? {
        if (mapDetails != null) return mapDetails

        return createMapDetails().also { mapDetails = it }
    }

    private fun createMapDetails(): MapDetails? {
        val groupType = mapGroupType()
        val typeRefs = getTypeArguments()

        if (groupType == null || typeRefs == null) return null

        return MapDetails(groupType, typeRefs.first(), typeRefs.last())
    }

    private fun getTypeArguments(): List<TypeName>? {
        if (actualPropTypeName !is ParameterizedTypeName) return null

        return actualPropTypeName.typeArguments
    }

    /**
     * Details about a map property in the DSL.
     *
     * @property mapGroupType The type of the map group.
     * @property keyType The type of the keys in the map.
     * @property valueType The type of the values in the map.
     */
    class MapDetails(
        override val mapGroupType: MapGroupType = MapGroupType.SINGLE,
        override val keyType: TypeName,
        override val valueType: TypeName
    ) : PropertySchemaFactoryAdapter.MapDetails
}
