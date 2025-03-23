package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "model.deepvk.onnx")
class DeepVKONNXModelProperties {
    lateinit var name: String
    lateinit var modelPath: String
    lateinit var tokenizerPath: String
}
