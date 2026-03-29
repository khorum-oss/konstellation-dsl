package org.khorum.oss.konstellation.dsl.utils

import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim

class ColorsTest : UnitSim() {

    @Test
    fun `red wraps content with red ANSI code and reset`() = test {
        given {
            expect { "\u001B[31mhello\u001B[0m" }
            whenever { Colors.red("hello") }
        }
    }

    @Test
    fun `green wraps content with green ANSI code and reset`() = test {
        given {
            expect { "\u001B[32mhello\u001B[0m" }
            whenever { Colors.green("hello") }
        }
    }

    @Test
    fun `yellow wraps content with yellow ANSI code and reset`() = test {
        given {
            expect { "\u001B[33mhello\u001B[0m" }
            whenever { Colors.yellow("hello") }
        }
    }

    @Test
    fun `blue wraps content with blue ANSI code and reset`() = test {
        given {
            expect { "\u001B[34mhello\u001B[0m" }
            whenever { Colors.blue("hello") }
        }
    }

    @Test
    fun `purple wraps content with purple ANSI code and reset`() = test {
        given {
            expect { "\u001B[35mhello\u001B[0m" }
            whenever { Colors.purple("hello") }
        }
    }

    @Test
    fun `cyan wraps content with cyan ANSI code and reset`() = test {
        given {
            expect { "\u001B[36mhello\u001B[0m" }
            whenever { Colors.cyan("hello") }
        }
    }

    @Test
    fun `red with empty content wraps correctly`() = test {
        given {
            expect { "\u001B[31m\u001B[0m" }
            whenever { Colors.red("") }
        }
    }

    @Test
    fun `colors contain correct ANSI constants`() = test {
        given {
            expect { "\u001B[0m" }
            whenever { Colors.RESET }
        }
    }
}
