# language: ru
@imgur
@image
@get
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#2078c7e0-c2b8-4bc8-a646-6e544b087d0f
Функционал: [Get Image]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                     |
      | MULTIPART | image       | image.url.lt10      |
      | MULTIPART | title       | initial title       |
      | MULTIPART | description | initial description |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash |
      | BODY_JSON | data.id | hash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC      | UploadSuccess     | -  | none   | none                |
      | BODY_JSON | data.title        | == | string | initial title       |
      | BODY_JSON | data.description  | == | string | initial description |

  Сценарий: Получение информации об изображении
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |           |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id           | == | string | imageHash            |
      | BODY_JSON | data.link         | == | string | imgur.correct.link   |
      | BODY_JSON | data.title        | == | string | initial title        |
      | BODY_JSON | data.description  | == | string | initial description  |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC      | BearerAuth  |                     |