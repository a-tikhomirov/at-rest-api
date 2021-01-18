# language: ru
@imgur
@image
@update-unauthed
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#9d34ae67-9251-432c-9fe4-caad3fb46121
Функционал: [Image Update Un-Authed]

  Предыстория: Подготовка данных и Загрузка изображения
    Пусть установлено значение переменной "emptyString" равным ""
    И установлено значение переменной "oneSpaceString" равным " "
    
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                     |
      | MULTIPART | image       | image.url.lt10      |
      | MULTIPART | title       | initial title       |
      | MULTIPART | description | initial description |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | deleteHash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess     | -  | none   | none                |
      | BODY  | data.title        | == | string | initial title       |
      | BODY  | data.description  | == | string | initial description |

  Структура сценария: Неавторизованное обновление информации изоборажения с указанием пустых значений параметров "title" и "description"
    Пусть выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |               |
      | PATH_PARAMETER  | hash          | deleteHash    |
      | MULTIPART       | title         | <title>       |
      | MULTIPART       | description   | <description> |
    
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |               |
      | PATH_PARAMETER  | hash          | imageHash     |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id           | ==    | string  | imageHash           |
      | BODY_JSON | data.link         | ==    | string  | imgur.correct.link  |
      | BODY_JSON | data.title        | null  | none    |                     |
      | BODY_JSON | data.description  | null  | none    |                     |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |            |
      | PATH_PARAMETER  | hash          | deleteHash |

    Примеры:
      | title           | description     |
      | emptyString     | oneSpaceString  |
      | oneSpaceString  | emptyString     |