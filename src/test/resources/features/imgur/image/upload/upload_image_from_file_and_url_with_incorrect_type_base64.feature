# language: ru
@imgur
@image
@upload
@negative
@RequestSpec=BearerAuth
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Структура сценария: Загрузка изображения с использованием файла/URL и указанием некорректного значения параметра "type"=base64
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | <image-type>  | image       | <image-source>  |
      | MULTIPART     | type        | base64          |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS_LINE | message             | == | string   | HTTP/1.1 400 Bad Request |
      | BODY_JSON   | data.error.message  | == | string   | <error-message>          |
      | BODY_JSON   | success             | == | boolean  | false                    |
      | BODY_JSON   | status              | == | int      | 400                      |

    Примеры:
      | image-type  | image-source              | error-message             |
      | FILE        | images/testImageLt10.jpg  | imgur.error.not.uploaded  |
      | MULTIPART   | image.url.lt10            | imgur.error.invalid.type  |