package com.gearsy.scitechsearchengine.db.neo4j.repository.projection

interface RubricHierarchyProjection {
    fun getParentCipher(): String
    fun getParentTitle(): String
    fun getParentEmbedding(): List<Float>
    fun getChildCiphers(): List<String>
}
