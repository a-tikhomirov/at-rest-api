# language: ru
@imgur
#@image
@upload
@positive
@RequestSpec=BearerAuth
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть установлено значение переменной "oneSpaceString" равным " "
    И установлено значение переменной "emptyString" равным ""

  Структура сценария: Загрузка изображения с указанием пустого значения параметра "name"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | name          | <name>            |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash |
      | BODY_JSON | data.id         | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess     | -  | none   | none        |
      | BODY  | data.name         | == | string | emptyString |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify"

    Примеры:
      | image-type  | image-source              | name            |
      | FILE        | images/testImageLt10.jpg  | oneSpaceString  |
      | BASE64_FILE | images/testImageLt10.jpg  | emptyString     |