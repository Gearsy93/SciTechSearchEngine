package com.gearsy.scitechsearchengine.service.thesaurus.terminological

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class RubricImportFromFileService(
    private val rubricDBImportService: RubricDBImportService,
) {
    val mapper = jacksonObjectMapper()

    private val logger = LoggerFactory.getLogger(RubricImportFromFileService::class.java)

    fun fillTermThesaurus(rubricCipher: String) {

        // Путь к файлу JSON
        val rubricEmbeddingsPath = "src/main/resources/rubricator/embedding/$rubricCipher.json"
        val rootRubric: RubricImportDTO = mapper.readValue(File(rubricEmbeddingsPath))

        // Открываем сессию и заполняем базу
        rubricDBImportService.insertRubricsAndTermsHierarchy(rootRubric, ThesaurusType.TERMINOLOGICAL)
    }
}