#!/usr/bin/env kscript

@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.time.LocalDateTime

data class Configuration(
    @JsonProperty("history_languages")
    val history: List<String>,
    @JsonProperty("last_updated")
    val lastUpdated: String = LocalDateTime.now().toString()
)

val historyDir = File("history/")

val languages = historyDir.list()?.mapNotNull {
    it.split("/")
        .lastOrNull()
        ?.split(".json")
        ?.firstOrNull()
} ?: emptyList()

val configuration = Configuration(
    history = languages
)

val configurationFile = File("configuration.json")

if (configurationFile.exists()) {
    println("Deleting old configuration files")
    configurationFile.delete()
}

val objectMapper: ObjectMapper =
    ObjectMapper().registerModule(KotlinModule())

objectMapper.writerWithDefaultPrettyPrinter()
    .writeValue(File("configuration.json"), configuration)