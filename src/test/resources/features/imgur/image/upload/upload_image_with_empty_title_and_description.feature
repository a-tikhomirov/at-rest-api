# language: ru
@imgur
#@image
@upload
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть установлено значение переменной "emptyString" равным ""

  Структура сценария: Загрузка изображения с указанием пустого значения параметров "title" и "description"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC          | BearerAuth    |                   |
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | title         | emptyString       |
      | MULTIPART     | description   | emptyString       |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash |
      | BODY_JSON | data.id | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess     | -     | none   | none |
      | BODY  | data.title        | null  | none   |      |
      | BODY  | data.description  | null  | none   |      |

    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |               |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id           | ==    | string | imageHash          |
      | BODY_JSON | data.link         | ==    | string | imgur.correct.link |
      | BODY_JSON | data.title        | null  | none   |                    |
      | BODY_JSON | data.description  | null  | none   |                    |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC  | BearerAuth  |     |

    Примеры:
      | image-type  | image-source              |
      | FILE        | images/testImageLt10.jpg  |
      | BASE64_FILE | images/testImageLt10.jpg  |
      | MULTIPART   | image.url.lt10            |