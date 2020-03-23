package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minskrotterdam.airquality.models.measurements.Data
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.time.Instant
import java.time.temporal.ChronoUnit

@RunWith(VertxUnitRunner::class)
class MeasurementsServiceIT : AbstractHttpServiceIT() {

    private val start = Instant.now().truncatedTo(ChronoUnit.DAYS).toString()
    private val end = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

    private fun measurementsUrl(): String {
        return "${TEST_API_URL}:${port}/${TEST_API_ENDPOINT}/measurements?station_number=NL01487"
    }

    private fun aggrMeasurementsUrl(): String {
        return "${TEST_API_URL}:${port}/${TEST_API_ENDPOINT}/measurements/NL01487"
    }


    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(measurementsUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
    }

    @Test
    fun testIsGivingValidBody(ctx: TestContext) {
        val entity = httpGet("${measurementsUrl()}&start=$start&end=$end").entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val typeOMap: Type = object : TypeToken<Array<Map<String, List<Data>>>>() {}.type
        val measurements: Array<Map<String, List<Data>>> = Gson().fromJson(content.toString(), typeOMap)
        ctx.assertTrue(measurements[0].keys.isNotEmpty())
    }

    @Test
    fun testIsDataCorrectlyAggregated(ctx: TestContext) {
        val entity = httpGet("${aggrMeasurementsUrl()}?formula=NO").entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val typeOMap: Type = object : TypeToken<List<Data>>() {}.type
        val measurements: List<Data> = Gson().fromJson(content.toString(), typeOMap)
        ctx.assertEquals(measurements.size, 1)
        ctx.assertNotNull(measurements[0].formula === "NO")
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}