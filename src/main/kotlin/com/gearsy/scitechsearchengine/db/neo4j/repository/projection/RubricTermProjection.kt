package com.gearsy.scitechsearchengine.db.neo4j.repository.projection

interface RubricTermProjection {
    fun getRubricCipher(): String
    fun getContent(): String
    fun getEmbedding(): List<Float>
}
