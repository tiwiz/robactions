#!/usr/bin/env kscript

@file:DependsOn("org.jsoup:jsoup:1.13.1")
@file:DependsOn("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.2")


import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

data class DayLight(
    val image: String,
    val color: String,
    val reason: String,
    val date: String,
    val colorHex: Array<String>? = null
)

data class Lights(
    val todayColor: String,
    val picture: String,
    val calendar: List<DayLight>,
    val colorHex: Array<String>? = null
)

val ROOT = "https://www.esbnyc.com"

fun LocalDateTime.midnight(): LocalDateTime =
    withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(1)

val dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX")

fun String.toISO8601(): String =
    LocalDate.parse(this, DateTimeFormatter.ISO_DATE)
        .atStartOfDay()
        .toISO8601()

fun LocalDateTime.toISO8601(): String = atOffset(ZoneOffset.UTC).format(dateFormat)

fun Document.todayLights() : Pair<String, String> =
    todayBackground(this) to todayColor(this)

fun todayBackground(doc: Document) : String =
    doc.select("div.background-image-wrapper")
        .attr("style")
        .split("url(")
        .last()
        .replace(")", "")

fun todayColor(doc: Document) : String =
    doc.select("div.is-today")
        .select("div.info")
        .select("h3")
        .html()

fun Document.otherLights() : List<DayLight> =
    select("div.view-content > div").map { it.extractDayData() }

fun Element.extractDayData(): DayLight {
    val date = date()

    val (color, reason, picture) = with(colorElement()) {
        Triple(color(), reason(), picture())
    }

    return DayLight(
        image = picture,
        color = color.format(),
        reason = reason,
        date = date,
        colorHex = color.toColorArray()
    )
}

fun Element.date() : String = select("article.lse")
    .attr("data-date")
    .toISO8601()

fun Element.colorElement() : Element = select("div.field_light").first()

fun Element.color() : String = select("div > h3 > div").text()

fun Element.reason() : String = select("div.clearfix.text-formatted.field_description > p").text()

fun Element.picture() : String =
    "$ROOT${select("div.media-image > img").attr("src")}"

class Scraper {

    fun fetchData(): Lights {
        val (picture, color) = Jsoup.connect("https://www.esbnyc.com/about/tower-lights")
            .get()
            .todayLights()

        val days = Jsoup.connect("https://www.esbnyc.com/about/tower-lights/calendar").get()
            .otherLights()

        return Lights(
            todayColor = color.format(),
            picture = picture,
            calendar = days,
            colorHex = color.toColorArray()
        )
    }
}

val htmlColors = hashMapOf(
    "Gainsboro".toUpperCase(Locale.getDefault()) to "#DCDCDC",
    "LightGray".toUpperCase(Locale.getDefault()) to "#D3D3D3",
    "Silver".toUpperCase(Locale.getDefault()) to "#C0C0C0",
    "DarkGray".toUpperCase(Locale.getDefault()) to "#A9A9A9",
    "DimGray".toUpperCase(Locale.getDefault()) to "#696969",
    "Gray".toUpperCase(Locale.getDefault()) to "#808080",
    "LightSlateGray".toUpperCase(Locale.getDefault()) to "#778899",
    "SlateGray".toUpperCase(Locale.getDefault()) to "#708090",
    "DarkSlateGray".toUpperCase(Locale.getDefault()) to "#2F4F4F",
    "Black".toUpperCase(Locale.getDefault()) to "#000000",
    "White".toUpperCase(Locale.getDefault()) to "#FFFFFF",
    "Snow".toUpperCase(Locale.getDefault()) to "#FFFAFA",
    "HoneyDew".toUpperCase(Locale.getDefault()) to "#F0FFF0",
    "MintCream".toUpperCase(Locale.getDefault()) to "#F5FFFA",
    "Azure".toUpperCase(Locale.getDefault()) to "#F0FFFF",
    "AliceBlue".toUpperCase(Locale.getDefault()) to "#F0F8FF",
    "GhostWhite".toUpperCase(Locale.getDefault()) to "#F8F8FF",
    "WhiteSmoke".toUpperCase(Locale.getDefault()) to "#F5F5F5",
    "SeaShell".toUpperCase(Locale.getDefault()) to "#FFF5EE",
    "Beige".toUpperCase(Locale.getDefault()) to "#F5F5DC",
    "OldLace".toUpperCase(Locale.getDefault()) to "#FDF5E6",
    "FloralWhite".toUpperCase(Locale.getDefault()) to "#FFFAF0",
    "Ivory".toUpperCase(Locale.getDefault()) to "#FFFFF0",
    "AntiqueWhite".toUpperCase(Locale.getDefault()) to "#FAEBD7",
    "Linen".toUpperCase(Locale.getDefault()) to "#FAF0E6",
    "LavenderBlush".toUpperCase(Locale.getDefault()) to "#FFF0F5",
    "MistyRose".toUpperCase(Locale.getDefault()) to "#FFE4E1",
    "Cornsilk".toUpperCase(Locale.getDefault()) to "#FFF8DC",
    "BlanchedAlmond".toUpperCase(Locale.getDefault()) to "#FFEBCD",
    "Bisque".toUpperCase(Locale.getDefault()) to "#FFE4C4",
    "NavajoWhite".toUpperCase(Locale.getDefault()) to "#FFDEAD",
    "Wheat".toUpperCase(Locale.getDefault()) to "#F5DEB3",
    "BurlyWood".toUpperCase(Locale.getDefault()) to "#DEB887",
    "Tan".toUpperCase(Locale.getDefault()) to "#D2B48C",
    "RosyBrown".toUpperCase(Locale.getDefault()) to "#BC8F8F",
    "SandyBrown".toUpperCase(Locale.getDefault()) to "#F4A460",
    "GoldenRod".toUpperCase(Locale.getDefault()) to "#DAA520",
    "DarkGoldenRod".toUpperCase(Locale.getDefault()) to "#B8860B",
    "Peru".toUpperCase(Locale.getDefault()) to "#CD853F",
    "Chocolate".toUpperCase(Locale.getDefault()) to "#D2691E",
    "Olive".toUpperCase(Locale.getDefault()) to "#808000",
    "SaddleBrown".toUpperCase(Locale.getDefault()) to "#8B4513",
    "Sienna".toUpperCase(Locale.getDefault()) to "#A0522D",
    "Brown".toUpperCase(Locale.getDefault()) to "#A52A2A",
    "Maroon".toUpperCase(Locale.getDefault()) to "#800000",
    "CadetBlue".toUpperCase(Locale.getDefault()) to "#5F9EA0",
    "SteelBlue".toUpperCase(Locale.getDefault()) to "#4682B4",
    "LightSteelBlue".toUpperCase(Locale.getDefault()) to "#B0C4DE",
    "LightBlue".toUpperCase(Locale.getDefault()) to "#ADD8E6",
    "PowderBlue".toUpperCase(Locale.getDefault()) to "#B0E0E6",
    "LightSkyBlue".toUpperCase(Locale.getDefault()) to "#87CEFA",
    "SkyBlue".toUpperCase(Locale.getDefault()) to "#87CEEB",
    "CornflowerBlue".toUpperCase(Locale.getDefault()) to "#6495ED",
    "DeepSkyBlue".toUpperCase(Locale.getDefault()) to "#00BFFF",
    "DodgerBlue".toUpperCase(Locale.getDefault()) to "#1E90FF",
    "RoyalBlue".toUpperCase(Locale.getDefault()) to "#4169E1",
    "Blue".toUpperCase(Locale.getDefault()) to "#0000FF",
    "MediumBlue".toUpperCase(Locale.getDefault()) to "#0000CD",
    "DarkBlue".toUpperCase(Locale.getDefault()) to "#00008B",
    "Navy".toUpperCase(Locale.getDefault()) to "#000080",
    "MidnightBlue".toUpperCase(Locale.getDefault()) to "#191970",
    "Aqua".toUpperCase(Locale.getDefault()) to "#00FFFF",
    "Cyan".toUpperCase(Locale.getDefault()) to "#00FFFF",
    "LightCyan".toUpperCase(Locale.getDefault()) to "#E0FFFF",
    "PaleTurquoise".toUpperCase(Locale.getDefault()) to "#AFEEEE",
    "Aquamarine".toUpperCase(Locale.getDefault()) to "#7FFFD4",
    "Turquoise".toUpperCase(Locale.getDefault()) to "#40E0D0",
    "MediumTurquoise".toUpperCase(Locale.getDefault()) to "#48D1CC",
    "DarkTurquoise".toUpperCase(Locale.getDefault()) to "#00CED1",
    "GreenYellow".toUpperCase(Locale.getDefault()) to "#ADFF2F",
    "Chartreuse".toUpperCase(Locale.getDefault()) to "#7FFF00",
    "LawnGreen".toUpperCase(Locale.getDefault()) to "#7CFC00",
    "Lime".toUpperCase(Locale.getDefault()) to "#00FF00",
    "LimeGreen".toUpperCase(Locale.getDefault()) to "#32CD32",
    "PaleGreen".toUpperCase(Locale.getDefault()) to "#98FB98",
    "LightGreen".toUpperCase(Locale.getDefault()) to "#90EE90",
    "MediumSpringGreen".toUpperCase(Locale.getDefault()) to "#00FA9A",
    "SpringGreen".toUpperCase(Locale.getDefault()) to "#00FF7F",
    "MediumSeaGreen".toUpperCase(Locale.getDefault()) to "#3CB371",
    "SeaGreen".toUpperCase(Locale.getDefault()) to "#2E8B57",
    "ForestGreen".toUpperCase(Locale.getDefault()) to "#228B22",
    "Green".toUpperCase(Locale.getDefault()) to "#008000",
    "DarkGreen".toUpperCase(Locale.getDefault()) to "#006400",
    "YellowGreen".toUpperCase(Locale.getDefault()) to "#9ACD32",
    "OliveDrab".toUpperCase(Locale.getDefault()) to "#6B8E23",
    "DarkOliveGreen".toUpperCase(Locale.getDefault()) to "#556B2F",
    "MediumAquaMarine".toUpperCase(Locale.getDefault()) to "#66CDAA",
    "DarkSeaGreen".toUpperCase(Locale.getDefault()) to "#8FBC8F",
    "LightSeaGreen".toUpperCase(Locale.getDefault()) to "#20B2AA",
    "DarkCyan".toUpperCase(Locale.getDefault()) to "#008B8B",
    "Teal".toUpperCase(Locale.getDefault()) to "#008080",
    "Gold".toUpperCase(Locale.getDefault()) to "#FFD700",
    "Yellow".toUpperCase(Locale.getDefault()) to "#FFFF00",
    "LightYellow".toUpperCase(Locale.getDefault()) to "#FFFFE0",
    "LemonChiffon".toUpperCase(Locale.getDefault()) to "#FFFACD",
    "LightGoldenRodYellow".toUpperCase(Locale.getDefault()) to "#FAFAD2",
    "PapayaWhip".toUpperCase(Locale.getDefault()) to "#FFEFD5",
    "Moccasin".toUpperCase(Locale.getDefault()) to "#FFE4B5",
    "PeachPuff".toUpperCase(Locale.getDefault()) to "#FFDAB9",
    "PaleGoldenRod".toUpperCase(Locale.getDefault()) to "#EEE8AA",
    "Khaki".toUpperCase(Locale.getDefault()) to "#F0E68C",
    "DarkKhaki".toUpperCase(Locale.getDefault()) to "#BDB76B",
    "Orange".toUpperCase(Locale.getDefault()) to "#FFA500",
    "DarkOrange".toUpperCase(Locale.getDefault()) to "#FF8C00",
    "Coral".toUpperCase(Locale.getDefault()) to "#FF7F50",
    "Tomato".toUpperCase(Locale.getDefault()) to "#FF6347",
    "OrangeRed".toUpperCase(Locale.getDefault()) to "#FF4500",
    "LightSalmon".toUpperCase(Locale.getDefault()) to "#FFA07A",
    "Salmon".toUpperCase(Locale.getDefault()) to "#FA8072",
    "DarkSalmon".toUpperCase(Locale.getDefault()) to "#E9967A",
    "LightCoral".toUpperCase(Locale.getDefault()) to "#F08080",
    "IndianRed ".toUpperCase(Locale.getDefault()) to "#CD5C5C",
    "Crimson".toUpperCase(Locale.getDefault()) to "#DC143C",
    "Red".toUpperCase(Locale.getDefault()) to "#FF0000",
    "FireBrick".toUpperCase(Locale.getDefault()) to "#B22222",
    "DarkRed".toUpperCase(Locale.getDefault()) to "#8B0000",
    "Lavender".toUpperCase(Locale.getDefault()) to "#E6E6FA",
    "Thistle".toUpperCase(Locale.getDefault()) to "#D8BFD8",
    "Plum".toUpperCase(Locale.getDefault()) to "#DDA0DD",
    "Orchid".toUpperCase(Locale.getDefault()) to "#DA70D6",
    "Violet".toUpperCase(Locale.getDefault()) to "#EE82EE",
    "Fuchsia".toUpperCase(Locale.getDefault()) to "#FF00FF",
    "Magenta".toUpperCase(Locale.getDefault()) to "#FF00FF",
    "MediumOrchid".toUpperCase(Locale.getDefault()) to "#BA55D3",
    "DarkOrchid".toUpperCase(Locale.getDefault()) to "#9932CC",
    "DarkViolet".toUpperCase(Locale.getDefault()) to "#9400D3",
    "BlueViolet".toUpperCase(Locale.getDefault()) to "#8A2BE2",
    "DarkMagenta".toUpperCase(Locale.getDefault()) to "#8B008B",
    "Purple".toUpperCase(Locale.getDefault()) to "#800080",
    "MediumPurple".toUpperCase(Locale.getDefault()) to "#9370DB",
    "MediumSlateBlue".toUpperCase(Locale.getDefault()) to "#7B68EE",
    "SlateBlue".toUpperCase(Locale.getDefault()) to "#6A5ACD",
    "DarkSlateBlue".toUpperCase(Locale.getDefault()) to "#483D8B",
    "RebeccaPurple".toUpperCase(Locale.getDefault()) to "#663399",
    "Indigo ".toUpperCase(Locale.getDefault()) to "#4B0082",
    "Pink".toUpperCase(Locale.getDefault()) to "#FFC0CB",
    "LightPink".toUpperCase(Locale.getDefault()) to "#FFB6C1",
    "HotPink".toUpperCase(Locale.getDefault()) to "#FF69B4",
    "DeepPink".toUpperCase(Locale.getDefault()) to "#FF1493",
    "PaleVioletRed".toUpperCase(Locale.getDefault()) to "#DB7093",
    "MediumVioletRed".toUpperCase(Locale.getDefault()) to "#C71585"
)

fun String.format() : String {
    val message = replace(" ", "")

    return if(message.all { it.isUpperCase() } && contains(" ")) {
        split(" ").joinToString(" ") { it.toLowerCase().capitalize() }
    } else {
        this
    }
}

fun String.toColorArray(): Array<String> =
    removeWordsOtherThanColors()
        .split("_")
        .mapNotNull { htmlColors[it.toUpperCase(Locale.getDefault())] }
        .toTypedArray()


fun String.removeWordsOtherThanColors(): String =
    replace("and", "_")
        .replace("signature", "", ignoreCase = true)
        .replace("color", "", ignoreCase = true)
        .replace("-", "_")
        .replace(",", "_")
        .replace("&", "_")
        .replace("\\s+", "_")
        .replace(" ", "")

val data = Scraper().fetchData()
val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

mapper.writerWithDefaultPrettyPrinter().writeValue(File("lights.json"), data)