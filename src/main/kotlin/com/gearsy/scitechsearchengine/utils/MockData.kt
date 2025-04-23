package com.gearsy.scitechsearchengine.utils

import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.model.viniti.catalog.RubricTermData
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiDocumentMeta
import com.gearsy.scitechsearchengine.model.yandex.YandexSearchResultModel
import java.util.*

fun generateMockResults(query: Query): List<SearchResult> {
    val count = (5..15).random()
    val titles = listOf(
        "Искусственный интеллект в промышленности",
        "Цифровая трансформация производств",
        "Интернет вещей и автоматизация",
        "Умные фабрики и системы управления",
        "Оптимизация процессов с помощью ИИ",
        "Цифровизация производственных цепочек",
        "Автоматизация технологических процессов"
    )
    val used = mutableSetOf<String>()

    return (1..count).map { i ->
        val availableTitles = titles.filter { it !in used }
        val title = if (availableTitles.isNotEmpty()) {
            availableTitles.random()
        } else {
            "Автоматизированный заголовок №$i" // или просто titles.random(), если дубли допустимы
        }

        used.add(title)

        SearchResult(
            query = query,
            documentId = UUID.randomUUID().toString(),
            documentUrl = "https://example.com/doc-$i",
            title = title,
            snippet = "$title, $title, $title, $title, $title",
            score = "%.2f".format((0.6 + Math.random() * 0.39)).toDouble()
        )
    }
}
//
//fun getYandexResultsMock(): List<YandexSearchResultModel> {
//    return listOf(
//        YandexSearchResultModel(
//            documentId = "Z0F8BC48B3ACF7200",
//            title = "Microsoft Word - обл",
//            url = "https://tstu.ru/book/elib/pdf/2014/dvorecky.pdf"
//        ),
//        YandexSearchResultModel(
//            documentId = "Z487E31F8E73255D1",
//            title = "Федеральное государственное бюджетное образовательное...",
//            url = "https://klgtu.ru/vikon/sveden/files/UMP_k_LR_Matematicheskoe_modelirovanie_v_pischevoy_biotexnologii.pdf"
//        ),
//        YandexSearchResultModel(
//            documentId = "Z631A4BEA00FBB4DE",
//            title = "Моделирование объектов и процессов в пищевых производствах",
//            url = "https://cyberleninka.ru/article/n/modelirovanie-obektov-i-protsessov-v-pischevyh-proizvodstvah"
//        ),
//        YandexSearchResultModel(
//            documentId = "Z95A89CD42B209DA7",
//            title = "Федеральное государственное бюджетное образовательное...",
//            url = "https://klgtu.ru/vikon/sveden/files/UMP_po_Matematicheskomu_modelirovaniyu_proektirovaniya_produktov_pitaniya(2).pdf"
//        )
//    )
//}

fun getVinitiCatalogMock(): List<VinitiDocumentMeta> {
    return listOf(
        VinitiDocumentMeta(
            title = "Exact hyperplanen covers for subsets of the hypercube",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J1972066994%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("гиперкуб", "гиперплоскость", "покрытие")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Fault-tolerant Hamiltonian connectivity of 2-tree-generated networks",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2027013261%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("сеть", "дефектоустойчивость", "дерево")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "A local injective proof of log-concavity for increasing spanning forests",
            annotation = null,
            translateTitle = "Локальное инъективное доказательство лог-вогнутости для возрастающих остовных лесов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2179069547X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("лес", "лог-вогнутость", "биекция")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Gap sets for the spectra of regular graphs with minimum spectral gap",
            annotation = null,
            translateTitle = "Множества разрывов для спектров регулярных графов с минимальным спектральным разрывом",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20889353596%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("спектр", "разрыв", "регулярный граф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Quartic graphs with minimum spectral gap",
            annotation = null,
            translateTitle = "Четвертичные графы с минимальным спектральным разрывом",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2096393612%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("спектр", "регулярность", "случайность")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Maximal double Roman domination in graphs",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2018502X160%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("доминирование", "дерево", "цепь")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Distance-regular graphs with a few q-distance eigenvalues",
            annotation = null,
            translateTitle = "Дистанционно регулярные графы с несколькими q-дистанционными собственными значениями",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22100633232%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("дистанционно регулярный граф", "дистанционная матрица", "собственное значение")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On the Kirchhoff index and the number of spanning trees of cylinder/Möbius pentagonal chain",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J210261624X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("индекс Кирхгофа", "дерево", "цепь")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Differences between the list-coloring and DP-coloring for planar graphs",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J19703144310%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("раскраска графа", "планарный граф", "предписанная раскраска")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "The Alon-Tarsi number of K5-minor-free graphs",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20184961145%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("раскраска графа", "планарный граф", "минор")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "A bound for the p-domination number of a graph in terms of its eigenvalue multiplicities",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2107115X139%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("доминирование", "спектр", "ранг")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Optimization of eigenvalue bounds for the independence and chromatic number of graph powers",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20110526208%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("собственное значение", "число независимости", "хроматическое число")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Enumeration of cospectral and coinvariant graphs",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J1995520837%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("перечисление", "спектр графа", "инвариант графа")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Codeterminantal graphs",
            annotation = null,
            translateTitle = "Кодетерминантные графы",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2058732278%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("спектр графа", "детерминант", "коспектральные графы")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "A switching method for constucting cospectral gain graphs",
            annotation = null,
            translateTitle = "Метод переключения для построения коспектральных графов усиления",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J220056497X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("граф усиления", "переключение", "знаковый граф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On the status sequences of trees",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J1945977493%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("дерево", "статус вершины", "связность")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Extending a conjecture of Graham and Lovász on the distance characteristic polynomial",
            annotation = null,
            translateTitle = "Расширение гипотезы Грэхема и Ловаса о дистанционном характеристическом многочлене",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2220437469%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("дистанционная матрица", "характеристический многочлен", "граф блоков")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "An infinite class of Neumaier graphs and non-existence results",
            annotation = null,
            translateTitle = "Бесконечный класс графов Ноймаера и результаты о несуществовании",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20944842107%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("регулярность", "клика", "граф Ноймаера")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On inertia and ratio type bounds for the k-independence number of a graph and their relationship",
            annotation = null,
            translateTitle = "Об оценках типа инерции и отношения для числа k-независимости графа и их взаимосвязи",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J212626212X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("степень графа", "число независимости", "расстояние")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Neumaier graphs with few eigenvalues",
            annotation = "A Neumaier graph is a non-complete edge-regular graph containing a regular clique. In this paper we give some sufficient and necessary conditions for a Neumaier graph to be strongly regular. Further we show that there does not exist Neumaier graphs with exactly four distinct eigenvalues. We also determine the Neumaier graphs with smallest eigenvalue -2.",
            translateTitle = "Графы Ноймайера с несколькими собственными значениями",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2065527153%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("собственное значение", "клика", "регулярность")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Bounding the sum of the largest signless Laplacian eigenvalues of a graph",
            annotation = null,
            translateTitle = "Оценка суммы наибольших беззнаковых лапласовых собственных значений графа",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21733551162%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("лапласова матрица", "собственное значение", "степень вершины")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Cospectral mates for generalized Johnson and Grassmann graphs",
            annotation = null,
            translateTitle = "Коспектральные сопряжения для обобщенных графов Джонсона и Грассмана",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2168570019%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("коспектральность", "граф Джонсона", "граф Грассмана")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Bordering of symmetric matrices and an application to the minimum number of distinct eigenvalues for the join of graphs",
            annotation = null,
            translateTitle = "Окаймление симметричных матриц и применение к минимальному числу различных собственных значений для соединения графов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J216857197X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("матрица", "собственное значение", "соединение графов")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Characterizing and computing weight-equitable partitions of graphs",
            annotation = null,
            translateTitle = "Характеристика и вычисление равновесных разбиений графов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2042693030%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("разбиение графа", "кограф", "собственное значение")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Coloring the normalized Laplacian for oriented hypergraphs",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2000882994%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("гиперграф", "лапласиан", "раскраска")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On the diameter and zero forcing number of some graph classes in the Johnson, Grassmann and Hamming association scheme",
            annotation = null,
            translateTitle = "О диаметре и нулевом числе форсинга для некоторых классов графов в ассоциативной схеме Джонсона, Грассмана и Хэмминга",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22152927121%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("граф Грассмана", "граф Джонсона", "число форсинга")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Colouring a dominating set without conflicts: q-subset square colouring",
            annotation = null,
            translateTitle = "Раскраска доминирующего множества без конфликтов: квадратная раскраска q-подмножества",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2169069054%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("доминирование", "раскраска", "конфликт")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Four proofs of the directed Brooks' theorem",
            annotation = null,
            translateTitle = "4 доказательства ориентированной теоремы Брукса",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J217399672X%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("теорема Брукса", "орграф", "NP-полнота")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Heroes in oriented complete multipartite graphs",
            annotation = null,
            translateTitle = "Герои в направленных полных многодольных графах",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2205033492%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("дихроматическое число", "орграф", "герой")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Graphs with no induced house nor induced hole have the de Bruijn-Erdös property",
            annotation = null,
            translateTitle = "Графы без индуцированного дома и индуцированной дыры обладают свойством де Брейна—Эрдёша",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2055627333%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("метрика", "цикл", "дыра")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Grundy coloring and friends, half-graphs, bicliques",
            annotation = "The first-fit coloring is a heuristic that assigns to each vertex, arriving in a specified order σ, the smallest available color. The problem Grundy Coloring asks how many colors are needed for the most adversarial vertex ordering σ, i.e., the maximum number of colors that the first-fit coloring requires over all possible vertex orderings. Since its inception by Grundy in 1939, Grundy Coloring has been examined for its structural and algorithmic aspects. A brute-force f(k)n2k-1-time algorithm for Grundy Coloring on general graphs is not difficult to obtain, where k is the number of colors required by the most adversarial vertex ordering. It was asked several times whether the dependency on k in the exponent of n can be avoided or reduced, and its answer seemed elusive until now. We prove that Grundy Coloring is W[1]-hard and the brute-force algorithm is essentially optimal under the Exponential Time Hypothesis, thus settling this question by the negative. The key ingredient in our W[1]-hardness proof is to use so-called half-graphs as a building block to transmit a color from one vertex to another. Leveraging the half-graphs, we also prove that b-Chromatic Core is W[1]-hard, whose parameterized complexity was posed as an open question by Panolan et al. [JCSS '17]. A natural follow-up question is, how the parameterized complexity changes in the absence of (large) half-graphs. We establish fixed-parameter tractability on Kt,t-free graphs for b-Chromatic Core and Partial Grundy Coloring, making a step toward answering this question. The key combinatorial lemma underlying the tractability result might be of independent interest.",
            translateTitle = "Раскраска Гранди и друзья, полуграфы, биклики",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2098354614%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("раскраска", "полуграф", "биклика")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On maximum matchings in 5-regular and 6-regular multigraphs",
            annotation = null,
            translateTitle = "О максимальных паросочетаниях в 5-регулярных и 6-регулярных мультиграфах",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21002662156%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("паросочетание", "регулярность", "мультиграф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "New dualities from old: generating geometric, Petrie, and Wilson dualities and trialities of ribbon graphs",
            annotation = "We define a new ribbon group action on ribbon graphs that uses a semidirect product of a permutation group and the original ribbon group of Ellis-Monaghan and Moffatt to take (partial) twists and duals, or twuals, of ribbon graphs. A ribbon graph is a fixed point of this new ribbon group action if and only if it is isomorphic to one of its (partial) twuals. This extends the original ribbon group action, which only used the canonical identification of edges, to the more natural setting of self-twuality up to isomorphism. We then show that every ribbon graph has in its orbit an orientable embedded bouquet and prove that the (partial) twuality properties of these bouquets propagate through their orbits. Thus, we can determine (partial) twualities via these one vertex graphs, for which checking isomorphism reduces simply to checking dihedral group symmetries. Finally, we apply the new ribbon group action to generate all self-trial ribbon graphs on up to seven edges, in contrast with the few, large, very high-genus, self-trial regular maps found by Wilson, and by Jones and Poultin. We also show how the automorphism group of a ribbon graph yields self-dual, -petrial or -trial graphs in its orbit, and produce an infinite family of self-trial graphs that do not arise as covers or parallel connections of regular maps, thus answering a question of Jones and Poulton.",
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2052572627%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("карта", "двойственность", "ленточный граф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On a ratio of Wiener indices for embedded graphs",
            annotation = null,
            translateTitle = "О соотношении индексов Винера для вложенных графов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21207507136%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("индекс Винера", "вложенный граф", "молекула")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "On a ratio of Wiener indices for embedded graphs",
            annotation = "The Wiener index of a graph G, denoted by W(G), is the sum of all distances between pairs of vertices in G. Originally called the path number and used to predict the boiling points of paraffin molecules, W(G) has been studied extensively from both a chemical perspective and a mathematical perspective. In this article, we consider a completely unexplored aspect of the Wiener index involving a cellularly embedded graph and its dual graph. For a cellularly embedded graph G on a given surface S and its dual graph G*, we define the Wiener ratio of G on S, denoted by Λ(G), as the minimum value between W(G)/W(G*) and W(G*)/W(G). We elucidate some basic properties of Λ(G), explore convergence of the Wiener ratios of families of cellularly embedded graphs and prove a density result, and then conclude with some open questions about the Wiener ratio.",
            translateTitle = "О соотношении индексов Винера для вложенных графов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21194979137%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("вложенный граф", "индекс Винера", "химия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "The outerplanar crossing number of the complete bipartite graph",
            annotation = null,
            translateTitle = "Число внешнепланарных пересечений для полного двудольного графа",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20829148341%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("число пересечений", "внешнепланарность", "полный двудольный граф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Total tessellation cover: Bounds, hardness, and applications",
            annotation = null,
            translateTitle = "Полное мозаичное покрытие: границы, трудность и применение",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20952292102%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("покрытие", "мозаика", "раскраска")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "A construction for a counterexample to the pseudo 2-factor isomorphic graph conjecture",
            annotation = null,
            translateTitle = "Построение контрпримера к гипотезе о псевдо 2-факторном изоморфном графе",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21110228119%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("фактор", "изоморфизм", "цикл")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Strictly chordal graphs: Structural properties and integer Laplacian eigenvalues",
            annotation = null,
            translateTitle = "Строго хордальные графы: структурные свойства и целочисленные лапласовы собственные значения",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2181443831%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("хордальный граф", "собственное значение", "инвариант")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "A note of Reed's conjecture for triangle-free graphs",
            annotation = null,
            translateTitle = "Заметка о гипотезе Рида для графов без треугольников",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21790695321%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("раскраска", "граф без треугольников", "гипотеза Рида")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Local metric dimension for graphs with small clique numbers",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20184961153%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("метрическая размерность", "клика", "паросочетание")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Induced subgraphs and tree decompositions V. one neighbor in a hole",
            annotation = null,
            translateTitle = "Индуцированные подграфы и разложения деревьев V. Один сосед в дыре",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2205033441%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("древовидная ширина", "подграф", "дыра")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Induced subgraphs and tree decompositions. VII. Basic obstructions in H-free graphs",
            annotation = null,
            translateTitle = "Индуцированные подграфы и древовидные разложения VII. Основные препятствия в графах без H-подграфов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21792825168%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("древовидное разложение", "препятствие", "подграф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Induced subgraphs and tree decompositions II. Toward walls and their line graphs in graphs of bounded degree",
            annotation = null,
            translateTitle = "Порожденные подграфы и древесные декомпозиции II. К стенам и их реберным графам в графах с ограниченной степенью вершин",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21792825141%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("реберный граф", "дерево", "декомпозиция")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Graphs with polynomially many minimal separators",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J1999997387%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("алгоритм", "разделитель", "призма")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Induced subgraphs and tree decompositions I. Even-hole-free graphs of bounded degree",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20767770206%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("дерево", "дыра", "степень вершины")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Approximating maximum diameter-bounded subgraph in unit disk graphs",
            annotation = "We consider a well-studied generalization of the maximum clique problem which is defined as follows. Given a graph G on n vertices and a fixed parameter d≥1, in the maximum diameter-bounded subgraph problem (MaxDBS for short) the goal is to find a (vertex) maximum subgraph of G of diameter at most d. For d=1, this problem is equivalent to the maximum clique problem and thus it is NP-hard to approximate it within a factor n1-∈, for any ∈>0. Moreover, it is known that, for any d≥2, it is NP-hard to approximate MaxDBS within a factor n1/2-∈, for any ∈>0. In this paper we focus on MaxDBS for the class of unit disk graphs. We provide a polynomial-time constant-factor approximation algorithm for the problem. The approximation ratio of our algorithm does not depend on the diameter d. Even though the algorithm itself is simple, its analysis is rather involved. We combine tools from the theory of hypergraphs with bounded VC-dimension, k-quasi planar graphs, fractional Helly theorems, and several geometric properties of unit disk graphs.",
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J1992255598%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("алгоритм", "граф единичных дисков", "диаметр")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "An improved exact algorithm for minimum dominating set in chordal graphs",
            annotation = "Minimum Dominating Set is among the most studied classical NP-hard problems. On general graphs, an O(1.4864n) exact algorithm for this problem is known. Minimum Dominating Set remains NP-hard on chordal graphs where the current asymptotically-fastest exact algorithm solves the problem in O(1.3687n) time and super-polynomial space. In this paper, a simple exact polynomial-space algorithm that exploits the local structure of neighborhoods of simplicial vertices in chordal graphs is presented, resulting in an improved running time in O(1.3384n).",
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20020691161%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("алгоритм", "доминирующее множество", "хордовый граф")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "An improved fixed-parameter algorithm for 2-Club Cluster Edge Deletion",
            annotation = null,
            translateTitle = "Улучшенный алгоритм с фиксированными параметрами для удаления ребер кластера с двумя клубами",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2134136X66%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("кластер", "удаление ребра", "алгоритм")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Vertex-critical (P3 + lP1)-free and vertex-critical (gem, co-gem)-free graphs",
            annotation = null,
            translateTitle = "Вершинно критические графы без (P3 + lP1)- подграфов и вершинно критические графы без (гемма, когемма)-подграфов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21851872139%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.45.17",
                    keywords = listOf("раскраска", "критичность", "гемма")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Дискретные временные ряды на основе экспоненциального семейства с многомерным параметром и их вероятностно-статистический анализ",
            annotation = "Предложена новая малопараметрическая модель дискретного временного ряда на основе экспоненциального семейства дискретных распределений вероятностей с многомерным параметром. Для параметров предложенной модели строится семейство состоятельных асимптотически нормальных статистических оценок явного вида, в котором найдена асимптотически эффективная оценка, достигающая границы Крамера - Рао при растущей длительности наблюдения временного ряда. Полученные результаты могут быть использованы для робастного статистического анализа дискретных временных рядов, статистического анализа дискретных пространственно-временных данных и случайных полей",
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2089694521%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "27.43",
                    keywords = listOf("дискретный временной ряд", "малопараметрическая модель", "цепь Маркова высокого порядка", "экспоненциальное семейство")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Impulse für die Verpackungsbranche",
            annotation = null,
            translateTitle = "Импульс к развитию упаковочной промышленности",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22499560165%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "выставки", "Германия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Optimierter Verbrauch beim Verpackungsdruck",
            annotation = null,
            translateTitle = "Оптимизированный расход при печати упаковки",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22329940137%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "печатные машины")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "RosUpack — зеркало упаковочной индустрии",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22451681191%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочное оборудование", "выставки", "Россия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "UPAKEXPO-2024 демонстрирует позитивную динамику упаковочной отрасли",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22140147142%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("пищевая промышленность", "упаковка", "упаковочные машины", "выставки", "Россия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Амистайл пленки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2226081992%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные материалы", "пленки", "барьерные", "термоусадочные", "производство", "компании")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "АО \"Завод Протей\" — один из российских лидеров упаковки для БАД",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2226055X46%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "тара", "банки пластмассовые", "производство", "компании")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Барьер на бумажной основе для асептической упаковки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2216940496%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка асептическая", "барьерные слои", "бумага")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Без упаковки — ни за что: проблемы защиты продукта",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22169404100%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("пищевая промышленность", "упаковка", "инновации")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Волшебное зеркальце от \"ЛАМБУМИЗ\"",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2222962846%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("молочные продукты", "упаковка", "анализ и сравнение на заводе \"ЛАМБУМИЗ\"", "ребрендинг", "искусственный интелект")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Высокоинтеллектуальные линии розлива от завода АВРОРА покоряют новые рынки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2226055X38%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "разливочные машины", "компании")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Демонстрационная модель: термоупаковочное оборудование. Производитель \"Сигнал-Пак\"",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2249591377%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "термоформеры", "вакуум-упаковочные устройства", "лотки", "упаковочные материалы", "пленки", "пищевые продукты", "охлажденные")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Инновации в упаковке продуктов питания: повышение срока годности и удобство использования",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2211832X25%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "инновации")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Новое направление — дистрибуция",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2246089352%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "пластиковая", "производство")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Обзор мирового рынка металлической упаковки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2216940410%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("тара", "металлическая", "контейнеры")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Производство упаковки в 2023 году: экономия ресурсов везде, где это возможно",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2216940488%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные материалы", "выставки", "Германия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Роботы-коботы ускоряют инновации в секторе упаковки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22169404119%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные системы", "упаковочные машины", "роботы", "коботы", "робототехника")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Стекольная промышленность: меньше материала и больше экологически чистой энергии",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2216940461%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("стекольная промышленность", "тара", "упаковка", "окружающая среда охрана", "экология")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Умная идея крепления транспортных коробок на поддонах",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J22169404127%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "коробки", "транспортирование")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "30 Prozent schneller als das Vorgängermodell",
            annotation = null,
            translateTitle = "Новая упаковочная установка типа Box Motion",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21075686101%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "пленки", "склеивание")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Avoiding food waste with reclose systems",
            annotation = null,
            translateTitle = "Предотвращение усиления потоков пищевых отходов путем использования систем повторной упаковки",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2179105525%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("отходы", "пищевые", "предотвращение увеличения", "упаковка", "повторная")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Die Schokoladenseite von Polycarbonat",
            annotation = null,
            translateTitle = "Кругооборот поликарбоната",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2158674571%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "55.39.33",
                    keywords = listOf("рециклаты", "для пищевой промышленности", "поликарбонаты", "рециклинг", "новые способы", "Германия", "экструдеры двухчервячные", "использование")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Digitale Services für mehr Nachhaltigkeit",
            annotation = null,
            translateTitle = "Снижение брака при упаковке продуктов питания",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21262745183%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "износ", "прогнозирование", "цифровые платформы")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Innovative Sensorlösungen",
            annotation = null,
            translateTitle = "Современная сенсорика",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21316721287%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "датчики", "сенсорные", "инновации")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Packaging for beverages is growing more sustainable",
            annotation = null,
            translateTitle = "Упаковка напитков становится более устойчивой",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2120792224%27)&TYP=Full1",
            language = "английский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("напитки", "упаковка", "устойчивая")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Sainsbury's выпускает перерабатываемую картонную упаковку для своих стейков",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2154817726%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("мясные продукты", "стейки", "упаковка", "перерабатываемая картонная", "от Sainsbury's")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "TDNT Industrial: через инновации к успеху",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2160649546%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочное оборудование", "компании", "импортозамещение")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "\"X-Faktor\" für den Verpackungsprozess",
            annotation = null,
            translateTitle = "Х-фактор для упаковочных процессов",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21430788301%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "тара", "картон")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Выпущена первая бутылка из вторичного ПЭТ для вина качества \"премиум\"",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2158285515X%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "55.63.39",
                    keywords = listOf("винодельческие заводы", "вина", "упаковка", "бутылки", "ПЭТ", "качество", "контроль")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Жесть — и ничего лишнего",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2201683735%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("тара", "литографированная", "банки жестяные")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Компания ООО \"ХД РУС\" на выставке RosUpack-2023",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21582855192%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "картон", "прессы", "высекальные")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Новая биоразлагаемая пленка дольше сохраняет свежесть продуктов",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2166500951%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "61.61.33",
                    keywords = listOf("пленки", "упаковка", "продуктовая", "изготовление", "полимеры", "лимонен", "хитозаны", "из хитина ракообразных", "антиоксиданты", "термостабильность")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Пищевые продукты: упаковка против отходов",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21685123119%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "упаковочные материалы", "экологичные", "перспективы развития")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "\"Подводные камни\" на пути к упаковке из мономатериала",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2140532546%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "из мономатериала")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Российский рынок упаковки: новая стратегия развития в действии",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2141255015%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "рынок", "перспективы развития", "интервью")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Технология вспенивания для облегчения упаковки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J21343540107%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "55.39.33",
                    keywords = listOf("пластмассы", "вспененные", "упаковка", "уменьшенного веса", "изготовление", "технология Microcell", "использование")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Турецкие печатные машины для российского рынка",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2111980218%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "73.47.45",
                    keywords = listOf("упаковка", "гибкая", "флексопечатные установки")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Улучшение свойств rПЭТ для пищевой упаковки",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2134354070%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "61.61.91",
                    keywords = listOf("полиэтилентерефталат", "первичный и вторичный", "переработка", "модификаторы", "добавки", "производство компании Repi")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Упаковочное машиностроение Германии",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2127609670%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "упаковочное оборудование", "Германия")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Экологичная упаковка: взгляд потребителей",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2154817718%27)&TYP=Full1",
            language = "русский",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "экологичная", "выбор потребителей", "окружающая среда охрана")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Eine Anlage, viele Verpackungen",
            annotation = null,
            translateTitle = "Универсальные установки для производства упаковки",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20960961131%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "пленки пластмассовые")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Flexibel in Linie verpacken",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20447601187%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные линии", "пленки")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Flexibler Allrounder",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20447601195%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочное оборудование", "пленки")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Für garantiert faltenfreie Verpackungen",
            annotation = null,
            translateTitle = "Новая упаковочная машина типа R3",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20761314195%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные линии", "пленки", "применение")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Gasanalyse bei laufendem Verpackungsprozess",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J2059601177%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("тара", "газовые смеси", "газоанализаторы")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Gläser und Dosen exakt befüllen",
            annotation = null,
            translateTitle = "Новая фасовочная установка типа FKF 607",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20587632131%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("дозаторы", "тара", "конвейеры")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Große Trays schnell und effizient verpacken",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20324224113%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("лотки", "пленки", "упаковочное оборудование")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Gut verpackt heißt genussbereit",
            annotation = null,
            translateTitle = null,
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20369627195%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковка", "упаковочные материалы")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Haltbarer ohne Luft verpackt",
            annotation = null,
            translateTitle = "Надежная упаковка без воздуха",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20761314101%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "упаковка", "пленки", "применение")
                )
            )
        ),
        VinitiDocumentMeta(
            title = "Ich sehe Dich",
            annotation = null,
            translateTitle = "Фотоэлектрические датчики серии С 23",
            link = "http://catalog.viniti.ru/srch_result.aspx?IRL=SELECT%20(*)%20FROM%20(eARTC)%20WHERE%20(id_art)%20contains%20(E%27J20865993250%27)&TYP=Full1",
            language = "немецкий",
            rubricTermDataList = listOf(
                RubricTermData(
                    rubricCipher = "65.01.90",
                    keywords = listOf("упаковочные машины", "термоформовочные", "датчики", "применение")
                )
            )
        )
    )
}