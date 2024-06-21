package com.zkrallah.zhttp.post

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Post
import io.mockk.MockKAnnotations
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidPostUnitTest {
    private lateinit var client: ZHttpClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val builder = ZHttpClient.Builder()
            .baseUrl("http://localhost:8080")
            .connectionTimeout(6000)
            .readTimeout(6000)
            .build()

        client = spyk(builder)
    }

    @Test
    fun `test void request`() = runBlocking {
        // Act
        val response = client.post<Unit>("", Unit)

        // Assert
        assertNotNull(response)
        assertNull(response.body)
        assertEquals(201, response.code)
    }

    @Test
    fun `test string request`() = runBlocking {
        // Act
        val str = "Hello World!"
        val response = client.post<String>("str", str)

        // Assert
        assertNotNull(response)
        assertEquals(str, response.body)
        assertEquals(201, response.code)
    }

    @Test
    fun `test serializable request`() = runBlocking {
        // Act
        val post = Post(
            id = 21,
            userId = 1,
            title = "Title",
            body = "Random post body."
        )
        val response = client.post<Post>("post", post)

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(post.id, this?.id)
            assertEquals(post.userId, this?.userId)
            assertEquals(post.title, this?.title)
            assertEquals(post.body, this?.body)
        }
        assertEquals(201, response.code)
    }
}