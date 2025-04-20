package com.gearsy.scitechsearchengine.service.viniti.document

import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.repository.VinitiDocumentRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiDocumentMeta
import com.gearsy.scitechsearchengine.db.postgres.entity.VinitiDocumentRubricTerm
import com.gearsy.scitechsearchengine.db.postgres.entity.VinitiDocument
import org.springframework.stereotype.Service

@Service
class VinitiDocumentService(
    private val vinitiDocumentRepository: VinitiDocumentRepository,
) {

    fun saveVinitiResults(query: Query, documents: List<VinitiDocumentMeta>) {
        val entities = documents.map { doc ->
            val docEntity = VinitiDocument(
                query = query,
                title = doc.title,
                annotation = doc.annotation,
                translateTitle = doc.translateTitle,
                link = doc.link,
                language = doc.language ?: "неизвестен"
            )

            val termEntities = doc.rubricTermDataList.flatMap { rubric ->
                rubric.keywords.orEmpty().map { keyword ->
                    VinitiDocumentRubricTerm(
                        document = docEntity,
                        rubricCipher = rubric.rubricCipher,
                        keyword = keyword
                    )
                }
            }

            docEntity.rubricTerms.addAll(termEntities)
            docEntity
        }

        vinitiDocumentRepository.saveAll(entities)
    }

}