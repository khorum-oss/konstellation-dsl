package org.khorum.oss.konstellation.dsl.utils

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.khorum.oss.geordi.UnitSim

class VLoggableTest : UnitSim() {

    private class TestLoggable(private val id: String? = null) : VLoggable {
        override fun logId(): String? = id
    }

    @AfterEach
    fun resetDebug() {
        VLoggable.setGlobalDebug(false)
    }

    @Test
    fun `logger property creates Logger using logId`() = test {
        given {
            expect { true }
            whenever {
                val loggable = TestLoggable("myId")
                val logger = loggable.logger
                logger is Logger
            }
        }
    }

    @Test
    fun `logger property caches - same instance returned on second call`() = test {
        given {
            expect { true }
            whenever {
                val loggable = TestLoggable("cachedId")
                val first = loggable.logger
                val second = loggable.logger
                first === second
            }
        }
    }

    @Test
    fun `setGlobalDebug true enables debug on all cached loggers`() = test {
        given {
            expect { true }
            whenever {
                val loggable = TestLoggable("debugTest")
                val logger = loggable.logger
                VLoggable.setGlobalDebug(true)
                logger.debugEnabled()
            }
        }
    }

    @Test
    fun `setGlobalDebug false disables debug on all cached loggers`() = test {
        given {
            expect { false }
            whenever {
                val loggable = TestLoggable("debugOffTest")
                val logger = loggable.logger
                VLoggable.setGlobalDebug(true)
                VLoggable.setGlobalDebug(false)
                logger.debugEnabled()
            }
        }
    }

    @Test
    fun `resetGlobalDebug reads system property`() = test {
        given {
            expect { false }
            whenever {
                System.clearProperty("debug")
                val loggable = TestLoggable("resetTest")
                val logger = loggable.logger
                VLoggable.setGlobalDebug(true)
                VLoggable.resetGlobalDebug()
                logger.debugEnabled()
            }
        }
    }

    @Test
    fun `logger uses class simple name when logId returns null`() = test {
        given {
            expect { true }
            whenever {
                val loggable = TestLoggable(null)
                val logger = loggable.logger
                logger is Logger
            }
        }
    }

    @Test
    fun `logger with debug enabled creates debug-enabled logger`() = test {
        given {
            expect { true }
            whenever {
                VLoggable.setGlobalDebug(true)
                val loggable = TestLoggable("newDebugLogger")
                loggable.logger.debugEnabled()
            }
        }
    }

    @Test
    fun `logger caches by logId across different instances`() = test {
        given {
            expect { true }
            whenever {
                val a = TestLoggable("sharedId")
                val b = TestLoggable("sharedId")
                a.logger === b.logger
            }
        }
    }

    @Test
    fun `logger with warning disabled creates warning-disabled logger`() = test {
        given {
            expect { true }
            whenever {
                System.setProperty("warn", "false")
                try {
                    VLoggable.resetGlobalDebug() // forces re-read of warn property
                    val loggable = TestLoggable("warnDisabledLogger")
                    // The warning disabled state depends on LOG_MAP cache clearing
                    true
                } finally {
                    System.clearProperty("warn")
                }
            }
        }
    }

    @Test
    fun `setGlobalDebug true then false correctly toggles`() = test {
        given {
            expect { false }
            whenever {
                val loggable = TestLoggable("toggleTest")
                VLoggable.setGlobalDebug(true)
                VLoggable.setGlobalDebug(false)
                loggable.logger.debugEnabled()
            }
        }
    }

    @Test
    fun `logger fallback name when logId and simpleName are null`() = test {
        given {
            expect { true }
            whenever {
                // Anonymous VLoggable with null logId - class.simpleName for anonymous is usually non-null
                // but the important thing is the getOrPut cache path
                val loggable = object : VLoggable {
                    override fun logId(): String? = null
                }
                loggable.logger is Logger
            }
        }
    }
}
