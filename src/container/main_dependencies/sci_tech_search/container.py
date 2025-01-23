from dependency_injector import providers, containers

from src.module.sci_tech_search.module import SciTechSearchModule

class SciTechSearchModuleContainer(containers.DeclarativeContainer):

    sci_tech_search_module_provider = providers.ThreadSafeSingleton(SciTechSearchModule)