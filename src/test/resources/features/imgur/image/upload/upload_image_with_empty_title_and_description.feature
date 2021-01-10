# language: ru
@imgur
@image
@upload
@positive
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть установлено значение переменной "emptyString" равным ""

  Структура сценария: Загрузка изображения с указанием пустого значения параметров "title" и "description"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | title         | emptyString       |
      | MULTIPART     | description   | emptyString       |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash |
      | BODY_JSON | data.deletehash | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message           | ==    | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id           | ~     | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash   | ~     | imgur.correct.deletehash |
      | BODY_JSON | data.title        | null  |                          |
      | BODY_JSON | data.description  | null  |                          |
      | BODY_JSON | data.link         | ==    | imgur.correct.link       |
      | BODY_JSON | success           | ==    | true                     |
      | BODY_JSON | status            | ==    | 200                      |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | HEADER  | Authorization | Client-ID {imgur.api.client.id} |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |

    Примеры:
      | image-type  | image-source              |
      | FILE        | images/testImageLt10.jpg  |
      | BASE64_FILE | images/testImageLt10.jpg  |
      | MULTIPART   | image.url.lt10            |