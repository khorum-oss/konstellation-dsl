package org.khorum.oss.konstellation.dsl.builder

import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.STRING
import org.khorum.oss.geordi.UnitSim
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KPParameterSpecGroupTest : UnitSim() {

    @Nested
    inner class ParamTest {
        @Test
        fun `param builds and returns a ParameterSpec`() = test {
            given {
                expect { "myParam" }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val spec = group.param {
                        name = "myParam"
                        type = STRING
                    }
                    spec.name
                }
            }
        }

        @Test
        fun `param builds spec with correct type`() = test {
            given {
                expect { "kotlin.String" }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val spec = group.param {
                        name = "myParam"
                        type = STRING
                    }
                    spec.type.toString()
                }
            }
        }
    }

    @Nested
    inner class VarargParamTest {
        @Test
        fun `varargParam sets name to items`() = test {
            given {
                expect { "items" }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val spec = group.varargParam {
                        type = STRING
                    }
                    spec.name
                }
            }
        }

        @Test
        fun `varargParam adds VARARG modifier`() = test {
            given {
                expect { true }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val spec = group.varargParam {
                        type = STRING
                    }
                    spec.modifiers.contains(KModifier.VARARG)
                }
            }
        }

        @Test
        fun `varargParam renders correctly`() = test {
            given {
                expect { "vararg items: kotlin.String" }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val spec = group.varargParam {
                        type = STRING
                    }
                    spec.toString().trimIndent().trim()
                }
            }
        }
    }

    @Nested
    inner class AccumulationTest {
        @Test
        fun `Group accumulates multiple params via items`() = test {
            given {
                expect { 2 }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val first = group.param {
                        name = "first"
                        type = STRING
                    }
                    val second = group.param {
                        name = "second"
                        type = INT
                    }
                    group.items.add(first)
                    group.items.add(second)
                    group.items.size
                }
            }
        }

        @Test
        fun `Group accumulates params with correct names`() = test {
            given {
                expect { listOf("first", "second") }

                whenever {
                    val group = KPParameterSpecBuilder.Group()
                    val first = group.param {
                        name = "first"
                        type = STRING
                    }
                    val second = group.param {
                        name = "second"
                        type = INT
                    }
                    group.items.add(first)
                    group.items.add(second)
                    group.items.map { it.name }
                }
            }
        }
    }
}
