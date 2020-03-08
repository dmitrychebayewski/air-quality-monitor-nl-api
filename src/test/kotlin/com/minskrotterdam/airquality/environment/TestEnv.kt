package com.minskrotterdam.airquality.environment

private val DEFAULT_HOST = "localhost"
private val DEFAULT_TEST_PORT = 8081

val TEST_PORT: Int
    get() {
        val systemPort = System.getenv("TEST_PORT")
        when (systemPort) {
            null -> return DEFAULT_TEST_PORT
            else -> return Integer.valueOf(systemPort)
        }
    }

val HOST: String
    get() {
        return System.getenv("HOST") ?: DEFAULT_HOST
    }