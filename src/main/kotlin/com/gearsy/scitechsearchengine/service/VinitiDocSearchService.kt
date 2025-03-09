package com.gearsy.scitechsearchengine.service

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VinitiDocSearchService(
    vinitiECatalogProperties: VinitiECatalogProperties
) {

    private val logger = LoggerFactory.getLogger(VinitiDocSearchService::class.java)

    private val startPageUrl = vinitiECatalogProperties.startPageUrl

    // Инициализация драйвера
    private lateinit var driver: ChromeDriver
    private lateinit var wait: WebDriverWait

    fun makeRequest() {

        val testCSCTIRubricCipher = "27"


    }

    fun processSearchParams() {

        // Активация пункта "Общие для выбранных объектов"
        val radioButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_RBList_1_1"))
        )
        radioButton.click()
        println("Выбран пункт с критериями поиска (radioButton)")

        // Ожидание появления пунктов по выбору критерия поиска
        val criteriaSpanContainer = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_ContentPlaceHolder1_CBList_1"))
        )


    }
}