<h1 align="center">ZHttp Client Library</h1>

## Introduction

ZHttp is a Kotlin-based HTTP Client Library offering various use cases. I've abstracted away all the technical 
details of making an asynchronous HTTP request, making it very easy to use and beginner-friendly. <br><br>
This does not mean that you cannot customize your request. In fact, you can ignore all my threading and serialization/deserialization 
logic and make a completely raw request, where you can handle reactive programming and the serialization process.
In manual mode, you take complete control over the whole process. <br> <br>
ZHttp is not built on any high-level or low-level networking or threading libraries.
The only third-party library used is Google's `Gson` library for serialization/deserialization. <br><br>
There are some useful settings you can customize in ZHttp. You can set default headers, connection and read time out periods, and buffer size for uploading files. 


## Features

• Beginner-friendly & simple to use <br>
• Thread-safe & type-safe <br>
• Asynchronous/synchronous <br>
• Handles serialization/deserialization <br>
• Has cancelation strategy <br>
• Customizable <br>
• Supports `GET`, `POST`, `DELETE`, `PUT`, `PATCH,` and `MULTIPART` requests <br> <br>

## Installation

To start using ZHttp, you should add the dependencies to your project :

```gradle
// Add it in your root build.gradle at the end of repositories:
dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
            mavenCentral()
            maven { url 'https://jitpack.io' }
       }
}
```

```gradle
// Add the dependency:
dependencies {
      implementation 'com.github.muhammadzkralla:ZHttp:v1.0'
}
```


## How to use
As stated above, ZHttp supports the `GET`, `POST`, `DELETE`, `PUT`, `PATCH`, and `MULTIPART` requests. We will illustrate how to use each one of them below.

<h1 align = "center">  ZHttpClient  </h1> <br>
ZHttpClient is your gate to make any request. To get started, we make an instance of the client : <br> <br>

```kotlin
// Creating a client instance.
val client = ZHttpClient.Builder()
            .baseUrl(BASE_URL)
            .build()

```

You can also specifiy the connection and the read time out periods : <br> 

```kotlin
// Setting the connection and the read time out periods to 20 seconds.
val client = ZHttpClient.Builder()
            .baseUrl(BASE_URL)
            .connectionTimeout(20000)
            .readTimeout(20000)
            .build()

```

You can also add some default headers, these headers will be automatically included on each request made with this client instance : <br> 

```kotlin
// Setting the connection and the read time out periods to 20 seconds.
// Assigning some default headers to be included in each request.
val defaultHeaders = listOf(
            Header("Content-Type", "application/json; charset=UTF-8"),
            Header("Authorization", "Bearer $token")
)

val client = ZHttpClient.Builder()
            .baseUrl(BASE_URL)
            .connectionTimeout(20000)
            .readTimeout(20000)
            .defaultHeaders(defaultHeaders)
            .build()

```

> **Note:** Default headers are not added to the request unless you pass `null` to the headers argument of the request function.
>  We will explain this part more later in the `GET` Request section.

You can also specify the buffer size for file uploading in `MULTIPART` requests : <br> 

```kotlin
// Setting the connection and the read time out periods to 20 seconds.
// Assigning some default headers to be included in each request.
// Setting the buffer size for file uploading in MULTIPART requests to 8 KB.
val defaultHeaders = listOf(
            Header("Content-Type", "application/json; charset=UTF-8"),
            Header("Authorization", "Bearer $token")
)

val client = ZHttpClient.Builder()
            .baseUrl(BASE_URL)
            .connectionTimeout(20000)
            .readTimeout(20000)
            .defaultHeaders(defaultHeaders)
            .filesBufferSize(8 * 1024)
            .build()

```

> **Please Note That:** For each property of the above, there's a default value used if you do not specify them explicitly. <br> <br>
> • baseUrl: is an empty string by default. <br>
> • connectionTimeout & readTimeout: are 20 seconds by default. <br>
> • defaultHeaders: is "Content-Type:application/json; charset=UTF-8" by default. <br>
> • filesBufferSize: is 8 KB by default.

Finally, you can set the default headers after building the client by this function :

```kotlin
// Setting the default headers of the client.
client.setDefaultHeaders(headers)
```

Or removing them by this function :

```kotlin
// Removing the default headers of the client.
client.removeDefaultHeaders()
```

<h1 align = "center"> Asynchronous GET </h1> <br>

To make a `GET` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a GET request.
val getRequest = client.get(END_POINT, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }
})
```

The `response` argument is a data class that contains the response code, the serialized body,
response headers, permissions, and exceptions of the HTTP request.

> **IMPORTANT:** `RESPONSE_TYPE` is generic, that means that it can be of type string, data class, list of objects, map of any object to any object..etc It's totally type-safe.

> **Note:** If the `HEADERS` argument is `null`, the default headers will be added to the request automatically,
> `QUERIES` argument can be `null` too but, if you want to add `HEADERS` or `QUERIES` to the request :

```kotlin
// Adding custom headers and queries to the request.
val HEADERS = listOf(
            Header("Content-Type", "application/json; charset=UTF-8"),
            Header("Authorization", "Bearer $token")
)
        
val QUERIES = listOf(
            Query("param1", "value"),
            Query("param2", "value")
)
```
> **However:** If you specify custom headers for the request, default headers are not going to be added to the request,
>  meaning that only the specified headers will be added to this request.

Finally, to cancel the request :

```kotlin
// Cancelling the request to free up resources.
getRequest?.cancel(true)
```

> **Note:** Once the process is done, this message is logged with the TAG `"ZGet"` : `"processGet: DONE"`, or `"processGet: CANCELLED"` if cancelled.

<h1 align = "center"> Asynchronous POST </h1> <br>

To make a `POST` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a POST request.
val postRequest = client.post(END_POINT, BODY, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }

})
```

> **IMPORTANT:** `BODY` is generic, that means that it can be a string, data class object, list of objects, map of any object to any object..etc It's totally type-safe. 

> **Note:** `HEADERS`, `QUERIES`, `RESPONSE_TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request. 

<h1 align = "center"> Asynchronous DELETE </h1> <br>

To make a `DELETE` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a DELETE request.
val deleteRequest = client.delete(END_POINT, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }

})
```



> **Note:** `HEADERS`, `QUERIES`, `RESPONSE_TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request. 

<h1 align = "center"> Asynchronous PUT </h1> <br>

To make a `PUT` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PUT request.
val putRequest = client.put(END_POINT, BODY, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }
})
```

> **IMPORTANT:** `BODY` is generic, that means that it can be a string, data class object, list of objects, map of any object to any object..etc It's totally type-safe. 

> **Note:** `HEADERS`, `QUERIES`, `RESPONSE_TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

<h1 align = "center"> Asynchronous PATCH </h1> <br>

To make a `PATCH` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PATCH request.
val ARG = JsonObject().apply {
            addProperty("arg", "New Value!")
}

val patchRequest = client.patch(END_POINT, ARG, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }

})
```

> **Note:** `HEADERS`, `QUERIES`, `RESPONSE_TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

<h1 align = "center"> Asynchronous MULTIPART </h1> <br>

To make a `MULTIPART` request, there's actually one or two extra steps according to the type of the part you would like to post. <br> <br>
In case of a normal object / list / map / set or any serializable part, you should first serialize it yourself, for example :

 ```kotlin
// Serialization of an object.
val obj = Gson().toJson(YOUR_OBJECT)
```

The second step is to make a `MultipartBody` of the serialized object :

```kotlin
// Making a MultipartBody of a serializable object.
val objectMultiPartBody = MultipartBody(
                    name = "NAME (REQUIRED)",
                    body = obj,
                    contentType = "application/json"
)
```

In case of a file part, you should just make a `MultipartBody` of the file :

```kotlin
// Making a MultipartBody of a file.
val imageMultipartBody = MultipartBody(
                        fileName = "FILE_NAME (REQUIRED)",
                        filePath = "FILE_PATH",
                        contentType = "image/jpeg"
)
```

> **Note:** The name and fileName are not optional, they must be provided, however, they are not required to have a specific value,
> if they are not important to you or you just don't know what to name them, use any descriptive names.

After you make all your `MultipartBody` objects, you should now add them in a list :

```kotlin
// Add all your parts in a list.
val parts = listOf(objectMultiPartBody, imageMultipartBody)
```

Finally create the `MULTIPART` request itself :

```kotlin
// The syntax of a MULTIPART request.
val multipart = client.multiPart(END_POINT, parts, HEADERS, QUERIES, object : ZListener<RESPONSE_TYPE> {
            override fun onSuccess(response: Response<RESPONSE_TYPE>?) {
                Log.d(TAG, "onSuccess: $response")
                // Handle success :
            }

            override fun onFailure(error: Exception) {
                Log.e(TAG, "onFailure: $error", error)
                // Handle failure :
            }
})
```

> **Note:** `HEADERS`, `QUERIES`, `RESPONSE_TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

> **CRUCIAL:** Please do not attempt to make a `MultipartBody` with both `name`, `body` and `fileName`, `filePath` in the same object, for example :

```kotlin
// DO NOT MAKE THIS :
val badMultiPartBody = MultipartBody(
                    name = "NAME (REQUIRED)",
                    body = obj,
                    fileName = "FILE_NAME (REQUIRED)",
                    filePath = "FILE_PATH",
                    contentType = "application/json"
)
```

<h1 align = "center">  Manual Mode </h1> <br>

As stated earlier, ZHttp is engineered for all developers, not just beginners, as it supports full customization for your HTTP request. 
The manual mode is designed to make you take complete control over your HTTP request. <br> <br> The request made with manual mode is just 
like using a manual car, you specifiy everything. You must handle the threading yourself using for example: Kotlin Coroutines / RxJava / AsyncTasks..etc
, serialization, deserialization, and pass the complete url on each request. <br> <br>
An instance of the client is required in the manual mode as the connection / read time out periods, default headers, and buffer size values are applied to the synchronous request too.

> **Note:** All the synchronous functions are annotated with the `synchronized` annotaion meaning that they are all thread-safe.

<h1 align = "center"> Synchronous GET </h1> <br>

To make a synchronous `GET` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous GET request.
val response = ZGet(client).doRawGetRequest(COMPLETE_URL, HEADERS)
```

The `response` variable is a data class that contains the response code, the unserialized body as a string,
response headers, permissions, and exceptions. Please remember that the body is the raw body string that is received from the HTTP request.

> **Note:** The code above should not be called from the main thread, if you do so, an `android.os.NetworkOnMainThreadException`
> will be thrown.

<h1 align = "center"> Synchronous POST </h1> <br>

To make a synchronous `POST` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous POST request.
val obj = Gson().toJson(YOUR_OBJECT)
val response = ZPost(client).doRawPostRequest(COMPLETE_URL, obj, HEADERS)
```

<h1 align = "center"> Synchronous DELETE </h1> <br>

To make a synchronous `DELETE` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous DELETE request.
val response = ZDelete(client).doRawDeleteRequest(COMPLETE_URL, HEADERS)
```

<h1 align = "center"> Synchronous PUT </h1> <br>

To make a synchronous `PUT` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous PUT request.
val obj = Gson().toJson(YOUR_OBJECT)
val response = ZPut(client).doRawPutRequest(COMPLETE_URL, obj, HEADERS)
```

<h1 align = "center"> Synchronous PATCH </h1> <br>

To make a synchronous `PATCH` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous PATCH request.
val ARG = JsonObject().apply {
                addProperty("arg", "New Value!")
}.toString()
val response = ZPatch(client).doRawPatchRequest(COMPLETE_URL, ARG, HEADERS)
```

<h1 align = "center"> Synchronous MULTIPART </h1> <br>

To make a synchronous `MULTIPART` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous MULTIPART request.
val response = ZMultipart(client).doRawMultipartRequest(COMPLETE_URL, PARTS, HEADERS)
```



