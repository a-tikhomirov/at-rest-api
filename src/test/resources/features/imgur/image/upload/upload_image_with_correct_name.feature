# language: ru
@imgur
@image
@upload
@positive
@RequestSpec=BearerAuth
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть установлено значение переменной "oneSpaceString" равным " "
    И выполнена генерация 1 случайных EN символов и результат сохранен в переменную "oneCharString"
    И выполнена генерация 5 случайных EN символов и результат сохранен в переменную "oneWordString"
    И выполнена генерация 256 случайных EN символов и результат сохранен в переменную "longLineString"

  Структура сценария: Загрузка изображения с указанием корректного значения параметра "name"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | name          | <name>            |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash |
      | BODY_JSON | data.id | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | -  | none   | none    |
      | BODY  | data.name     | == | string | <name>  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify"

    Примеры:
      | image-type  | image-source              | name                  |
      | FILE        | images/testImageLt10.jpg  | oneCharString         |
      | BASE64_FILE | images/testImageLt10.jpg  | oneWordString         |
      | BASE64_FILE | images/testImageLt10.jpg  | text.phrase.en        |
      | MULTIPART   | image.url.lt10            | longLineString        |
      | MULTIPART   | image.url.lt10            | text.special.chars    |