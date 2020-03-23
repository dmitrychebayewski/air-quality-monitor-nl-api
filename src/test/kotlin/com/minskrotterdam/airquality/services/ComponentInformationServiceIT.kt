package com.minskrotterdam.airquality.services

import com.google.gson.Gson
import com.minskrotterdam.airquality.models.component_info.ComponentInfo
import com.minskrotterdam.airquality.models.component_info.Limit
import com.minskrotterdam.airquality.routes.COMPONENT_FORMULA_LIMIT_PATH
import com.minskrotterdam.airquality.routes.COMPONENT_FORMULA_PATH
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import java.io.ByteArrayOutputStream

@RunWith(VertxUnitRunner::class)
class ComponentInformationServiceIT : AbstractHttpServiceIT() {
    private val FORMULA = "NO2"

    private fun componentInfoUrl(): String {
        return "${TEST_API_URL}:${port}/${COMPONENT_FORMULA_PATH}/${FORMULA}"
    }

    private fun componentLimitUrl(): String {
        return "${TEST_API_URL}:${port}/${COMPONENT_FORMULA_LIMIT_PATH}/${FORMULA}"
    }

    @Before
    fun setUp(ctx: TestContext) {
        setupVerticle(ctx)
    }

    @Test
    fun testIsGivingValidResponse(ctx: TestContext) {
        val response = httpGet(componentInfoUrl())
        ctx.assertEquals(response.statusLine.statusCode, 200)
        ctx.assertEquals(response.getFirstHeader("content-type").value, "application/json")
    }

    @Test
    fun testIsGivingValidBody(ctx: TestContext) {
        val entity = httpGet(componentInfoUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val componentInfo = Gson().fromJson(content.toString(), ComponentInfo::class.java)
        ctx.assertEquals(componentInfo.data.formula, FORMULA)
    }

    @Test
    fun testIsGivingValidValues(ctx: TestContext) {
        val entity = httpGet(componentLimitUrl()).entity
        val content = ByteArrayOutputStream()
        entity.writeTo(content)
        val limit = Gson().fromJson(content.toString(), Limit::class.java)
        ctx.assertEquals(limit.lowerband, 200)
    }

    @After
    fun tearDown(ctx: TestContext) {
        tearDownTests(ctx)
    }

}