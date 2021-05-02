#!/usr/bin/env kscript

@file:DependsOn("com.squareup.okhttp3:okhttp:4.9.1")
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

val EARTH_CAM_URL =
    "https://www.earthcam.com/cams/common/gethofitems.php?camera=208d600d317dc6ae2fb4bd0ca3d4746c&length=25"

data class Response(
    @JsonProperty("hofdata")
    val images: List<Image>
)

data class Image(
    @JsonProperty("image_source")
    val imageSource: String,
    val description: String,
    @JsonProperty("date_added")
    val unixTimestamp: String
)

/**
 * Output
 **/

data class Storage(
    val images: List<Livecam>
)

data class Livecam(
    val image: String,
    val description: String,
    val timestamp: String
)

val client = OkHttpClient()

val mapper: ObjectMapper = ObjectMapper()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY))
    .registerModule(KotlinModule())

fun getImages(): Response {
    val request: Request = Request.Builder()
        .url(EARTH_CAM_URL)
        .build()

    client.newCall(request).execute().use { response ->
        val json = response.body!!.string()
        return mapper.readValue(json, Response::class.java)
    }
}

fun Image.livecam() =
    Livecam(
        image = imageSource,
        description = description,
        timestamp = unixTimestamp
    )

val file = File("livecams.json")

fun String.fromJson() = mapper.readValue(file, Response::class.java)

fun String.fromFile() = mapper.readValue(file, Storage::class.java)

val newImages = getImages().images.map { it.livecam() }

if (file.exists()) {
    val json = file.readText()

    val oldData = json.fromFile().images.toMutableList()

    oldData.removeAll(newImages)
    oldData.addAll(newImages)

    if (oldData != newImages) {
        file.delete()
        mapper.writeValue(file, Storage(oldData))
        println("Upgrading content with new pictures")
    }
} else {
    println("Creating new file livecams.json")
    mapper.writeValue(file, Storage(newImages))
}




