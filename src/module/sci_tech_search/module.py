from dependency_injector.wiring import Provide

from src.container.main_dependencies.sci_tech_search.doc_ranking.container import DocRankingModuleContainer
from src.module.base_module_class import BaseModule
from src.module.sci_tech_search.doc_ranking.module import DocRankingModule


class SciTechSearchModule(BaseModule):

    def __init__(self) -> None:
        super().__init__()

    def process_request(self,
                    doc_ranking_module: DocRankingModule = Provide(DocRankingModuleContainer.doc_ranking_module_provider)):

        doc_ranking_module.logger.info("Обработка запроса (заглушка)")