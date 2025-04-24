package com.gearsy.scitechsearchengine.service.external

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gearsy.scitechsearchengine.config.properties.YandexApiProperties
import com.gearsy.scitechsearchengine.db.postgres.entity.YandexDocument
import com.gearsy.scitechsearchengine.db.postgres.repository.YandexDocumentRepository
import com.gearsy.scitechsearchengine.model.yandex.SearchPrescription
import com.gearsy.scitechsearchengine.model.yandex.YandexSearchResultModel
import kotlinx.coroutines.*
import org.jpedal.PdfDecoder
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.file.Paths
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

@Service
class YandexService(
    yandexApiProperties: YandexApiProperties,
    private val restTemplate: RestTemplate,
    private val yandexDocumentRepository: YandexDocumentRepository
) {
    private val logger = LoggerFactory.getLogger(YandexService::class.java)


    private val apiKey = yandexApiProperties.apiKey
    private val searchApiUrl = yandexApiProperties.searchApiUrl
    private val resultApiUrl = yandexApiProperties.resultApiUrl
    private val groupsPerPage = yandexApiProperties.groupsPerPage
    private val docsPerGroup = yandexApiProperties.docsPerGroup
    private val region = yandexApiProperties.region
    private val localization = yandexApiProperties.localization
    private val sortMode = yandexApiProperties.sortMode
    private val sortOrder = yandexApiProperties.sortOrder
    private val maxPages = yandexApiProperties.maxPages

    suspend fun processUnstructuredSearch(
        queryId: Long,
        prescriptionList: List<SearchPrescription>
    ): List<YandexSearchResultModel> {

        val allDownloaded = mutableListOf<Deferred<List<YandexSearchResultModel>>>()

        coroutineScope {
            for (prescription in prescriptionList) {
                delay(1000) // Ограничение: 1 запрос в секунду

                val prescriptionCopy = prescription // нужен для использования в launch

                val job = async(Dispatchers.IO) {
                    val results = getPrescriptionResultList(prescriptionCopy.generatedText, prescription, queryId)
                    logger.info("Получены результаты по первому ПП")
                    val resultsWithPrescription = results.map {
                        YandexSearchResultModel(
                            documentId = it.documentId,
                            title = it.title,
                            url = it.url,
                            prescription = prescriptionCopy
                        )
                    }

                    // Асинхронно запускаем скачивание файлов
                    launch {
                        logger.info("Погнала асинхронная загрузка файлов по ПП")
                        downloadFiles(results, UUID.randomUUID().toString(), queryId)
                    }

                    resultsWithPrescription
                }

                allDownloaded += job
            }
        }

        logger.info("Ожиданием загрузки всех ПП")

        val verifiedDownloads = allDownloaded
            .awaitAll()
            .flatten()
            .filter { result ->
                val file = File("src/main/resources/session/yandexDocument/$queryId/${result.documentId}.pdf")
                if (!file.exists()) return@filter false

                return@filter try {
                    val decoder = PdfDecoder()
                    decoder.setExtractionMode(PdfDecoder.TEXT)
                    decoder.openPdfFile(file.absolutePath)
                    decoder.closePdfFile()
                    true
                } catch (e: Exception) {
                    logger.warn("Файл повреждён или не читается: ${file.name}")
                    try {
                        val deleted = file.delete()
                        if (deleted) {
                            logger.info("Удалён битый файл: ${file.name}")
                        } else {
                            logger.warn("Не удалось удалить файл: ${file.name}")
                        }
                    } catch (deleteError: Exception) {
                        logger.error("Ошибка при удалении файла ${file.name}", deleteError)
                    }
                    false
                }
            }

        return verifiedDownloads.distinctBy { it.documentId }

    }

    fun getPrescriptionResultList(queryText: String, prescription: SearchPrescription, queryId: Long): List<YandexSearchResultModel> {

        val yandexDocumentResultList: MutableList<YandexSearchResultModel> = mutableListOf()

        for (page in 0 until maxPages.toInt()) {

            // Создание асинхронного запроса в сервисе
            val requestId = createRequest(queryText, page)

            if (requestId.isEmpty()) {
                continue
            }

            // Ожидание успешного выполнения запроса
            val base64Result = runBlocking {
                getRequestResult(requestId)
            }

            if (base64Result.isEmpty()) {
                continue
            }

            val requestXMLBody = getBase64DecodedBody(base64Result)

            val documentResultList = getDocumentResultList(requestXMLBody, page, prescription)

            yandexDocumentResultList.addAll(documentResultList)
        }
        logger.info("Полученые результаты всех страниц")

        return yandexDocumentResultList
    }

    fun createRequest(query: String, page: Int): String {

        val requestBody = mapOf(
            "query" to mapOf(
                "searchType" to "SEARCH_TYPE_RU",
                "queryText" to query,
                "familyMode" to "FAMILY_MODE_STRICT",
                "page" to page.toString(),
            ),
            "sortSpec" to mapOf(
                "sortMode" to sortMode,
                "sortOrder" to sortOrder
            ),
            "groupSpec" to mapOf(
                "groupMode" to "GROUP_MODE_FLAT",
                "groupsOnPage" to groupsPerPage,
                "docsInGroup" to docsPerGroup
            ),
            "maxPassages" to maxPages,
            "region" to region,
            "l10n" to localization,
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

        logger.info("Получение тела ответа...")
        repeat(5) {

            try {
                val response: ResponseEntity<String> = restTemplate.exchange(
                    "${resultApiUrl}${requestId}", HttpMethod.GET, requestEntity, String::class.java
                )

                val responseBody = response.body ?: "{}"

                if (response.statusCode.is2xxSuccessful) {
                    val jsonResponse = jacksonObjectMapper().readTree(responseBody)

                    try {
                        val status = jsonResponse["done"].booleanValue()

                        if (status) {
                            logger.info("Ответ получен\n")
                            return jsonResponse["response"]["rawData"].textValue()
                        }
                    }
                    catch (e: Exception) {
                        logger.error("Ошибка парсинга ответа", e)
                    }
                }

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
            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val inputStream = ByteArrayInputStream(decodedString.toByteArray(Charsets.UTF_8))
            val document = documentBuilder.parse(inputStream)
            document.documentElement.normalize()

            document
        } catch (ex: Exception) {
            logger.error("Ошибка при декодировании Base64 или разборе XML", ex)
            throw ex
        }
    }

    fun getDocumentResultList(xmlDoc: Document, page: Int, prescription: SearchPrescription): List<YandexSearchResultModel> {
        val results = mutableListOf<YandexSearchResultModel>()
        val docNodes: NodeList = xmlDoc.getElementsByTagName("doc")

        for (i in 0 until docNodes.length) {
            val docElement = docNodes.item(i)
            val docId = docElement.attributes.getNamedItem("id")?.nodeValue ?: continue


            var url: String? = null
            var title = "(без названия)"

            val children = docElement.childNodes
            for (j in 0 until children.length) {
                when (children.item(j).nodeName) {
                    "url" -> url = children.item(j).textContent.trim()
                    "title" -> title = children.item(j).textContent.trim()
                }
            }

            if (url != null) {
                results.add(YandexSearchResultModel(
                    documentId = docId,
                    url = url,
                    title = title,
                    prescription = prescription))
            }
        }
        return results
    }

    suspend fun downloadFiles(
        resultList: List<YandexSearchResultModel>,
        requestId: String,
        queryId: Long
    ): List<YandexSearchResultModel> {
        val downloadPath = "src/main/resources/session/yandexDocument/$queryId"
        createFolder(downloadPath)

        logger.info("Загрузка файлов страницы...")

        val successfulDownloads = resultList.mapNotNull { result ->
            try {
                delay(1000)
                val filePath = Paths.get(downloadPath, "${result.documentId}.pdf").toString()

                URI(result.url).toURL().openStream().use { input ->
                    FileOutputStream(filePath).use { output ->
                        input.copyTo(output)
                    }
                }
                result // возвращаем объект, если загрузка прошла успешно
            } catch (ex: Exception) {
                logger.error("Ошибка при загрузке ${result.url}: ${ex.message}")
                null // отбрасываем объект, если произошла ошибка
            }
        }

        val documentsToSave = successfulDownloads.map {
            YandexDocument(
                queryId = queryId,
                documentId = it.documentId,
                title = it.title,
                link = it.url
            )
        }

        yandexDocumentRepository.saveAll(documentsToSave)

        logger.info("Загрузка завершена. Успешно загружено: ${successfulDownloads.size} файлов.\n")

        return successfulDownloads
    }


    fun createFolder(path: String): Boolean {
        val folder = File(path)
        return if (!folder.exists()) {
            folder.mkdirs()
        } else {
            false
        }
    }
}