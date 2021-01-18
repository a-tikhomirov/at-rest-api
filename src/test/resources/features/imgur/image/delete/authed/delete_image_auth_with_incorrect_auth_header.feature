# language: ru
@imgur
@image
@delete-auth
@negative
@Link=https://apidocs.imgur.com/#ca48883b-6964-4ab8-b87f-c274e32a970d
Функционал: [Image Delete Auth]

  Предыстория: Загрузка изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash       |
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

  Сценарий: Авторизованное удаление изоборажения с указанием некорректного заголовка "Authorization"
    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC  | ClientIDAuth    |   |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | Unauthorized403 | - | none  | none  |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC  | BearerAuth    |   |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |