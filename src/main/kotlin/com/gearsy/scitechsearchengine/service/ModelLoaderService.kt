package com.gearsy.scitechsearchengine.service

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.providers.OrtCUDAProviderOptions
import com.gearsy.scitechsearchengine.config.properties.DeepVKModelProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.stereotype.Service
import java.io.File

@Service
class ModelLoaderService(private val modelProperties: DeepVKModelProperties,
    private val args: ApplicationArguments
) {

    private val logger = LoggerFactory.getLogger(ModelLoaderService::class.java)

    lateinit var onnxSession: OrtSession
    lateinit var environment: OrtEnvironment

    @PostConstruct
    fun loadModelOnStartup() {
        if (args.sourceArgs.contains("-generateCSCSTIThesaurusVectors") ||
            args.sourceArgs.contains("-getQueryRelevantCSCSTIRubricList")) {
            val modelPath = File(modelProperties.modelPath)

            if (!modelPath.exists()) {
                logger.error("Модель не найдена по пути: ${modelProperties.modelPath}")
                return
            }

            logger.info("Модель '${modelProperties.name}' найдена по пути: ${modelProperties.modelPath}")

            loadOnnxModel(modelProperties.modelPath)
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
