package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.cache.StationsCache
import com.minskrotterdam.airquality.handlers.CacheHandler
import com.minskrotterdam.airquality.models.stations.Data
import com.minskrotterdam.airquality.routes.STATIONS_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(VertxUnitRunner::class)
class StationsServiceIT : AbstractHttpServiceIT() {
    val LOCATION = "amsterdam"

    private fun stationsUrl(): String {
        return "${TEST_API_URL}:${port}/${STATIONS_PATH}/amsterdam"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testLoadStationsCache(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        ctx.assertNotNull(StationsCache.getStation("51.9236286", "4.4083969"), "It should be loaded")
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(stationsUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
        ctx.assertEquals(response.getFirstHeader("content-type").value, "application/json")
    }

    @Test
    fun testIsGivingValidData(ctx: TestContext) {
        val entity = httpGet(stationsUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val stations = Gson().fromJson(content.toString(), Array<Data>::class.java)
        stations.forEach { station -> ctx.assertTrue(station.location.toLowerCase().contains(LOCATION)) }
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}