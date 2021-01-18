# language: ru
@imgur
@image
@update-unauthed
@negative
@Link=https://apidocs.imgur.com/#9d34ae67-9251-432c-9fe4-caad3fb46121
Функционал: [Image Update Un-Authed]

  Предыстория: Загрузка изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash   |
      | BODY_JSON | data.deletehash | deleteHash  |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

  Сценарий: Неавторизованное обновление информации изоборажения с указанием некорректного значения "imageDeleteHash"
    Когда выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUpdateResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | imageHash   |
      | MULTIPART       | title         | some title  |
    Тогда ответ Response из переменной "imageUpdateResponse" соответствует условиям из таблицы
      | STATUS_LINE | message     | == | string   | HTTP/1.1 403 Permission Denied |
      | BODY_JSON   | data.error  | == | string   | imgur.error.permission.denied  |
      | BODY_JSON   | success     | == | boolean  | false                          |
      | BODY_JSON   | status      | == | int      | 403                            |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |