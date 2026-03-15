package org.khorum.oss.konstellation.generateTest

import org.khorum.oss.konstellation.metaDsl.annotation.SingleEntryTransformDsl

@SingleEntryTransformDsl<String>(String::class)
data class Stardate(val content: String)
