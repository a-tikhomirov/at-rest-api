# language: ru
@imgur
@image
@upload
@positive
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть выполнена генерация 1 случайных EN символов и результат сохранен в переменную "oneCharString"
    И выполнена генерация 5 случайных EN символов и результат сохранен в переменную "oneWordString"
    И выполнена генерация 128 случайных EN символов и результат сохранен в переменную "seq128Chars"
    И выполнена генерация 256 случайных EN символов и результат сохранен в переменную "seq256Chars"

  Структура сценария: Загрузка изображения с указанием корректных значений параметров "title", "description" и "type"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | type          | <type>            |
      | MULTIPART     | title         | <title>           |
      | MULTIPART     | description   | <description>     |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | imageDeleteHash |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message           | == | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id           | ~  | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash   | ~  | imgur.correct.deletehash |
      | BODY_JSON | data.title        | == | <title>                  |
      | BODY_JSON | data.description  | == | <description>            |
      | BODY_JSON | data.link         | == | imgur.correct.link       |
      | BODY_JSON | success           | == | true                     |
      | BODY_JSON | status            | == | 200                      |

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
      | image-type  | image-source              | title               | description         | type    |
      | FILE        | images/testImageLt10.jpg  | oneCharString       | text.special.chars  | file    |
      | FILE        | images/testImageLt10.jpg  | oneWordString       | seq256Chars         | file    |
      | BASE64_FILE | images/testImageLt10.jpg  | text.phrase.en      | text.phrase.en      | base64  |
      | BASE64_FILE | images/testImageLt10.jpg  | seq128Chars         | oneWordString       | base64  |
      | MULTIPART   | image.url.lt10            | text.special.chars  | oneCharString       | URL     |