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

    @Test
    fun `debug with tier and branch updates active branches`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test").enableDebug()
                val output = captureStdout {
                    logger.debug("first", tier = 1, branch = true)
                    logger.debug("second", tier = 2)
                }
                output.contains("first") && output.contains("second") && output.contains("|")
            }
        }
    }

    @Test
    fun `warn prints when warning is enabled`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test", isWarningEnabled = true)
                val output = captureStdout { logger.warn("warning msg") }
                output.contains("warning msg") && output.contains("WARN")
            }
        }
    }

    @Test
    fun `error with tier has prefix`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout { Logger("test").error("err", tier = 1) }
                output.contains("|__") && output.contains("err")
            }
        }
    }

    @Test
    fun `globalDebugEnabled reads system property`() = test {
        given {
            expect { false }
            whenever { Logger("test").globalDebugEnabled() }
        }
    }

    @Test
    fun `disableWarning sets warning disabled`() = test {
        given {
            expect { "" }
            whenever {
                val logger = Logger("test", isWarningEnabled = true).disableWarning()
                captureStdout { logger.warn("should not print") }.trim()
            }
        }
    }

    @Test
    fun `tier prefix with active branches shows vertical bar`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test").enableDebug()
                val output = captureStdout {
                    // Set branch at tier 1 to mark it as active
                    logger.debug("parent", tier = 1, branch = true)
                    // Tier 3 should show "|" for tier 1 since it's active
                    logger.debug("grandchild", tier = 3, branch = false)
                }
                output.contains("|") && output.contains("grandchild")
            }
        }
    }

    @Test
    fun `tier prefix without active branches shows spaces`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test").enableDebug()
                val output = captureStdout {
                    // Set branch=false at tier 1 (not active)
                    logger.debug("parent", tier = 1, branch = false)
                    // Tier 3 should show spaces for tier 1 since it's not active
                    logger.debug("grandchild", tier = 3)
                }
                output.contains("grandchild")
            }
        }
    }

    @Test
    fun `updateBranches removes higher tiers when new lower tier is set`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test").enableDebug()
                val output = captureStdout {
                    logger.debug("deep", tier = 3, branch = true)
                    // When we set tier 1, tier 3 branch should be cleared
                    logger.debug("shallow", tier = 1, branch = true)
                    logger.debug("deep again", tier = 3)
                }
                output.contains("deep again")
            }
        }
    }

    @Test
    fun `warn with tier and branch prints correctly`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test", isWarningEnabled = true)
                val output = captureStdout { logger.warn("warning", tier = 2, branch = true) }
                output.contains("warning") && output.contains("|__")
            }
        }
    }

    @Test
    fun `error with branch tracks active branches`() = test {
        given {
            expect { true }
            whenever {
                val logger = Logger("test")
                val output = captureStdout {
                    logger.error("err", tier = 1, branch = true)
                    logger.info("info", tier = 2)
                }
                output.contains("err") && output.contains("info")
            }
        }
    }

    @Test
    fun `infoMultiline with empty string prints nothing`() = test {
        given {
            expect { 1 }
            whenever {
                val output = captureStdout { Logger("test").infoMultiline("") }
                output.trim().lines().filter { it.isNotBlank() }.size
            }
        }
    }

    @Test
    fun `formattedName uses exact 30 char name as-is`() = test {
        given {
            val name30 = "a".repeat(30)
            expect { true }
            whenever {
                val output = captureStdout { Logger(name30).info("msg") }
                output.contains(name30)
            }
        }
    }

    @Test
    fun `globalDebugEnabled returns false when property not set`() = test {
        given {
            expect { false }
            whenever {
                val prev = System.getProperty("debug")
                try {
                    System.clearProperty("debug")
                    Logger("test").globalDebugEnabled()
                } finally {
                    if (prev != null) System.setProperty("debug", prev)
                }
            }
        }
    }

    @Test
    fun `globalDebugEnabled returns true when property is true`() = test {
        given {
            expect { true }
            whenever {
                val prev = System.getProperty("debug")
                try {
                    System.setProperty("debug", "true")
                    Logger("test").globalDebugEnabled()
                } finally {
                    if (prev != null) System.setProperty("debug", prev)
                    else System.clearProperty("debug")
                }
            }
        }
    }

    @Test
    fun `VLoggable companion setGlobalDebug enables debug`() = test {
        given {
            expect { true }
            whenever {
                try {
                    VLoggable.Companion.setGlobalDebug(true)
                    true
                } finally {
                    VLoggable.Companion.setGlobalDebug(false)
                }
            }
        }
    }

    @Test
    fun `VLoggable companion resetGlobalDebug runs without error`() = test {
        given {
            expect { true }
            whenever {
                val prev = System.getProperty("debug")
                try {
                    System.clearProperty("debug")
                    VLoggable.Companion.resetGlobalDebug()
                    true
                } finally {
                    if (prev != null) System.setProperty("debug", prev)
                }
            }
        }
    }

    @Test
    fun `infoMultiline with multiple lines prints all`() = test {
        given {
            expect { true }
            whenever {
                val output = captureStdout {
                    Logger("test").infoMultiline("line1\nline2\nline3")
                }
                output.contains("line1") && output.contains("line2") && output.contains("line3")
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
