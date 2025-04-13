@echo off
REM Активируем виртуальное окружение
call venv\Scripts\activate

REM Запускаем FastAPI сервер с uvicorn
uvicorn main:app --reload

REM Отключаем виртуальное окружение после завершения
deactivate
