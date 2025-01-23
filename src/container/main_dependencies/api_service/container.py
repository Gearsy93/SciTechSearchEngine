from dependency_injector import containers, providers

from src.module.api_service.ahunter.module import AhunterModule
from src.module.api_service.kontur.module import KonturModule


class APIServiceModuleContainer(containers.DeclarativeContainer):

    # TODO Контейнеры модулей внешний подключений
    pass