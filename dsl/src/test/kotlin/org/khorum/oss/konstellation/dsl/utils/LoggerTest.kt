package org.khorum.oss.konstellation.dsl.utils

import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class LoggerTest : UnitSim() {

    @Test
    fun `enableDebug sets debugEnabled to true`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test")
                logger.enableDebug()
                logger.debugEnabled()
            }
        }
    }

    @Test
    fun `disableDebug sets debugEnabled to false`() = test {
        given {
            expect { false }
            whenever {
                val logger = Logger("test")
                logger.enableDebug()
                logger.disableDebug()
                logger.debugEnabled()
            }
        }
    }

    @Test
    fun `debugEnabled is false by default`() = test {
        given {
            expect { false }
            whenever { Logger("test").debugEnabled() }
        }
    }

    @Test
    fun `debug prints nothing when debug is disabled`() = test {
        given {
            expect { "" }
            whenever {
                val output = captureStdout { Logger("test").debug("message") }
                output.trim()
            }
        }
    }

    @Test
    fun `debug prints when debug is enabled`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test").enableDebug()
                val output = captureStdout { logger.debug("message") }
                output.contains("message") && output.contains("DEBUG")
            }
        }
    }

    @Test
    fun `info always prints`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").info("hello info") }
                output.contains("hello info") && output.contains("INFO")
            }
        }
    }

    @Test
    fun `warn prints nothing when warning is disabled`() = test {
        given {
            expect { "" }
            whenever {
                val logger = Logger("test", isWarningEnabled = false)
                val output = captureStdout { logger.warn("warning msg") }
                output.trim()
            }
        }
    }

    @Test
    fun `error always prints`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").error("error msg") }
                output.contains("error msg") && output.contains("ERROR")
            }
        }
    }

    @Test
    fun `info with tier 0 has no prefix`() = test {
        given {
            expect { false }
            whenever {
                val output = captureStdout { Logger("test").info("msg", tier = 0) }
                output.contains("|__")
            }
        }
    }

    @Test
    fun `info with tier 1 has branch prefix`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").info("msg", tier = 1) }
                output.contains("|__")
            }
        }
    }

    @Test
    fun `info with tier 2 has nested prefix`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").info("msg", tier = 2) }
                output.contains("|__")
            }
        }
    }

    @Test
    fun `infoMultiline with single line prints one line`() = test {
        given {
            expect { 1 }
            whenever {
                val output = captureStdout { Logger("test").infoMultiline("single") }
                output.trim().lines().filter { it.isNotBlank() }.size
            }
        }
    }

    @Test
    fun `infoMultiline with multi-line message prints all lines`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").infoMultiline("line1\nline2\nline3") }
                val lines = output.trim().lines().filter { it.isNotBlank() }
                lines.size == 3 && output.contains("line1") && output.contains("line2") && output.contains("line3")
            }
        }
    }

    @Test
    fun `formattedName truncates names longer than 30 chars`() = test {
        given {
            val longName = "a".repeat(35)
            expect { true }
            whenever {
                val output = captureStdout { Logger(longName).info("msg") }
                output.contains("a".repeat(27) + "...")
            }
        }
    }

    @Test
    fun `formattedName leaves short names as-is`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("short").info("msg") }
                output.contains("short")
            }
        }
    }

    private fun captureStdout(block: () -> Unit): String {
        val originalOut = System.out
        val baos = ByteArrayOutputStream()
        System.setOut(PrintStream(baos))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return baos.toString()
    }
}
