package com.zkrallah.zhttp.get

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Complex
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

class ValidGetUnitTest {
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
        val response = client.get<Unit>("")

        // Assert
        assertNotNull(response)
        assertNull(response.body)
        assertEquals(200, response.code)
    }

    @Test
    fun `test string request`() = runBlocking {
        // Act
        val response = client.get<String>("str")

        // Assert
        assertNotNull(response)
        assertEquals("Hello World!", response.body)
        assertEquals(200, response.code)
    }

    @Test
    fun `test serializable request`() = runBlocking {
        // Act
        val response = client.get<Post>("post")

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
        assertEquals(200, response.code)
    }

    @Test
    fun `test params request`() = runBlocking {
        // Act
        val param1 = "Title"
        val param2 = "Body"
        val response = client.get<Post>("params",
            queries = listOf(
                Query("param1", param1),
                Query("param2", param2)
            )
        )

        val expectedPost = Post(
            id = 21,
            userId = 1,
            title = param1,
            body = param2
        )

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(expectedPost.id, this?.id)
            assertEquals(expectedPost.userId, this?.userId)
            assertEquals(expectedPost.title, this?.title)
            assertEquals(expectedPost.body, this?.body)
        }
        assertEquals(200, response.code)
    }

    @Test
    fun `test complex request`() = runBlocking {
        // Act
        val response = client.get<Complex>("complex")

        val post1 = Post(101, 99, "Title1", "Body1")
        val post2 = Post(102, 99, "Title2", "Body2")
        val post3 = Post(101, 99, "Title3", "Body1")
        val post4 = Post(102, 99, "Title2", "Body2")

        val map = mapOf(
            1 to post1,
            2 to post2,
            3 to post3,
            4 to post4
        )
        val set = setOf(
            post1, post2, post3, post4
        )
        val list = listOf(
            "1", "test", "2", "5.0"
        )
        val expectedComplex = Complex(
            map,
            set,
            list,
            "string",
            5L
        )

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(expectedComplex.map, this?.map)
            assertEquals(expectedComplex.set, this?.set)
            assertEquals(expectedComplex.list, this?.list)
            assertEquals(expectedComplex.str, this?.str)
            assertEquals(expectedComplex.num, this?.num)
        }
        assertEquals(200, response.code)
    }

    @Test
    fun `test map request`() = runBlocking {
        // Act
        val response = client.get<String>("str")

        // Assert
        assertNotNull(response)
        assertNotNull(response.body)
        assertEquals(200, response.code)
    }
}