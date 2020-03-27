package com.minskrotterdam.airquality.services

import com.google.gson.reflect.TypeToken
import com.minskrotterdam.airquality.models.measurements.ExtData
import com.minskrotterdam.airquality.routes.MEASUREMENTS_PATH
import com.minskrotterdam.airquality.routes.MEASUREMENT_COMPONENTS_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Type
import java.time.Instant
import java.time.temporal.ChronoUnit

@RunWith(VertxUnitRunner::class)
class ComponentsMeasurementServiceIT : AbstractHttpServiceIT() {

    private fun componentsMeasurementUrl(): String {
        return "${TEST_API_URL}:${port}/${MEASUREMENT_COMPONENTS_PATH}"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testAreStationMeasurementsCorrectlyAggregated(ctx: TestContext) {
        val TOP_POLLUTANTS_PARAMS = "?formula=NO&formula=NO2&formula=SO2&formula=PM10&formula=PM25&formula=O3&formula=PM10&formula=O4"
        var entity = httpGet("${componentsMeasurementUrl()}$TOP_POLLUTANTS_PARAMS").entity
        val typeOMap: Type = object : TypeToken<List<ExtData>>() {}.type
        val measurementsMax: List<ExtData> = toObject(entity, typeOMap) as List<ExtData>

        val actualMax = measurementsMax.groupBy { it-> it.formula }
        ctx.assertEquals(actualMax.size, 6)

        entity = httpGet("${componentsMeasurementUrl()}$TOP_POLLUTANTS_PARAMS").entity
        val measurementsMin: List<ExtData> = toObject(entity, typeOMap) as List<ExtData>
        val actualMin =  measurementsMin.groupBy { it-> it.formula }
        ctx.assertEquals(actualMin.size, 6)

        ctx.assertTrue( actualMax["SO2"]?.get(0)?.value!! >= actualMin["SO2"]?.get(0)?.value!!)
        ctx.assertTrue( actualMax["NO2"]?.get(0)?.value!! >= actualMin["NO2"]?.get(0)?.value!!)
        ctx.assertTrue( actualMax["NO"]?.get(0)?.value!! >= actualMin["NO"]?.get(0)?.value!!)
        ctx.assertTrue( actualMax["PM10"]?.get(0)?.value!! >= actualMin["PM10"]?.get(0)?.value!!)
        ctx.assertTrue( actualMax["PM25"]?.get(0)?.value!! >= actualMin["PM25"]?.get(0)?.value!!)
        ctx.assertTrue( actualMax["O3"]?.get(0)?.value!! >= actualMin["O3"]?.get(0)?.value!!)
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}