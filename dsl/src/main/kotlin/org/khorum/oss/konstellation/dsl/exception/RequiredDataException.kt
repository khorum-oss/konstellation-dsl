package org.khorum.oss.konstellation.dsl.exception

import org.khorum.oss.konstellation.dsl.common.ExcludeFromCoverage

@ExcludeFromCoverage
class RequiredDataException(identifier: String) : RuntimeException("value is required. value: $identifier")
