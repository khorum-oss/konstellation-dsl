package org.khorum.oss.konstellation.dsl.exception

import org.khorum.oss.konstellation.dsl.common.ExcludeFromCoverage

@ExcludeFromCoverage
class KonstellationException(message: String) : IllegalAccessException(message)
