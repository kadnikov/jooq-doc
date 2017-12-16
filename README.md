Завершена первая версия юнит и интеграционных тестов в модуле docmanagers.
Нужно определить какие тест кейсы еще нужны. Ну и оптимизировать структуры данных для тестов.

Настройки подклчения к базе:
profiles/integration-test/config.properties

схема базы для тестов
docmanagement/src/integration-test/resources/schema_integration_test.sql


Для запуска тнит тестов нужно использовать когстпукцию:
mvn clean verify -P dev
интеграционных тестов
mvn clean verify -P integration-test

К проекту прикручен JACoCo показывающий покрытие мнтодов тестами.
Отчет интеграцилнных тестов в папке target/site/jacoco-it
для юнит тестов target/site/jacoco-ut.
в папке открыть файл index.html

ОТдельнвя задача сконфигурировать jacoco на Jenkins

Маленькая неблокирующая проблема:
Также нужно вынести настройки в config.properties настроек integration-tests профайла. В данный момент, настройки в application_it_test.properties файле. 