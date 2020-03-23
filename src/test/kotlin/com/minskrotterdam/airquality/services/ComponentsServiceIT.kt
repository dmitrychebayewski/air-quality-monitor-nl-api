package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.minskrotterdam.airquality.models.components.Data
import com.minskrotterdam.airquality.routes.COMPONENTS_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type

@RunWith(VertxUnitRunner::class)
class ComponentsServiceIT : AbstractHttpServiceIT() {

    private fun componentInfoUrl(): String {
        return "${TEST_API_URL}:${port}/${COMPONENTS_PATH}"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(componentInfoUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
    }

    @Test
    fun testIsGivingValidBody(ctx: TestContext) {
        val entity = httpGet(componentInfoUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val typeOMap: Type = object : TypeToken<Array<Map<String, List<Data>>>>() {}.type
        val pollutants: Array<Map<String, List<Data>>> = Gson().fromJson(content.toString(), typeOMap)
        ctx.assertEquals(pollutants.size, 1)
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}