# Проект автоматизации тестирования IMGUR REST API

## Сводка

- [Требования](#Требования)
- [Скачивание и запуск проекта](#Скачивание-и-запуск-проекта)
- [Информация о проекте](#Информация-о-проекте)
    - [Информация по тестам](#Информация-по-тестам)
    - [Информация по отчету Allure](#Информация-по-отчету-Allure)  
- [Автор](#Автор)

## Требования
<a name="Требования"></a>
Для отладки и запуска проекта в среде разработки понадобится:

1. JDK 1.8
2. Maven 3.6.3
3. Установленные плагины IDEA:
    - Gherkin
    - Cucumber for Java
    - Lombok

## Скачивание и запуск проекта
<a name="Скачивание-и-запуск-проекта"></a>
Возможно скачать архив или клонировать проект при наличии git.

[Ссылка на скачивание архива](https://github.com/a-tikhomirov/at-rest-api/archive/master.zip)

Команда для клонирования проекта:

```
$ git clone https://github.com/a-tikhomirov/at-rest-api.git
$ cd at-rest-api/
```

Для запуска тестового набора *image* необходимо в командной строке перейти в директорию проекта и выполнить команду:

```
mvn clean test allure:serve
```

Для запуска тестов c заданными тегами необходимо в командной строке перейти в директорию проекта и выполнить команду:

```
mvn clean test -Dcucumber.filter.tags=@tag_to_run allure:serve
```

В результаты выполнения данной команды:
- При необходимости будут скачаны зависимости проекта;
- В однопоточном режиме будут запущен набор тестов по умолчанию (с тегом *image*) или тесты с заданными тегами;
- По окончании тестов будет открыт браузер с отчетом по выполненным тестам.

> Примечание: используется именно однопоточный режим, так как Imgur не всегда нормально
> работает при частой и, тем более, одновременнно отправке множества запросов

## Информация о проекте
<a name="Информация-о-проекте"></a>
### Информация по тестам
<a name="Информация-по-тестам"></a>
> **ВНИМАНИЕ** Imgur API имеет ограничение по числу загружаемых изображений: [не более 50 изображений в час](https://help.imgur.com/hc/en-us/articles/115000083326-What-files-can-I-upload-What-is-the-size-limit-)  
> Поэтому не рекомендуется запускать весь тестовый набор чаще чем раз в час.  
> По этой же причине несколько некритичных тестов были исключены из набора (закомментирован тег @image)

Сценарии тестирования расположены: `src/test/resources/features/`  
Один сценарий тестирования может иметь несколько наборов тестовых данных. Такие сценарии будут запускаться несколько раз (по числу тестовых наборов)  

Тесты, которые, как мне кажется, отражают баги в работе Imgur API помечены тегом `@Issue=...`  
Краткое описание бага указано в сценарии (*.feature* файле) после комментария `#TODO @Issue`  
Такие тесты не проходят (и не должны проходить) так как фунционал, который они проверяют, работает некорректно (на мой взгляд), то есть фактический результат не совпадает с ожидаемым. 

По окончании прохождения тестов будут сформированы логи:
- Единый лог на весь запущенный тестовый набор: `logs/test_log.log`
- Индивидуальный лог для каждого теста: `logs/<дата запуска тестового набора>/features/imgur/image/...`

### Информация по отчету Allure
<a name="Информация-по-отчету-Allure"></a>
Для просмотра отчета по результатам прохождения тестов используется команда: `allure:serve`

Пример отчета: [Allure Overview](https://drive.google.com/file/d/1eXKAYbitijOZVUgGdNCSxAnlz_4vv6Hm/view?usp=sharing)
> Примечание: все упавшие тесты в указанном примере отчета - тесты с багами

В информацию о прохождении теста включается:
- Текущие данные тестового набора - при наличии;
- Ссылка на документацию по тестируемому запросу;
- Отметка о наличии бага (которая должна включать в себя ссылку на заведенный баг) - при наличии;
- Шаги теста;
- Лог прохождения теста (в шаге Tear down - AttachLogs).

Пример данных отчета по одному тесту: [Allure test view](https://drive.google.com/file/d/1wH-_Xp9e1NOUUCSItCLsDZ1WocmtLRrF/view?usp=sharing)

## Автор

- **Андрей Тихомиров** - <andrey.tikhomirov.88@gmail.com>