package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.components.Data
import org.testng.Assert
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

class AirPollutantsServiceIT : AbstractHttpServiceIT() {

    val POLLUTANTS_SERVICE_URL = "${TEST_API_ENDPOINT}/components"

    @BeforeTest
    fun setUp() {
        setupTests()
    }

    @Test
    fun testGetStationsGivingValidResponse() {
        val response = httpGet(POLLUTANTS_SERVICE_URL)
        Assert.assertEquals(response.statusLine.statusCode, 200)
    }

    @Test
    fun testGetPollutantsGivingValidBody() {
        val entity = httpGet(POLLUTANTS_SERVICE_URL).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        println(content)
        val pollutants = Gson().fromJson(content.toString(), Array<Array<Data>>::class.java)
        Assert.assertEquals(pollutants.size, 1)
    }

    @AfterTest
    fun tearDown() {
        tearDownTests()
    }

}