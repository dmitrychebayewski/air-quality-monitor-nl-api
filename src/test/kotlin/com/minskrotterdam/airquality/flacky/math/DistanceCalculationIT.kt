package com.minskrotterdam.airquality.flacky.math

import com.minskrotterdam.airquality.cache.StationsCache
import com.minskrotterdam.airquality.handlers.onetime.CacheHandler
import com.minskrotterdam.airquality.services.AbstractHttpServiceIT
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class DistanceCalculationIT : AbstractHttpServiceIT() {


    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testFindStationNearestToLeiden(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("52.1518157", "4.4811088666204295")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Den Haag-Amsterdamse Veerkade"), "It should contain Den Haag-Amsterdamse Veerkade")
    }

    @Test
    fun testFindStationNearestToAhoy(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("51.88560", "4.48811")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Pleinweg"), "It should contain Pleinweg")
    }

    @Test
    fun testFindStationNearDeKuip(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("51.89079", "4.51305")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Rotterdam-Zwartewaalstraat"), "It should contain Rotterdam-Zwartewaalstraat")
    }

    @Test
    fun testFindStationNearDelft(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("51.9974", "4.3530")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Den Haag-Bleriotlaan"), "It should contain Den Haag-Bleriotlaan")
    }

    @Test
    fun testFindStationNearIjmuiden(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("52.4600", "4.6172")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("IJmuiden-Kanaalstraat"), "It should contain IJmuiden-Kanaalstraat")
    }

    @Test
    fun testFindStationNearBreda(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("51.5914", "4.7793")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Breda-Tilburgseweg"), "Breda-Tilburgseweg")
    }

    @Test
    fun testFindStationNearHaarlem(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("52.36960", "4.60766")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Haarlem-Schipholweg"), "It should contain Haarlem-Schipholweg")
    }

    @Test
    fun testFindStationInAmsterdam(ctx: TestContext) {
        runBlocking {
            withContext(Dispatchers.Default) {
                CacheHandler().initStationsCache()
            }
        }
        val station = StationsCache.getStation("52.3576", "4.8687")
        ctx.assertNotNull(station, "It should be loaded")
        ctx.assertTrue(station.location.contains("Amsterdam-Vondelpark"), "It should contain Amsterdam-Vondelpark")
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}