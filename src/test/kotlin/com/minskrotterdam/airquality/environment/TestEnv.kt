package com.minskrotterdam.airquality.environment

private const val DEFAULT_HOST = "localhost"
private const val DEFAULT_TEST_PORT = 8082

val TEST_PORT: Int
    get() {
        return when (val systemPort = System.getenv("TEST_PORT")) {
            null -> DEFAULT_TEST_PORT
            else -> Integer.valueOf(systemPort)
        }
    }

val HOST: String
    get() {
        return System.getenv("HOST") ?: DEFAULT_HOST
    }