package com.gearsy.scitechsearchengine.service.external

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.model.viniti.catalog.RubricTermData
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiDocumentMeta
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import io.github.bonigarcia.wdm.WebDriverManager
import jakarta.annotation.PreDestroy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration


@Service
class VinitiSearchService(
    vinitiECatalogProperties: VinitiECatalogProperties
) {

    private val logger = LoggerFactory.getLogger(VinitiSearchService::class.java)

    // Данные для работы с сайтом ВИНИТИ
    private val startPageUrl = vinitiECatalogProperties.startPageUrl
    private val username = vinitiECatalogProperties.username
    private val password = vinitiECatalogProperties.password

    // Переменные драйвера
    private lateinit var driver: ChromeDriver
    private lateinit var wait: WebDriverWait
    private lateinit var jsExecutor: JavascriptExecutor

    // Результирующий список метаданных публикаций
    private val results = mutableListOf<VinitiDocumentMeta>()

    @PreDestroy
    fun cleanup() {
        if (::driver.isInitialized) {
            logger.info("Завершение работы драйвера и завершение процессов ChromeDriver")
            try {
                driver.quit()
            } catch (e: Exception) {
                logger.warn("Ошибка при завершении ChromeDriver: ${e.message}")
            }

            try {
                ProcessBuilder("taskkill", "/F", "/IM", "chromedriver.exe", "/T")
                    .inheritIO()
                    .start()
            } catch (_: Exception) { }
        }
    }

    fun getActualRubricListTerm(input: VinitiServiceInput): List<VinitiDocumentMeta> {
        logger.info("Настройка ChromeDriver и запуск браузера")
        val options = ChromeOptions()
        options.addArguments("--headless")
        options.addArguments("--window-size=1920,1080")

        WebDriverManager.chromedriver().setup()
        driver = ChromeDriver(options)
        wait = WebDriverWait(driver, Duration.ofSeconds(5))
        jsExecutor = driver

        results.clear()
        logger.info("Начало авторизации")
        processAuth()
        logger.info("Авторизация пройдена")

        for (code in input.rubricCodes) {
            logger.info("Обработка шифра рубрики: $code")
            processSearchParams(code)
            logger.info("Параметры поиска установлены для шифра: $code")
            val choiceResult = processCategoryChoice()
            if (!choiceResult) {
                continue
            }
            logger.info("Выбор категории 'Статья' выполнен")
            processPagesData(input)
            logger.info("Обработка результатов поиска для шифра $code завершена")
        }

        logger.info("Все шифры обработаны")
        return results
    }

    fun processAuth() {
        driver.get(startPageUrl)
        logger.info("Загружена страница: " + driver.title)
        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
        val loginField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_UserLogin")))
        loginField.sendKeys(username)
        val passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_UserPass")))
        passwordField.sendKeys(password, Keys.ENTER)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")))
    }

    fun processSearchParams(rubricCode: String) {
        driver.get(startPageUrl)
        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
        logger.info("Установка параметров поиска для шифра: $rubricCode")
        val radioButton = waitForElementVisibleById("ctl00_ContentPlaceHolder1_RBList_1_1")
        jsExecutor.executeScript("arguments[0].click();", radioButton)

        val cscstiCheckbox = wait.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_CBList_1_3")))
        cscstiCheckbox.click()
        val searchInput = wait.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_TBSearch_1")))
        searchInput.clear()
        searchInput.sendKeys(rubricCode)
        val dropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_DDLSearch_1")))
        val select = Select(dropdown)
        select.selectByValue("E")
        if (select.firstSelectedOption.getAttribute("value") != "E") {
            throw IllegalStateException("Ошибка: Выбран неверный пункт в выпадающем списке!")
        }
        wait.until {
            val currentValue = searchInput.getAttribute("value")
            currentValue == rubricCode
        }
        searchInput.sendKeys(Keys.RETURN)
    }

    fun processCategoryChoice(): Boolean {
        // Проверка на отсутствие результатов
        try {
            val statText = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_ContentPlaceHolder1_Statistics"))
            ).text
            if (Regex("""[-–]\s*0\s+объект""").containsMatchIn(statText)) {
                logger.warn("Рубрика не содержит результатов — поиск прерван")
                driver.switchTo().defaultContent()
                return false
            }

        } catch (e: Exception) {
            logger.warn("Не удалось определить наличие результатов: ${e.message}")
            throw e
        }

        try {
            val iframe = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_data_frame"))
            )
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(iframe))
            logger.info("Переключились в iframe для выбора категории")
        } catch (e: Exception) {
            logger.warn("Iframe не найден или не доступен: ${e.message}")
            throw e
        }

        try {
            val articleLink = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@class, 'ResultTitle') and contains(normalize-space(text()), 'Статьи')]")
                )
            )
            articleLink.click()
            logger.info("Переход по ссылке с категорией 'Статьи'")
        } catch (e: Exception) {
            logger.warn("Не удалось найти ссылку с категорией 'Статьи': ${e.message}")
            throw e
        }

        driver.switchTo().defaultContent()
        return true
    }

    fun processPagesData(input: VinitiServiceInput) {
        var currentPage = 1
        do {
            try {
                val frames = driver.findElements(By.id("ctl00_ContentPlaceHolder1_data_frame"))
                if (frames.isNotEmpty()) {
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ctl00_ContentPlaceHolder1_data_frame")))
                    logger.info("Обработка страницы $currentPage: переключились в iframe")
                } else {
                    logger.warn("На странице $currentPage iframe не обнаружен, продолжаем в основном окне")
                }
            } catch (e: Exception) {
                logger.warn("Ошибка при переключении в iframe на странице $currentPage: ${e.message}")
                driver.switchTo().defaultContent()
            }

            val resultTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ResultTable")))
            val tbody = resultTable.findElement(By.tagName("tbody"))
            val rows = tbody.findElements(By.tagName("tr"))
            val validRows = rows.filter { it.findElements(By.tagName("td")).size >= 3 }

            logger.info("Обработка страницы $currentPage: найдено ${validRows.size} публикаций")

            for (row in validRows) {
                val cells = row.findElements(By.tagName("td"))
                val documentPageTag = cells[1].findElement(By.tagName("a"))
                val publicationLink = documentPageTag.getAttribute("href")

                if (publicationLink.isNullOrBlank()) {
                    logger.warn("Пустая ссылка на публикацию — пропускаем")
                    continue
                }

                val docMeta = extractPublicationMetadata(publicationLink, input, null)

                if (docMeta != null) {
                    results.add(docMeta)
                    logger.info("Обработана публикация: ${docMeta.title}")
                } else {
                    logger.warn("Публикация $publicationLink не прошла фильтрацию")
                }
                try {
                    wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ctl00_ContentPlaceHolder1_data_frame")))
                    logger.info("Возвращаемся в iframe после обработки публикации")
                } catch (e: Exception) {
                    logger.warn("Не удалось вернуться в iframe после публикации: ${e.message}")
                }

            }

            currentPage++
            if (currentPage > input.maxPages) break
            driver.switchTo().defaultContent()
            try {
                val nextPageButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_NextPage")))
                logger.info("Переход на страницу $currentPage")
                nextPageButton.click()
                wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }
            } catch (e: Exception) {
                logger.warn("Пагинация: ошибка при переходе на страницу $currentPage: ${e.message}")
                break
            }
        } while (true)

        driver.switchTo().defaultContent()
    }

    fun waitForElementVisibleById(id: String, timeout: Long = 10): WebElement {
        return WebDriverWait(driver, Duration.ofSeconds(timeout)).until {
            try {
                val el = driver.findElement(By.id(id))
                el.isDisplayed && el.isEnabled
            } catch (e: Exception) {
                false
            }
        }.let { driver.findElement(By.id(id)) }
    }


    fun extractPublicationMetadata(link: String, input: VinitiServiceInput, documentId: String?): VinitiDocumentMeta? {
        logger.info("Начало обработки страницы публикации: $link")
        val originalWindow = driver.windowHandle
        jsExecutor.executeScript("window.open()")
        wait.until { driver.windowHandles.size > 1 }
        val newWindow = driver.windowHandles.first { it != originalWindow }

        driver.switchTo().window(newWindow)
        driver.get(link)
        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }

        try {
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ctl00_ContentPlaceHolder1_data_frame")))

            val tables = driver.findElements(By.cssSelector("table[border='1']"))

            var sid2: String? = null
            var title: String? = null
            var translateTitle: String? = null
            var annotation: String? = null
            var language: String? = null
            val rubrics = mutableListOf<String>()
            val keywordBlocks = mutableListOf<List<String>>()

            for (table in tables) {
                val rows = table.findElements(By.tagName("tr"))
                for (row in rows) {
                    val tds = row.findElements(By.tagName("td"))
                    if (tds.size < 2) continue

                    val key = tds[0].text.trim()
                    val valueTd = tds[1]

                    when (key) {
                        "Постоянная ссылка (СИД2)" -> {
                            val a = valueTd.findElements(By.tagName("a")).firstOrNull()
                            sid2 = a?.getAttribute("href")
                        }
                        "Название" -> title = valueTd.text.trim()
                        "Название - перевод на рус. язык" -> translateTitle = valueTd.text.trim()
                        "Аннотация" -> annotation = valueTd.text.trim()
                        "Язык текста" -> language = valueTd.text.trim()
                        "Шифр ГРНТИ" -> rubrics.add(valueTd.text.trim())
                        "Ключевые слова" -> {
                            val raw = valueTd.text.trim()
                            val list = raw.split(Regex("[,;]")).map { it.trim() }.filter { it.isNotEmpty() }
                            keywordBlocks.add(list)
                        }
                    }
                }
            }

            if (sid2 == null) {
                logger.warn("Не найдена постоянная ссылка (СИД2) — пропускаем публикацию")
                return null
            }

            if (rubrics.isEmpty()) {
                logger.warn("Нет ни одного шифра ГРНТИ — публикация пропущена")
                return null
            }

            val rubricTermDataList = rubrics.mapIndexed { i, cipher ->
                val keywords = keywordBlocks.getOrNull(i) ?: emptyList()
                RubricTermData(cipher, keywords)
            }

            return VinitiDocumentMeta(
                title = title ?: "(без названия)",
                annotation = annotation,
                translateTitle = translateTitle,
                link = sid2,
                language = language ?: "неизвестно",
                rubricTermDataList = rubricTermDataList
            )
        } catch (e: Exception) {
            logger.error("Ошибка извлечения метаданных публикации: ${e.message}")
            return null
        } finally {
            driver.close()
            driver.switchTo().window(originalWindow)
        }
    }
}