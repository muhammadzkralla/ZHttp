package com.zkrallah.zhttp.auth.basic

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Basic
import com.zkrallah.zhttp.model.BasicAuthResult
import io.mockk.MockKAnnotations
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ValidBasicAuthUnitTest {
    private lateinit var client: ZHttpClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val builder = ZHttpClient.Builder()
            .baseUrl("http://httpbin.org")
            .connectionTimeout(6000)
            .readTimeout(6000)
            .authenticated(Basic("foo", "bar"))
            .build()

        client = spyk(builder)
    }

    @Test
    fun `test basic auth request`() = runBlocking {
        // Act
        val response = client.get<BasicAuthResult>("basic-auth/foo/bar")

        val expectedBasicAuthResult = BasicAuthResult(
            authenticated = true,
            user = "foo"
        )

        // Assert
        assertNotNull(response)
        assertNotNull(response.body)
        with(response.body) {
            assertEquals(expectedBasicAuthResult.authenticated, this?.authenticated)
            assertEquals(expectedBasicAuthResult.user, this?.user)
        }
        assertEquals(200, response.code)
    }
}