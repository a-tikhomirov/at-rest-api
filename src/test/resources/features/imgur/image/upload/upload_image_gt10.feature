# language: ru
@imgur
@image
@negative
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Структура сценария: Загрузка изображения размер которого превышает 10Mb
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | <image-type>  | image         | <image-source>    |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message         | == | HTTP/1.1 400 Bad Request |
      | BODY_JSON | data.error      | == | imgur.error.oversize     |
      | BODY_JSON | success         | == | false                    |
      | BODY_JSON | status          | == | 400                      |

    Примеры:
      | image-type  | image-source              |
      | FILE        | images/testImageGt10.jpg  |
      | BASE64_FILE | images/testImageGt10.jpg  |
      | MULTIPART   | image.url.gt10            |