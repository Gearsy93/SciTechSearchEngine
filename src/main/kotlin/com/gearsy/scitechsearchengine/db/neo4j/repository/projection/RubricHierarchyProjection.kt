package com.gearsy.scitechsearchengine.db.neo4j.repository.projection

interface RubricHierarchyProjection {
    fun getParentCipher(): String
    fun getParentTitle(): String
    fun getParentEmbedding(): List<Double>
    fun getChildCiphers(): List<String>
}
