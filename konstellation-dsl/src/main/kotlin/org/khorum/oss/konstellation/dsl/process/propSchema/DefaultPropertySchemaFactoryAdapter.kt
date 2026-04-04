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
import org.khorum.oss.konstellation.metaDsl.annotation.GeneratedDsl
import org.khorum.oss.konstellation.metaDsl.annotation.SingleEntryTransformDsl

/**
 * Adapter for property schema factory, providing details about a property in the DSL.
 * This class extracts annotation metadata from KSP declarations and exposes it
 * through the [PropertySchemaFactoryAdapter] interface.
 */
class DefaultPropertySchemaFactoryAdapter(
    private val prop: KSPropertyDeclaration,
    singleEntryTransform: KSClassDeclaration?,
    override val defaultValue: DefaultPropertyValue? = null,
    override val annotationMetadata: PropertyAnnotationMetadata = PropertyAnnotationMetadata(),
) : PropertySchemaFactoryAdapter {
    override val propName: String = prop.simpleName.asString()
    override val actualPropTypeName: TypeName = prop.type.toTypeName()
    override val hasSingleEntryTransform: Boolean = singleEntryTransform != null

    // @ListDsl/@MapDsl take precedence over @PublicDslProperty/@PrivateDslProperty for withVararg/withProvider
    private val dslPropertyAnnotation =
        AnnotationLookup.findAnnotationByName(prop.annotations, "PublicDslProperty")
            ?: AnnotationLookup.findAnnotationByName(prop.annotations, "PrivateDslProperty")

    override val withVararg: Boolean =
        annotationMetadata.listDslWithVararg
            ?: annotationMetadata.mapDslWithVararg
            ?: AnnotationLookup.findArgumentValue<Boolean>(dslPropertyAnnotation, "withVararg")
            ?: true

    override val withProvider: Boolean =
        annotationMetadata.listDslWithProvider
            ?: annotationMetadata.mapDslWithProvider
            ?: AnnotationLookup.findArgumentValue<Boolean>(dslPropertyAnnotation, "withProvider")
            ?: true

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
        AnnotationLookup.hasAnnotationByName(it.annotations, "GeneratedDsl")
    } ?: false

    override val groupElementClassName: ClassName? = collectionFirstElementClassDecl?.toClassName()
    override val groupElementClassDeclaration: KSClassDeclaration? = collectionFirstElementClassDecl

    private fun hasMapGroup(): Boolean {
        return collectionSecondElementClassDecl?.let {
            AnnotationLookup.hasAnnotationByName(it.annotations, "GeneratedDsl")
        } ?: false
    }

    override var mapDetails: PropertySchemaFactoryAdapter.MapDetails? = null
    override val mapValueClassDeclaration: KSClassDeclaration? = collectionSecondElementClassDecl

    override fun mapDetails(): PropertySchemaFactoryAdapter.MapDetails? {
        if (mapDetails != null) return mapDetails

        return createMapDetails().also { mapDetails = it }
    }

    private fun createMapDetails(): MapDetails? {
        val typeRefs = getTypeArguments() ?: return null

        return MapDetails(hasMapGroup(), typeRefs.first(), typeRefs.last())
    }

    private fun getTypeArguments(): List<TypeName>? {
        if (actualPropTypeName !is ParameterizedTypeName) return null

        return actualPropTypeName.typeArguments
    }

    /**
     * Details about a map property in the DSL.
     *
     * @property hasMapGroup Whether the map value type has a @MapDsl annotation.
     * @property keyType The type of the keys in the map.
     * @property valueType The type of the values in the map.
     */
    class MapDetails(
        override val hasMapGroup: Boolean = false,
        override val keyType: TypeName,
        override val valueType: TypeName
    ) : PropertySchemaFactoryAdapter.MapDetails
}
