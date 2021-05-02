#!/usr/bin/env kscript

@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.VisibilityChecker
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class Configuration(
    @JsonProperty("history_languages")
    val history : List<String>
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

val objectMapper = ObjectMapper().registerModule(KotlinModule())

objectMapper.writerWithDefaultPrettyPrinter()
    .writeValue(File("configuration.json"), configuration)