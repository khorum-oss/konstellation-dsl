package org.khorum.oss.konstellation.dsl.process.propSchema

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.khorum.oss.konstellation.dsl.domain.DefaultDomainProperty
import org.khorum.oss.konstellation.dsl.domain.DefaultPropertyValue
import org.khorum.oss.konstellation.dsl.domain.PropertyAnnotationMetadata
import org.khorum.oss.konstellation.dsl.process.ResolverContext
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

    // @GeneratedDsl has SOURCE retention, so when the property type is compiled into
    // a separate module's JAR the annotation is stripped and KSP cannot see it. Fall
    // back to checking whether a `${qualifiedName}DslBuilder` class is visible on the
    // current resolver's classpath so cross-module nested DSL types are still detected.
    override val hasGeneratedDslAnnotation: Boolean = classDeclarationInternal?.let { decl ->
        AnnotationLookup.hasAnnotation(decl.annotations, GeneratedDsl::class) ||
            ResolverContext.hasGeneratedDslBuilderFor(decl)
    } ?: false

    override val propertyClassDeclarationQualifiedName: String? = classDeclarationInternal?.qualifiedName?.asString()
    override val propertyClassDeclaration: KSClassDeclaration? = classDeclarationInternal

    // list only
    private val collectionFirstElementClassDecl = resolveTypeArgDecl(
        resolvedPropKSType.arguments.firstOrNull()
    )

    // value in map
    private val collectionSecondElementClassDecl = resolveTypeArgDecl(
        resolvedPropKSType.arguments.lastOrNull()
    )

    override val isGroupElement: Boolean =
        collectionFirstElementClassDecl != null && isGeneratedDslType(collectionFirstElementClassDecl)

    override val groupElementClassName: ClassName? = collectionFirstElementClassDecl?.toClassName()
    override val groupElementClassDeclaration: KSClassDeclaration? = collectionFirstElementClassDecl

    private fun hasMapGroup(): Boolean {
        return collectionSecondElementClassDecl != null && isGeneratedDslType(collectionSecondElementClassDecl)
    }

    /**
     * Checks whether [decl] is a generated DSL type. Mirrors the cross-module fallback in
     * [hasGeneratedDslAnnotation]: the `@GeneratedDsl` annotation has `SOURCE` retention, so
     * KSP cannot see it on classes loaded from another module's compiled JAR. When the
     * annotation is not visible we fall back to looking up a sibling `${qualifiedName}DslBuilder`
     * class on the resolver's classpath.
     */
    private fun isGeneratedDslType(decl: KSClassDeclaration): Boolean =
        AnnotationLookup.hasAnnotationByName(decl.annotations, "GeneratedDsl") ||
            ResolverContext.hasGeneratedDslBuilderFor(decl)

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

    companion object {
        private fun resolveTypeArgDecl(arg: KSTypeArgument?): KSClassDeclaration? {
            if (arg == null) return null
            val typeRef = arg.type ?: return null
            return typeRef.resolve().declaration as? KSClassDeclaration
        }
    }
}
