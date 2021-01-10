# language: ru
@imgur
@image
@update-authed
@positive
@Link=https://apidocs.imgur.com/#7db0c13c-bf70-4e87-aecf-047abc65686d
Функционал: [Image Update Auth]

  Предыстория: Подготовка данных и Загрузка изображения
    Пусть выполнена генерация 1 случайных EN символов и результат сохранен в переменную "oneCharString"
    И выполнена генерация 5 случайных EN символов и результат сохранен в переменную "oneWordString"
    И выполнена генерация 128 случайных EN символов и результат сохранен в переменную "seq128Chars"
    И выполнена генерация 256 случайных EN символов и результат сохранен в переменную "seq256Chars"
    
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | MULTIPART     | image         | image.url.lt10    |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | imageDeleteHash |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message         | == | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id         | ~  | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash | ~  | imgur.correct.deletehash |
      | BODY_JSON | data.link       | == | imgur.correct.link       |
      | BODY_JSON | success         | == | true                     |
      | BODY_JSON | status          | == | 200                      |

  Структура сценария: Авторизованное обновление информации изоборажения с указанием корректных значений параметров "title" и "description"
    Когда выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUpdateResponse"
      | ACCESS_TOKEN    | Authorization | imgur.api.bearer |
      | PATH_PARAMETER  | hash          | imageHash        |
      | MULTIPART       | title         | <title>          |
      | MULTIPART       | description   | <description>    |
    Тогда ответ Response из переменной "imageUpdateResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |
    
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageHash                       |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | STATUS    | message           | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data.id           | == | imageHash        |
      | BODY_JSON | data.title        | == | <title>          |
      | BODY_JSON | data.description  | == | <description>    |
      | BODY_JSON | success           | == | true             |
      | BODY_JSON | status            | == | 200              |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageDeleteHash                 |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |

    Примеры:
      | title               | description         |
      | oneCharString       | text.special.chars  |
      | oneWordString       | seq256Chars         |
      | text.phrase.en      | text.phrase.en      |
      | seq128Chars         | oneWordString       |
      | text.special.chars  | oneCharString       |