package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "relevant-term-rubric")
class RelevantTermRubricProperties {
    lateinit var penaltyLevelZero: String
    lateinit var penaltyLevelOne: String
    lateinit var penaltyOtherLevel: String
    lateinit var penaltyHardcode: String
    lateinit var penaltyTitle: String
    lateinit var simImproveThreshold: String
    lateinit var minSimilarity: String
}