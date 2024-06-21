package com.zkrallah.zhttp.multipart

import com.zkrallah.zhttp.client.ZHttpClient
import com.zkrallah.zhttp.model.Images
import com.zkrallah.zhttp.model.MultipartBody
import io.mockk.MockKAnnotations
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ValidMultipartUnitTest {
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
    fun `test images request`() = runBlocking {
        // Act
        val id = 101
        val title = "Title"
        val userId = 2
        val post = Images(
            title = title,
            userId = userId
        )

        val postMultipart = MultipartBody(
            name = "data",
            body = post,
            contentType = "application/json"
        )

        val image1Multipart = MultipartBody(
            name = "image1",
            filePath = "E:\\Android Projects\\ZHttp\\zhttp\\src\\test\\java\\com\\zkrallah\\zhttp\\resources\\Screenshot_20240512_013716_Z-Students.jpg",
            contentType = "image/*"
        )

        val image2Multipart = MultipartBody(
            name = "image2",
            filePath = "E:\\Android Projects\\ZHttp\\zhttp\\src\\test\\java\\com\\zkrallah\\zhttp\\resources\\Screenshot_20240512_013730_Z-Students.jpg",
            contentType = "image/*"
        )

        val parts = listOf(postMultipart, image1Multipart, image2Multipart)

        val response = client.multiPart<Images>("images",
            parts = parts
        )

        println(response)

        // Assert
        assertNotNull(response)
        with(response.body) {
            assertEquals(id, this?.id)
            assertEquals(userId, this?.userId)
            assertEquals(title, this?.title)
            assertNotNull(this?.images)
        }
        assertEquals(201, response.code)
    }
}