from dependency_injector import containers, providers

from src.module.sci_tech_search.doc_ranking.module import DocRankingModule


class DocRankingModuleContainer(containers.DeclarativeContainer):

    doc_ranking_module_provider = providers.ThreadSafeSingleton(DocRankingModule)