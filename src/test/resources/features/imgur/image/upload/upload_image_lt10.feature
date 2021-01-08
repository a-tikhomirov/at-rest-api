# language: ru
@imgur
@image
@positive
Функционал: [Image Upload]

  Структура сценария: Загрузка изображения размер которого не превышает 10Mb
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | <image-type>  | image         | <image-source>    |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message         | == | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id         | ~  | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash | ~  | imgur.correct.deletehash |
      | BODY_JSON | success         | == | true                     |
      | BODY_JSON | status          | == | 200                      |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.deletehash | hash |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | HEADER  | Authorization | Client-ID {imgur.api.client.id} |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |

    Примеры:
      | image-type  | image-source              |
      | BASE64_FILE | images/testImageLt10.jpg  |
      | FILE        | images/testImageLt10.jpg  |
      | MULTIPART   | image.url.lt10            |