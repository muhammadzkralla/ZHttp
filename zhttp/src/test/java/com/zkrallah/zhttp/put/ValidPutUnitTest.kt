package com.zkrallah.zhttp.put

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Complex
import com.zkrallah.zhttp.model.Post
import io.mockk.MockKAnnotations
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidPutUnitTest {
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
        val response = client.put<Unit>("", Unit)

        // Assert
        assertNotNull(response)
        assertNull(response.body)
        assertEquals(201, response.code)
    }

    @Test
    fun `test string request`() = runBlocking {
        // Act
        val str = "Hello World!"
        val response = client.put<String>("str", str)

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
        val response = client.put<Post>("post", post)

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

    @Test
    fun `test list request`() = runBlocking {
        // Act
        val post1 = Post(
            id = 21,
            userId = 1,
            title = "Title",
            body = "Random post body."
        )
        val post2 = Post(
            id = 22,
            userId = 2,
            title = "Title2",
            body = "Random post body2."
        )
        val posts = listOf(post1, post2)
        val response = client.put<List<Post>>("posts", posts)

        // Assert
        assertNotNull(response)
        with(response.body?.get(0)) {
            assertEquals(post1.id, this?.id)
            assertEquals(post1.userId, this?.userId)
            assertEquals(post1.title, this?.title)
            assertEquals(post1.body, this?.body)
        }
        assertEquals(201, response.code)
    }

    @Test
    fun `test params request`() = runBlocking {
        // Act
        val post = Post(
            id = 21,
            userId = 1,
            title = "Title",
            body = "Random post body."
        )
        val response = client.put<Post>("post/${post.id}",
            post
        )

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

    @Test
    fun `test complex request`() = runBlocking {
        // Act
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
        val complex = Complex(
            map,
            set,
            list,
            "string",
            5L
        )
        val response = client.put<Complex>("complex", complex)

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(complex.map, this?.map)
            assertEquals(complex.set, this?.set)
            assertEquals(complex.list, this?.list)
            assertEquals(complex.str, this?.str)
            assertEquals(complex.num, this?.num)
        }
        assertEquals(201, response.code)
    }

    @Test
    fun `test map request`() = runBlocking {
        // Act
        val map = mapOf<Any, Any>(
            "test" to 1,
            2 to "any",
            "random" to 1.5
        )
        val response = client.put<Map<Any, Any>>("map", map)

        // Assert
        assertNotNull(response)
        assertNotNull(response.body)
        assertEquals(201, response.code)
    }
}