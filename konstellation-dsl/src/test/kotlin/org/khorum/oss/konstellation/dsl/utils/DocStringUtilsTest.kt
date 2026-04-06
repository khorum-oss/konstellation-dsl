package org.khorum.oss.konstellation.dsl.utils

import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim

class DocStringUtilsTest : UnitSim() {

    @Test
    fun `null input returns null`() = test {
        given {
            expect { null }
            whenever { cleanDocString(null) }
        }
    }

    @Test
    fun `blank input returns null`() = test {
        given {
            expect { null }
            whenever { cleanDocString("   ") }
        }
    }

    @Test
    fun `simple single-line doc is trimmed`() = test {
        given {
            expect { "The ship name" }
            whenever { cleanDocString(" The ship name ") }
        }
    }

    @Test
    fun `multiline doc with leading asterisks is cleaned`() = test {
        given {
            val raw = """
                | * First line
                | * Second line
            """.trimMargin()
            expect { "First line\nSecond line" }
            whenever { cleanDocString(raw) }
        }
    }

    @Test
    fun `blank lines at start and end are removed`() = test {
        given {
            val raw = "\n\n  Hello world  \n\n"
            expect { "Hello world" }
            whenever { cleanDocString(raw) }
        }
    }

    @Test
    fun `preserves KDoc references like brackets`() = test {
        given {
            val raw = "See [StarShip] for details"
            expect { "See [StarShip] for details" }
            whenever { cleanDocString(raw) }
        }
    }

    @Test
    fun `preserves at-tags like @see and @param`() = test {
        given {
            val raw = " * The name of the ship\n * @see StarShip"
            expect { "The name of the ship\n@see StarShip" }
            whenever { cleanDocString(raw) }
        }
    }

    @Test
    fun `all-blank lines after cleaning returns null`() = test {
        given {
            expect { null }
            whenever { cleanDocString(" * \n * \n * ") }
        }
    }

    @Test
    fun `preserves intentional indentation in code samples`() = test {
        given {
            val raw = " * Example:\n *     val x = 1"
            expect { "Example:\n    val x = 1" }
            whenever { cleanDocString(raw) }
        }
    }
}
