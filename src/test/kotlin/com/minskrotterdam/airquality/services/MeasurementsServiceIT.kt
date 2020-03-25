package com.minskrotterdam.airquality.services

import com.google.gson.reflect.TypeToken
import com.minskrotterdam.airquality.models.measurements.Data
import com.minskrotterdam.airquality.routes.MEASUREMENTS_PATH
import com.minskrotterdam.airquality.routes.MEASUREMENT_REGION_PATH
import com.minskrotterdam.airquality.routes.MEASUREMENT_STATION_PATH
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
class MeasurementsServiceIT : AbstractHttpServiceIT() {


    private val end = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
    private val start = Instant.now().minus(1, ChronoUnit.DAYS).toString()

    private fun measurementsUrl(): String {
        return "${TEST_API_URL}:${port}/${MEASUREMENTS_PATH}"
    }

    private fun aggrMeasurementsStationUrl(): String {
        return "${TEST_API_URL}:${port}/${MEASUREMENT_STATION_PATH}/NL01487"
    }

    private fun aggrMeasurementsRegionUrl(): String {
        return "${TEST_API_URL}:${port}/${MEASUREMENT_REGION_PATH}/ZH"
    }

    private fun aggrMeasurementsCityUrl(): String {
        return "${TEST_API_URL}:${port}/${MEASUREMENT_REGION_PATH}/ZH"
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
        val entity = httpGet("${measurementsUrl()}?start=$start&end=$end").entity
        val typeOMap: Type = object : TypeToken<Map<String, List<Data>>>() {}.type
        val measurements = toObject(entity, typeOMap) as Map<*, *>
        ctx.assertTrue(measurements.keys.isNotEmpty())
    }

    @Test
    fun testAreStationMeasurementsCorrectlyAggregated(ctx: TestContext) {
        val entity = httpGet("${aggrMeasurementsStationUrl()}?formula=NO&start=$start&end=$end&aggr=min").entity
        val typeOMap: Type = object : TypeToken<List<Data>>() {}.type
        val measurements: List<Data> = toObject(entity, typeOMap) as List<Data>
        ctx.assertEquals(measurements.size, 1)
        ctx.assertNotNull(measurements[0].formula === "NO")
    }

    @Test
    fun testAreSpottedAggregatedValuesWithinRegionalRange(ctx: TestContext) {
        var entity = httpGet("${aggrMeasurementsStationUrl()}?formula=NO&start=$start&end=$end&aggr=min").entity
        val listOfDataType: Type = object : TypeToken<List<Data>>() {}.type
        val measurementZuidpleinMin: List<Data> = toObject(entity, listOfDataType) as List<Data>
        val spottedMeasurementMin = measurementZuidpleinMin[0].value

        entity = httpGet("${aggrMeasurementsStationUrl()}?formula=NO&start=$start&end=$end&aggr=max").entity
        val measurementZuidpleinMax: List<Data> = toObject(entity, listOfDataType) as List<Data>
        val spottedMeasurementMax = measurementZuidpleinMax[0].value

        entity = httpGet("${aggrMeasurementsStationUrl()}?formula=NO&start=$start&end=$end").entity
        val measurementZuidpleinAvg: List<Data> = toObject(entity, listOfDataType) as List<Data>
        val spottedMeasurementAvg = measurementZuidpleinAvg[0].value

        ctx.assertTrue(spottedMeasurementMin <= spottedMeasurementMax,
                "Minimal value measured on the spot should be less than maximal one")
        ctx.assertInRange(spottedMeasurementMin, spottedMeasurementAvg, spottedMeasurementMax - spottedMeasurementMin,
                "Average value measured on the spot should be within the range of minimal and maximal ones")


        entity = httpGet("${aggrMeasurementsCityUrl()}?formula=NO&start=$start&end=$end&aggr=min").entity
        val measurementsRotterdamMin = toObject(entity, listOfDataType) as List<Data>
        val regionalMeasurementMin = measurementsRotterdamMin[0].value

        entity = httpGet("${aggrMeasurementsCityUrl()}?formula=NO&start=$start&end=$end&aggr=max").entity
        val measurementRotterdamMax: List<Data> = toObject(entity, listOfDataType) as List<Data>
        val regionalMeasurementMax = measurementRotterdamMax[0].value

        entity = httpGet("${aggrMeasurementsCityUrl()}?formula=NO&start=$start&end=$end").entity
        val measurementRotterdamAvg: List<Data> = toObject(entity, listOfDataType) as List<Data>
        val regionalMeasurementAvg = measurementRotterdamAvg[0].value

        ctx.assertTrue(regionalMeasurementMin <= regionalMeasurementMax,
                "Minimal value measured in the region should be less than maximal one")
        ctx.assertInRange(regionalMeasurementMin, regionalMeasurementAvg, regionalMeasurementMax - regionalMeasurementMin,
                "Average value measured in the region should be within the range of minimal and maximal ones")

        ctx.assertInRange(regionalMeasurementMin, spottedMeasurementAvg, regionalMeasurementMax - regionalMeasurementMin,
                "Average value measured on the spot should be within the range of minimal and maximal ones in the region")

        ctx.assertInRange(regionalMeasurementMin, spottedMeasurementMin, regionalMeasurementMax - regionalMeasurementMin,
                "Minimal value measured on the spot should be within the range of minimal and maximal ones in the region")

        ctx.assertInRange(regionalMeasurementMax, spottedMeasurementMin, regionalMeasurementMax - regionalMeasurementMin,
                "Maximal value measured on the spot should be within the range of minimal and maximal ones in the region")
    }

    @Test
    fun testAreRegionalAggregatedMeasurementsPresentInFlatMeasurementsSet(ctx: TestContext) {
        var entity = httpGet("${measurementsUrl()}?formula=NO&start=$start&end=$end").entity
        val mapType: Type = object : TypeToken<Map<String, List<Data>>>() {}.type
        val measurements = toObject(entity, mapType) as Map<String, List<Data>>
        val flatMeasurements = measurements["NO"]

        val listOfDataType: Type = object : TypeToken<List<Data>>() {}.type
        entity = httpGet("${aggrMeasurementsRegionUrl()}?formula=NO&start=$start&end=$end&aggr=min").entity
        val measurementsZuidHollandMin = toObject(entity, listOfDataType) as List<Data>

        entity = httpGet("${aggrMeasurementsCityUrl()}?formula=NO&start=$start&end=$end&aggr=max").entity
        val measurementsZuidHollandMax: List<Data> = toObject(entity, listOfDataType) as List<Data>

        val minimal = flatMeasurements?.find { it ->
            it.station_number == measurementsZuidHollandMin[0].station_number && it.timestamp_measured == measurementsZuidHollandMin[0].timestamp_measured
        }
        ctx.assertEquals(measurementsZuidHollandMin[0], minimal, "Aggregated min should be present in flat measurements set")

        val maximal = flatMeasurements?.find { it ->
            it.station_number == measurementsZuidHollandMax[0].station_number && it.timestamp_measured == measurementsZuidHollandMax[0].timestamp_measured
        }
        ctx.assertEquals(measurementsZuidHollandMax[0], maximal, "Aggregated max should be present in flat measurements set")
    }


    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}