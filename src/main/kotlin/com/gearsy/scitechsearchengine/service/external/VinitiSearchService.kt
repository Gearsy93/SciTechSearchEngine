package com.gearsy.scitechsearchengine.service.external

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import io.github.bonigarcia.wdm.WebDriverManager
import jakarta.annotation.PreDestroy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
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

    // Данные для работы с сайтов ВИНИТИ
    private val startPageUrl = vinitiECatalogProperties.startPageUrl
    private val username = vinitiECatalogProperties.username
    private val password = vinitiECatalogProperties.password

    // Переменные драйвера
    private lateinit var driver: ChromeDriver
    private lateinit var wait: WebDriverWait
    private lateinit var jsExecutor: JavascriptExecutor

    // Требуемое количество документов
    val minimumDocumentCount = 10

    @PreDestroy
    fun cleanup() {
        driver.quit()

        // Завершаем процессы Chrome и ChromeDriver
        Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T")
    }

    fun makeRequest() {

        val cscstiRubricCipher = "27.17.23"

        // Инициализация драйвера
        driver = ChromeDriver()
        wait = WebDriverWait(driver, Duration.ofSeconds(10))
        jsExecutor = driver as JavascriptExecutor
        WebDriverManager.chromedriver().setup()

        // Авторизация
        processAuth()

        // Выставление параметров поиска на рубрики ГРНТИ
        processSearchParams(cscstiRubricCipher)

        // Выбор категории документов (статьи за 2 года)
        processCategoryChoice()

        // Извлечение данных найденных документов
        processPagesData()
    }

    fun processAuth() {

        // Загрузка страницы поиска
        driver.get(startPageUrl)

        // Ожидание загрузки всей страницы
        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }

        // Находим поле ввода логина
        val loginField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_UserLogin"))
        )
        loginField.sendKeys(username)

        // Находим поле ввода пароля
        val passwordField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("ctl00_UserPass"))
        )
        passwordField.sendKeys(password, Keys.ENTER) // Вводим пароль и нажимаем Enter

        // Ожидание полной загрузки страницы после входа
        wait.until(
            ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
        )
    }


    fun processSearchParams(cscstiRubricCipher: String) {

        // Загрузка страницы поиска
        driver.get(startPageUrl)

        // Ожидание загрузки всей страницы

        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }

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

        // Ожидание кликабельности чекбокса рубрики ГРНТИ
        val cscstiCheckbox = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_CBList_1_3"))
        )
        cscstiCheckbox.click()

        // Ввод шифра в поле поиска
        // Находим поле ввода
        val searchInput = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("ctl00_ContentPlaceHolder1_TBSearch_1"))
        )

        // Запоминаем начальное значение
        val initialValue = searchInput.getAttribute("value")

        // Вводим новый текст
        searchInput.sendKeys(cscstiRubricCipher)

        // Найти выпадающий список <select>
        val dropdown = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_DDLSearch_1"))
        )

        // Создать объект Select для работы с выпадающим списком
        val select = Select(dropdown)

        // Выбрать пункт с value="E" (Точно соответствует)
        select.selectByValue("E")

        // Проверить, что выбрана правильная опция
        val selectedOption = select.firstSelectedOption
        if (selectedOption.getAttribute("value") == "E") {
            println("Опция 'Точно соответствует' успешно выбрана.")
        } else {
            throw IllegalStateException("Ошибка: Выбран неверный пункт в выпадающем списке!")
        }

        // Ожидаем, пока введенное значение зафиксируется в поле
        wait.until {
            val currentValue = searchInput.getAttribute("value")
            currentValue == cscstiRubricCipher
        }

        searchInput.sendKeys(Keys.RETURN)
    }

    private fun processCategoryChoice() {

        // Ожидание загрузки iframe и переключение на него
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ctl00_ContentPlaceHolder1_data_frame")))

        // Ожидание появления контейнера <nobr>, содержащего нужный чекбокс
        val nobrContainer = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(".//nobr[input[@type='checkbox' and @value='eARTC']]")
            )
        )

        // Ожидание кликабельности ссылки внутри найденного контейнера
        val resultLink = wait.until(
            ExpectedConditions.elementToBeClickable(nobrContainer.findElement(By.cssSelector("a.ResultTitle")))
        )

        // Кликаем по ссылке
        resultLink.click()
    }

    fun processPagesData() {

        // Окончание загрузки страницы
        wait.until { jsExecutor.executeScript("return document.readyState") == "complete" }

        // Количество найденных подходящих документов
        val totalFoundDocumentCount = 0

        do {
            // Ожидание загрузки iframe и переключение на него
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ctl00_ContentPlaceHolder1_data_frame")))

            // Таблица с результатами
            // Найти таблицу результатов поиска
            val resultTable = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("ResultTable"))
            )

            // Найти tbody в таблице
            val tbody = resultTable.findElement(By.tagName("tbody"))

            // Извлечь все <tr>, которые являются прямыми вложенными элементами tbody
            val rows = tbody.findElements(By.xpath("./tr"))

            // Проверка на наличие строк
            if (rows.isEmpty()) {
                println("Нет данных в таблице.")
                return
            }

            // Удалить первую строку (обычно заголовок таблицы)
            val dataRows = rows.drop(1)

            // Цикл по оставшимся строкам
            for (row in dataRows) {
                // Найти 3 первых td
                val cells = row.findElements(By.xpath("./td"))
                if (cells.size < 3) continue  // Пропуск, если не хватает столбцов

                val fullFormTdTag = cells[1]  // Второй td
                val titleTdTag = cells[2]     // Третий td

                // Извлечь вложенный <a> из fullFormTdTag
                val documentPageTag = fullFormTdTag.findElement(By.tagName("a"))

                // Вывести результаты (для отладки)
                println("Full Form TD: ${fullFormTdTag.text}")
                println("Title TD: ${titleTdTag.text}")
                println("Document Page Link: ${documentPageTag.getAttribute("href")}")
            }


            // TODO переход на след страницу
            // Условие выхода из цикла

        } while (true)

    }
}

