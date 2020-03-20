package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minskrotterdam.airquality.models.components.Data
import org.testng.Assert
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type

class ComponentsServiceIT : AbstractHttpServiceIT() {

    val POLLUTANTS_SERVICE_URL = "${TEST_API_ENDPOINT}/components"

    @BeforeTest
    fun setUp() {
        setupTests()
    }

    @Test
    fun testIsGivingValidResponse() {
        val response = httpGet(POLLUTANTS_SERVICE_URL)
        Assert.assertEquals(response.statusLine.statusCode, 200)
    }

    @Test
    fun testIsGivingValidBody() {
        val entity = httpGet(POLLUTANTS_SERVICE_URL).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val typeOMap: Type = object : TypeToken<Array<Map<String, List<Data>>>>() {}.type
        val pollutants: Array<Map<String, List<Data>>> = Gson().fromJson(content.toString(), typeOMap)
        Assert.assertEquals(pollutants.size, 1)
    }

    @AfterTest
    fun tearDown() {
        tearDownTests()
    }

}