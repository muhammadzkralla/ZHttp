package com.zkrallah.zhttp.delete

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Post
import com.zkrallah.zhttp.model.Query
import io.mockk.MockKAnnotations
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidDeleteUnitTest {
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
        val response = client.delete<Unit>("")

        // Assert
        assertNotNull(response)
        assertNull(response.body)
        assertEquals(202, response.code)
    }

    @Test
    fun `test string request`() = runBlocking {
        // Act
        val response = client.delete<String>("str")

        // Assert
        assertNotNull(response)
        assertEquals("Deleted!", response.body)
        assertEquals(202, response.code)
    }

    @Test
    fun `test serializable request`() = runBlocking {
        // Act
        val response = client.delete<Post>("post")

        val expectedPost = Post(
            id = 21,
            userId = 1,
            title = "Title",
            body = "Random post body."
        )

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(expectedPost.id, this?.id)
            assertEquals(expectedPost.userId, this?.userId)
            assertEquals(expectedPost.title, this?.title)
            assertEquals(expectedPost.body, this?.body)
        }
        assertEquals(202, response.code)
    }

    @Test
    fun `test params request`() = runBlocking {
        // Act
        val postId = 2
        val response = client.delete<Int>("post/$postId")

        // Assert
        assertNotNull(response)
        assertEquals(postId, response.body)
        assertEquals(202, response.code)
    }
}