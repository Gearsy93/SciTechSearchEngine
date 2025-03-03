package com.gearsy.scitechsearchengine.service

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxTensor
import com.gearsy.scitechsearchengine.config.properties.DeepVKModelProperties
import org.springframework.stereotype.Service
import java.nio.file.Paths

@Service
class EmbeddingProcessService(private val modelLoader: ModelLoaderService,
                              private val modelProperties: DeepVKModelProperties
) {

    fun generateEmbeddings(texts: List<String>): List<List<Float>> {
        val environment = modelLoader.environment
        val session = modelLoader.onnxSession
        val tokenizer: HuggingFaceTokenizer =
            HuggingFaceTokenizer.newInstance(Paths.get(modelProperties.tokenizerPath))

        val encodings = tokenizer.batchEncode(texts)

        val inputIdsData = encodings.map { it.ids }.toTypedArray()
        val attentionMaskData = encodings.map { it.attentionMask }.toTypedArray()

        // Создание тензоров
        val inputIdsTensor = OnnxTensor.createTensor(environment, inputIdsData)
        val attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMaskData)

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