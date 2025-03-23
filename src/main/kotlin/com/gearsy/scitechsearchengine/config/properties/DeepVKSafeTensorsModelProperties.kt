package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "model.deepvk.safetensors")
class DeepVKSafeTensorsModelProperties {
    lateinit var modelPath: String
}