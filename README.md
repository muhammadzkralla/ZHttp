<h1 align="center">ZHttp Client Library</h1>

## Introduction

ZHttp is a Kotlin-based HTTP Client Library offering various use cases. I've abstracted away all the technical 
details of making an asynchronous HTTP request, making it very easy to use and beginner-friendly. <br><br>
This does not mean that you cannot customize your request. In fact, you can ignore all my threading and serialization/deserialization 
logic and make a completely raw request, where you can handle reactive programming and the deserialization process.
In manual mode, you take complete control over the whole process. <br> <br>
ZHttp is not built on any high-level or low-level networking or threading libraries.
The only third-party library used is Google's `Gson` library for serialization/deserialization. <br><br>
There are some useful settings you can customize in ZHttp. You can set default headers, connection and read time out periods, and buffer size for uploading files. 

Refer to this [Demo](https://github.com/muhammadzkralla/ZHttp_Demo "Demo") for an example.

## Features

• Beginner-friendly & simple to use <br>
• Thread-safe & type-safe <br>
• Asynchronous/synchronous <br>
• Handles serialization/deserialization <br>
• Has cancellation strategy <br>
• Customizable <br>
• Supports `GET`, `POST`, `DELETE`, `PUT`, `PATCH`, and `MULTIPART` requests <br> <br>

## Installation

To start using ZHttp, you should add the dependencies to your project :

```gradle
// Add this part to your settings.gradle.kts :
dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
            mavenCentral()
            maven("https://jitpack.io")
       }
}
```

```gradle
// Add this dependency to your build.gradle.kts (module) :
dependencies {
      implementation("com.github.muhammadzkralla:ZHttp:2.1")
}
```


## How to use
As stated above, ZHttp supports the `GET`, `POST`, `DELETE`, `PUT`, `PATCH`, and `MULTIPART` requests. We will illustrate how to use each one of them below.

<table align="center">
  <tr>
    <td> 
      <img src ="https://github.com/muhammadzkralla/ZHttp/assets/54005330/f8b3a699-5705-423e-8ddd-ba7b65bee9a8"/>
    </td>
    <td>
      <img src ="https://github.com/muhammadzkralla/ZHttp/assets/54005330/abfcba37-1b26-491f-ac60-ffd68425dc59"/>
    </td>
    <td>
      <img src ="https://github.com/muhammadzkralla/ZHttp/assets/54005330/75d19040-95bb-4ed0-abcc-784fb1dd64d8"/>
    </td>
  </tr>

</table>

<h1 align = "center">  ZHttpClient  </h1> <br>
ZHttpClient is your gate to make any request. To get started, we make an instance of the client : <br> <br>

```kotlin
// Creating a client instance.
val client = ZHttpClient.Builder()
            .baseUrl(BASE_URL)
            .build()

```

You can also specify the connection and the read time out periods : <br> 

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

<h1 align = "center"> Asynchronous Coroutine GET </h1> <br>

To make a suspended `GET` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a GET request.
val response = client.get<TYPE>(ENDPOINT, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

This `response` is a data class that contains the response code, the deserialized body,
response headers, permissions, and exceptions of the HTTP request.

> **IMPORTANT:** `TYPE` is generic, that means that it can be of type string, data class, list of objects, map of any object to any object..etc It's totally type-safe.

> **Note:** 
> `QUERIES`, `HEADERS` arguments can be `null` but, if you want to add `HEADERS` or `QUERIES` to the request :

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

<h1 align = "center"> Asynchronous Lambda GET </h1> <br>

To make a callback `GET` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a GET request.
val getRequest = client.get<TYPE>(ENDPOINT, QUERIES, HEADERS) { success, failure ->
	
}
```

To cancel the request :

```kotlin
// Cancelling the request to free up resources.
getRequest.cancel()
```

<h1 align = "center"> Asynchronous Coroutine POST </h1> <br>

To make a suspended `POST` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a POST request.
val response = client.post<TYPE>(ENDPOINT, BODY, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

> **IMPORTANT:** `BODY` is generic, that means that it can be a string, data class object, list of objects, map of any object to any object..etc It's totally type-safe. 

<h1 align = "center"> Asynchronous Lambda POST </h1> <br>

To make a callback `POST` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a POST request.
val postRequest = client.post<TYPE>(ENDPOINT, BODY, QUERIES, HEADERS) { success, failure ->
	
}
```

> **Note:** `HEADERS`, `QUERIES`, `TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request. 

<h1 align = "center"> Asynchronous Coroutine DELETE </h1> <br>

To make a suspended `DELETE` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a DELETE request.
val response = client.delete<TYPE>(ENDPOINT, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

<h1 align = "center"> Asynchronous Lambda DELETE </h1> <br>

To make a callback `DELETE` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a DELETE request.
val deleteRequest = client.delete<TYPE>(ENDPOINT, QUERIES, HEADERS) { success, failure ->
	
}
```

> **Note:** `HEADERS`, `QUERIES`, `TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request. 

<h1 align = "center"> Asynchronous Coroutine PUT </h1> <br>

To make a suspended `PUT` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PUT request.
val response = client.put<TYPE>(ENDPOINT, BODY, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

<h1 align = "center"> Asynchronous Lambda PUT </h1> <br>

To make a callback `PUT` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PUT request.
client.put<TYPE>(ENDPOINT, BODY, QUERIES, HEADERS) { success, failure ->
	
}
```

> **IMPORTANT:** `BODY` is generic, that means that it can be a string, data class object, list of objects, map of any object to any object..etc It's totally type-safe. 

> **Note:** `HEADERS`, `QUERIES`, `TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

<h1 align = "center"> Asynchronous Coroutine PATCH </h1> <br>

To make a suspended `PATCH` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PATCH request.
val ARG = JsonObject().apply {
            addProperty("arg", "New Value!")
}
// Or you can do it as a data class if you don't want to use Gson:
data class UpdateArg(val arg: Any? = null)
val ARG = UpdateArg(arg = "New Value!")

val response = client.patch<TYPE>(ENDPOINT, ARG, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

<h1 align = "center"> Asynchronous Lambda PATCH </h1> <br>

To make a callback `PATCH` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a PATCH request.
val ARG = JsonObject().apply {
            addProperty("arg", "New Value!")
}
// Or you can do it as a data class if you don't want to use Gson:
data class UpdateArg(val arg: Any? = null)
val ARG = UpdateArg(arg = "New Value!")

val patchRequest = client.patch<TYPE>(ENDPOINT, ARG, QUERIES, HEADERS) { success, failure ->
	
}
```

> **Note:** `HEADERS`, `QUERIES`, `TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

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

<h2> Asynchronous Coroutine MULTIPART </h2> <br>

```kotlin
// The syntax of a MULTIPART request.
val response = client.multiPart<TYPE>(ENDPOINT, parts, QUERIES, HEADERS)
```

It will return you a `response` of the specified `TYPE`

<h2> Asynchronous Lambda MULTIPART </h2> <br>

```kotlin
// The syntax of a MULTIPART request.
val multipart = client.multiPart<TYPE>(ENDPOINT, PARTS, QUERIES, HEADERS) { success, failure ->
	
}
```

> **Note:** `HEADERS`, `QUERIES`, `TYPE`, logging messages, and cancellation strategy follow the same rules as in the `GET` request.

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
like using a manual car, you specify everything. You must handle the threading yourself using for example: Kotlin Coroutines / RxJava / AsyncTasks..etc 
and handle the response deserialization. <br> <br>
An instance of the client is required in the manual mode as the base url, connection / read time out periods, default headers, and buffer size values are applied to the synchronous request too.

> **Note:** All the synchronous functions are annotated with the `synchronized` annotation meaning that they are all thread-safe.

<h1 align = "center"> Synchronous GET </h1> <br>

To make a synchronous `GET` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous GET request.
val response = ZGet(client).doGet(END_POINT, QUERIES, HEADERS)
```

The `response` variable is a data class that contains the response code, the serialized body as a raw string,
response headers, permissions, and exceptions.
Please remember that the body is the raw body string that is received from the HTTP request so,
you need to deserialize the response yourself.

> **Note:** The code above should not be called from the main thread, if you do so, an `android.os.NetworkOnMainThreadException`
> will be thrown.

<h1 align = "center"> Synchronous POST </h1> <br>

To make a synchronous `POST` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous POST request.
val response = ZPost(client).doPost(END_POINT, BODY, QUERIES, HEADERS)
```

<h1 align = "center"> Synchronous DELETE </h1> <br>

To make a synchronous `DELETE` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous DELETE request.
val response = ZDelete(client).doDelete(END_POINT, QUERIES, HEADERS)
```

<h1 align = "center"> Synchronous PUT </h1> <br>

To make a synchronous `PUT` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous PUT request.
val response = ZPut(client).doPut(END_POINT, BODY, QUERIES, HEADERS)
```

<h1 align = "center"> Synchronous PATCH </h1> <br>

To make a synchronous `PATCH` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous PATCH request.
val ARG = JsonObject().apply {
                addProperty("arg", "New Value!")
}
// Or you can do it as a data class :
data class UpdateArg(val arg: Any? = null)
val ARG = UpdateArg(arg = "New Value!")

val response = ZPatch(client).doPatch(END_POINT, ARG, QUERIES, HEADERS)
```

<h1 align = "center"> Synchronous MULTIPART </h1> <br>

To make a synchronous `MULTIPART` request using ZHttp, here's an example of the syntax :

```kotlin
// The syntax of a synchronous MULTIPART request.
val response = ZMultipart(client).doMultipart(END_POINT, PARTS, QUERIES, HEADERS)
```

ZHttp supports both HTTP and HTTPS websites. To enable communication with HTTP websites,
you can add the following attribute to your AndroidManifest.xml file:
```xml
<application
    ...
    android:usesCleartextTraffic="true">
    ...
</application>
```

> **Note:** Please note that while this configuration allows communication with HTTP websites,
> it is generally not recommended due to security concerns.
> It's recommended to use HTTPS whenever possible to ensure data integrity and confidentiality during transit.

