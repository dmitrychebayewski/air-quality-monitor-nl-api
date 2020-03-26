package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.station_info.Data
import com.minskrotterdam.airquality.models.station_info.StationInfo
import com.minskrotterdam.airquality.routes.STATION_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.io.ByteArrayOutputStream

@RunWith(VertxUnitRunner::class)
class StationInformationServiceIT : AbstractHttpServiceIT() {
    private val STATION_NR = "NL01487"

    private fun stationInfoUrl(): String {
        return "${TEST_API_URL}:${port}/${STATION_PATH}/${STATION_NR}"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(stationInfoUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
        ctx.assertEquals(response.getFirstHeader("content-type").value, "application/json")
    }

    @Test
    fun testIsGivingValidBody(ctx: TestContext) {
        val entity = httpGet(stationInfoUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val stationDetails = Gson().fromJson(content.toString(), StationInfo::class.java)
        ctx.assertEquals(stationDetails.data.location, "Rotterdam-Pleinweg")
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}