# language: ru
@imgur
@image
@upload
@negative
@RequestSpec=BearerAuth
@ResponseSpec=CommonFail
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
@upload_gt10
Функционал: [Image Upload]

  Структура сценария: Загрузка изображения размер которого превышает 10Mb
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы
      | <image-type>  | image       | <image-source>    |

    Примеры:
      | image-type  | image-source              |
      | FILE        | images/testImageGt10.jpg  |
      | BASE64_FILE | images/testImageGt10.jpg  |
      | MULTIPART   | image.url.gt10            |