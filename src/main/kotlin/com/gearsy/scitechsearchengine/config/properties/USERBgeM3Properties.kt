package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "language-model.user-bge-m3")
class USERBgeM3Properties {
    lateinit var name: String
    lateinit var onnxPath: String
    lateinit var safetensorsPath: String
    lateinit var tokenizerPath: String
}
