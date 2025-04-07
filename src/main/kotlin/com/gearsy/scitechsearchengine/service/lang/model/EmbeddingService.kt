package com.gearsy.scitechsearchengine.service.lang.model

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import com.gearsy.scitechsearchengine.config.properties.DeepVKONNXModelProperties
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
                       private val modelProperties: DeepVKONNXModelProperties
) {

    suspend fun requestPythonEmbedding(term: String, context: List<String>, title: String): List<Float> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        val response: EmbeddingResponseDTO = client.post("http://localhost:8001/embedding") {
            contentType(ContentType.Application.Json)
            setBody(EmbeddingRequestDTO(term, context, title))
        }.body()

        client.close()
        return response.embedding
    }

    suspend fun requestBatchEmbeddings(requests: List<EmbeddingRequestDTO>): List<List<Float>> {
        println("request count: ${requests.size}")
        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 180_000  // 120 секунд
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

        val response: List<EmbeddingResponseDTO> = client.post("http://localhost:8001/embedding/batch") {
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

        val response: EmbeddingResponseDTO = client.post("http://localhost:8001/embedding/rubric") {
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
            HuggingFaceTokenizer.newInstance(Paths.get(modelProperties.tokenizerPath))

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
            println(e)
            throw e
        }

        return try {
            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to attentionMaskTensor
            )

            session.run(inputs).use { result ->
                val outputTensor = result["sentence_embedding"] // Используем "sentence_embedding"
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