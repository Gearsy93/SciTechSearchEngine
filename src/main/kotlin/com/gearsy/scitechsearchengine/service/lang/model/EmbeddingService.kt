package com.gearsy.scitechsearchengine.service.lang.model

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import com.gearsy.scitechsearchengine.config.properties.EmbeddingServiceProperties
import com.gearsy.scitechsearchengine.config.properties.USERBgeM3Properties
import com.gearsy.scitechsearchengine.controller.dto.embedding.EmbeddingRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.embedding.EmbeddingResponseDTO
import com.gearsy.scitechsearchengine.controller.dto.embedding.RubricEmbeddingRequestDTO
import org.springframework.stereotype.Service
import java.nio.file.Paths
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType


@Service
class EmbeddingService(private val modelLoader: ModelLoaderService,
                       private val userBgeM3Properties: USERBgeM3Properties,
                       embeddingServiceProperties: EmbeddingServiceProperties,
) {

    val serviceAddress = "http://${embeddingServiceProperties.host}:${embeddingServiceProperties.port}"

    suspend fun generateTermListEmbeddings(
        terms: List<Map<String, Any?>>,
        title: String
    ): List<Map<String, Any?>> {
        if (terms.isEmpty()) return emptyList()

        val pythonServiceRequests = terms.map { term ->
            EmbeddingRequestDTO(
                term = term["content"] as String,
                context = terms.filter { it["content"] != term["content"] }
                    .mapNotNull { it["content"] as? String },
                title = title
            )
        }

        val chunked = pythonServiceRequests.chunked(128)
        val embeddings = mutableListOf<List<Float>>()

        for (chunk in chunked) {
            embeddings += requestBatchEmbeddings(chunk)
        }

        return terms.mapIndexed { index, term ->
            mapOf(
                "content" to term["content"] as String,
                "embedding" to embeddings[index],
                "rubricatorId" to term["rubricatorId"]
            )
        }
    }

    suspend fun generateEmbeddingsWithExternalContext(
        terms: List<Map<String, Any>>,
        rubricTitle: String,
        contextTerms: List<String>
    ): List<EmbeddingResponseDTO> {
        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 180_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val payload: List<EmbeddingRequestDTO> = terms.map { term ->
            EmbeddingRequestDTO(
                term = term["content"] as String,
                context = contextTerms,
                title = rubricTitle
            )
        }

        val response: List<EmbeddingResponseDTO> = client.post("${serviceAddress}/embedding/batch") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()

        client.close()
        return response
    }



    suspend fun requestBatchEmbeddings(requests: List<EmbeddingRequestDTO>): List<List<Float>> {
        println("Request count: ${requests.size}")
        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 180_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val response: List<EmbeddingResponseDTO> = client.post("${serviceAddress}/embedding/batch") {
            contentType(ContentType.Application.Json)
            setBody(requests)
        }.body()

        client.close()
        return response.map { it.embedding }
    }

    suspend fun requestRubricEmbedding(title: String, terms: List<String>): List<Float> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
        }

        val payload = RubricEmbeddingRequestDTO(title, terms)

        val response: EmbeddingResponseDTO = client.post("${serviceAddress}/embedding/rubric") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()

        client.close()
        return response.embedding
    }

    fun generateEmbeddings(texts: List<String>): List<List<Float>> {
        val environment = modelLoader.environment
        val session = modelLoader.onnxSession
        val tokenizer: HuggingFaceTokenizer =
            HuggingFaceTokenizer.newInstance(Paths.get(userBgeM3Properties.tokenizerPath))

        // Токенизация
        val encodings = tokenizer.batchEncode(texts)
        val inputIdsData = encodings.map { it.ids }.toTypedArray()
        val attentionMaskData = encodings.map { it.attentionMask }.toTypedArray()

        // Создание тензоров
        val inputIdsTensor: OnnxTensor
        val attentionMaskTensor: OnnxTensor
        try {
            inputIdsTensor = OnnxTensor.createTensor(environment, inputIdsData)
            attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMaskData)
        }
        catch (e: Exception) {
            throw e
        }

        // Работа в моделью
        return try {
            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to attentionMaskTensor
            )

            session.run(inputs).use { result ->
                val outputTensor = result["sentence_embedding"]
                    ?.takeIf { it.isPresent }
                    ?.get()
                    ?.value as? Array<FloatArray>
                    ?: throw IllegalStateException("Empty output")

                // Возвращаем список эмбеддингов для всех текстов
                outputTensor.map { it.toList() }
            }
        } finally {
            inputIdsTensor.close()
            attentionMaskTensor.close()
        }
    }

}