# language: ru
@imgur
@image
@upload
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
Функционал: [Image Upload]

  Предыстория: Подготовка данных
    Пусть выполнена генерация 1 случайных EN символов и результат сохранен в переменную "oneCharString"
    И выполнена генерация 5 случайных EN символов и результат сохранен в переменную "oneWordString"
    И выполнена генерация 128 случайных EN символов и результат сохранен в переменную "seq128Chars"
    И выполнена генерация 256 случайных EN символов и результат сохранен в переменную "seq256Chars"

  Структура сценария: Загрузка изображения с указанием корректных значений параметров "title", "description" и "type"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC          | BearerAuth    |                   |
      | <image-type>  | image         | <image-source>    |
      | MULTIPART     | type          | <type>            |
      | MULTIPART     | title         | <title>           |
      | MULTIPART     | description   | <description>     |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash |
      | BODY_JSON | data.id | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess     | -  | none   | none          |
      | BODY  | data.title        | == | string | <title>       |
      | BODY  | data.description  | == | string | <description> |

    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |               |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id           | == | string | imageHash           |
      | BODY_JSON | data.link         | == | string | imgur.correct.link  |
      | BODY_JSON | data.title        | == | string | <title>             |
      | BODY_JSON | data.description  | == | string | <description>       |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC          | BearerAuth    |                   |

    Примеры:
      | image-type  | image-source              | title               | description         | type    |
      | FILE        | images/testImageLt10.jpg  | oneCharString       | text.special.chars  | file    |
      | FILE        | images/testImageLt10.jpg  | oneWordString       | seq256Chars         | file    |
      | BASE64_FILE | images/testImageLt10.jpg  | text.phrase.en      | text.phrase.en      | base64  |
      | BASE64_FILE | images/testImageLt10.jpg  | seq128Chars         | oneWordString       | base64  |
      | MULTIPART   | image.url.lt10            | text.special.chars  | oneCharString       | URL     |