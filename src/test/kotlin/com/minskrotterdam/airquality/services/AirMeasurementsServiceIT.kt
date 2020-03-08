package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.measurements.PagedMeasurements
import org.testng.Assert
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit

class AirMeasurementsServiceIT : AbstractHttpServiceIT() {

    private val MEASUREMENTS_SERVICE_URL = "${TEST_API_ENDPOINT}/measurements"
    private val start = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
    private val end = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

    @BeforeTest
    fun setUp() {
        setupTests()
    }

    @Test
    fun testGetStationsGivingValidResponse() {
        val response = httpGet(MEASUREMENTS_SERVICE_URL)
        Assert.assertEquals(response.statusLine.statusCode, 200)
    }

    @Test
    fun testGetMeasurementsGivingValidBody() {
        val entity = httpGet("$MEASUREMENTS_SERVICE_URL/?start=$start&end=$end&station_number=NL01487").entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val measurements = Gson().fromJson(content.toString(), Array<PagedMeasurements>::class.java)
        Assert.assertEquals(measurements.size, 1)
    }

    @AfterTest
    fun tearDown() {
        tearDownTests()
    }

}