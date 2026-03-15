package org.khorum.oss.konstellation.dsl.exception

class RequiredDataException(identifier: String) : RuntimeException("value is required. value: $identifier")
