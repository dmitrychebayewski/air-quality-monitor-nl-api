package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.component_info.ComponentInfo
import org.testng.Assert
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream

class PollutantInformationServiceIT : AbstractHttpServiceIT() {
    val FORMULA = "NO2"
    val POLLUTANT_INFO_SERVICE_URL = "${TEST_API_ENDPOINT}/components/${FORMULA}"

    @BeforeTest
    fun setUp() {
        setupTests()
    }

    @Test
    fun testGetStationsGivingValidResponse() {
        val response = httpGet(POLLUTANT_INFO_SERVICE_URL)
        Assert.assertEquals(response.statusLine.statusCode, 200)
        Assert.assertEquals(response.getFirstHeader("content-type").value, "application/json")
    }

    @Test
    fun testGetStationsGivingValidResponseBody() {
        val entity = httpGet(POLLUTANT_INFO_SERVICE_URL).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val componentInfo = Gson().fromJson(content.toString(), ComponentInfo::class.java)
        Assert.assertEquals(componentInfo.data.formula, FORMULA)
    }

    @AfterTest
    fun tearDown() {
        tearDownTests()
    }

}