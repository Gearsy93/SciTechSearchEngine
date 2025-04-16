package com.gearsy.scitechsearchengine.service.lang.model

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.providers.OrtCUDAProviderOptions
import com.gearsy.scitechsearchengine.config.properties.USERBgeM3Properties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.stereotype.Service
import java.io.File

@Service
class ModelLoaderService(private val uSERBgeM3Properties: USERBgeM3Properties,
                         private val args: ApplicationArguments
) {

    private val logger = LoggerFactory.getLogger(ModelLoaderService::class.java)

    lateinit var onnxSession: OrtSession
    lateinit var environment: OrtEnvironment

    private val modelLoadingFlags = listOf(
        "-get_query_relevant_rubric_term_list",
        "-run_search_conveyor"
    )

    @PostConstruct
    fun loadModelOnStartup() {
        if (args.sourceArgs.any { it in modelLoadingFlags }) {
            val modelPath = File(uSERBgeM3Properties.onnxPath)

            if (!modelPath.exists()) {
                logger.error("Модель не найдена по пути: ${uSERBgeM3Properties.onnxPath}")
                return
            }

            logger.info("Модель '${uSERBgeM3Properties.name}' найдена по пути: ${uSERBgeM3Properties.onnxPath}")

            loadOnnxModel(uSERBgeM3Properties.onnxPath)
        }
    }

    private fun loadOnnxModel(modelPath: String) {
        logger.info("Загрузка ONNX-модели по пути $modelPath...")

        try {
            environment = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions()

            options.addCUDA(OrtCUDAProviderOptions())

            onnxSession = environment.createSession(modelPath, options)
            logger.info("Модель загружена")
        } catch (e: Exception) {
            logger.error("Ошибка загрузки модели: ${e.message}")
            return
        }
    }
}
