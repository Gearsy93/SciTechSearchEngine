package com.gearsy.scitechsearchengine.service.externalApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gearsy.scitechsearchengine.config.properties.YandexApiProperties
import com.gearsy.scitechsearchengine.model.YandexSearchResult
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.util.*
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Paths

@Service
class YandexAPIInteractionService(
    yandexApiProperties: YandexApiProperties,
    private val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(YandexAPIInteractionService::class.java)
    private val apiKey = yandexApiProperties.apiKey
    private val searchApiUrl = yandexApiProperties.searchApiUrl
    private val resultApiUrl = yandexApiProperties.resultApiUrl

    fun makeRequest() {
        val query = "научно-техническая информация"

        // Создание асинхронного запроса в сервисе
        val requestId = createRequest(query)

        if (requestId.isEmpty()) {
            return
        }

        // Ожидание успешного выполнения запроса
        val base64Result = runBlocking {
            getRequestResult(requestId)
        }

        if (base64Result.isEmpty()) {
            return
        }

        val requestXMLBody = getBase64DecodedBody(base64Result)
        val requestBody = documentToString(requestXMLBody)

        val documentResultList = getDocumentResultObjectList(requestXMLBody)

        downloadFiles(documentResultList, requestId)
    }

    fun createRequest(query: String): String {

        val mimeTypes = listOf("pdf").joinToString(" ") { "mime:$it" }

        val requestBody = mapOf(
            "query" to mapOf(
                "searchType" to "SEARCH_TYPE_RU",
                "queryText" to "$query $mimeTypes",
                "familyMode" to "FAMILY_MODE_STRICT",
                "page" to "1"
            ),
            "sortSpec" to mapOf(
                "sortMode" to "SORT_MODE_BY_RELEVANCE",
                "sortOrder" to "SORT_ORDER_DESC"
            ),
            "groupSpec" to mapOf(
                "groupMode" to "GROUP_MODE_DEEP",
                "groupsOnPage" to "5",
                "docsInGroup" to "3"
            ),
            "maxPassages" to "3",
            "region" to "59",
            "l10N" to "LOCALIZATION_RU",
            "folderId" to "b1g1hhjt0m1luncs3rbf"
        )

        val headers = HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity(requestBody, headers)

        try {
            val response: ResponseEntity<String> = restTemplate.exchange(
                searchApiUrl, HttpMethod.POST, requestEntity, String::class.java
            )

            // Проверяем, что тело ответа не null и не пустое
            val responseBody = response.body ?: "{}"

            val jsonResponse = jacksonObjectMapper().readTree(responseBody)

            logger.info("Yandex API Response: ${jsonResponse.toPrettyString()}")

            if (response.statusCode.is2xxSuccessful) {
                return jsonResponse["id"].textValue()
            }
            else {
                return ""
            }
        } catch (ex: Exception) {
            logger.error("Error while calling Yandex API", ex)
        }

        return ""
    }

    private suspend fun getRequestResult(requestId: String): String {

        val headers = HttpHeaders().apply {
            set("Authorization", "Api-Key $apiKey")
            contentType = MediaType.APPLICATION_JSON
        }

        val requestEntity = HttpEntity("", headers)

        repeat(5) { attempt ->

            println("Попытка #${attempt + 1}")

            try {
                val response: ResponseEntity<String> = restTemplate.exchange(
                    "${resultApiUrl}${requestId}", HttpMethod.GET, requestEntity, String::class.java
                )


                // Проверяем, что тело ответа не null и не пустое
                val responseBody = response.body ?: "{}"

                if (response.statusCode.is2xxSuccessful) {
                    val jsonResponse = jacksonObjectMapper().readTree(responseBody)

                    logger.info("Yandex API Response: ${jsonResponse.toPrettyString()}")

                    try {
                        val status = jsonResponse["done"].booleanValue()

                        if (status) {
                            return jsonResponse["response"]["rawData"].textValue()
                        }
                    }
                    catch (e: Exception) {
                        logger.error("Ошибка парсинга ответа", e)
                    }
                }

//            return jsonResponse["id"].toString()
            } catch (ex: Exception) {
                logger.error("Error while calling Yandex API", ex)
            }

            delay(1000)
        }

        logger.error("Не удалось получить ответ")

        return ""
    }

    private fun getBase64DecodedBody(base64Result: String): Document {
        return try {
            val decodedBytes = Base64.getDecoder().decode(base64Result)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            // Обрабатываем XML вместо JSON
            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val inputStream = ByteArrayInputStream(decodedString.toByteArray(Charsets.UTF_8))
            val document = documentBuilder.parse(inputStream)
            document.documentElement.normalize()

            document // Возвращаем XML-документ
        } catch (ex: Exception) {
            logger.error("Ошибка при декодировании Base64 или разборе XML", ex)
            throw ex
        }
    }

    fun documentToString(doc: Document): String {
        return try {
            val transformer = TransformerFactory.newInstance().newTransformer()
            val writer = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))
            writer.toString()  // Получаем полное XML в виде строки
        } catch (ex: Exception) {
            "Ошибка преобразования XML в строку: ${ex.message}"
        }
    }

    fun getDocumentResultObjectList(xmlDoc: Document): List<YandexSearchResult> {
        val results = mutableListOf<YandexSearchResult>()
        val docNodes: NodeList = xmlDoc.getElementsByTagName("doc")

        for (i in 0 until docNodes.length) {
            val docElement = docNodes.item(i)
            val docId = docElement.attributes.getNamedItem("id")?.nodeValue ?: continue
            val urlNode = docElement.childNodes

            var url: String? = null
            for (j in 0 until urlNode.length) {
                if (urlNode.item(j).nodeName == "url") {
                    url = urlNode.item(j).textContent
                    break
                }
            }

            if (url != null) {
                results.add(YandexSearchResult(documentId = docId, url = url))
            }
        }
        return results
    }

    fun downloadFiles(resultList: List<YandexSearchResult>, requestId: String) {
        val downloadPath = "src/main/resources/yandexDownloads/$requestId"
        val isCreated = createFolder(downloadPath)
        println(if (isCreated) "Папка создана: $downloadPath" else "Папка уже существует")

        resultList.forEach { result ->
            try {
                val fileUrl = URL(result.url)
                var fileName = result.documentId + "_" + fileUrl.path.substringAfterLast("/")
                val filePath = Paths.get(downloadPath, fileName).toString()

                // Проверяем, есть ли у файла расширение, если нет – добавляем .pdf
                if (!fileName.contains(".")) {
                    fileName += ".pdf"
                }

                println("Скачивание файла: ${result.url} -> $filePath")

                URL(result.url).openStream().use { input ->
                    FileOutputStream(filePath).use { output ->
                        input.copyTo(output)
                    }
                }

                println("Файл сохранён: $filePath")
            } catch (ex: Exception) {
                println("Ошибка при загрузке ${result.url}: ${ex.message}")
            }
        }
    }

    fun createFolder(path: String): Boolean {
        val folder = File(path)
        return if (!folder.exists()) {
            folder.mkdirs() // Создаёт папку и все вложенные директории
        } else {
            false // Папка уже существует
        }
    }

}