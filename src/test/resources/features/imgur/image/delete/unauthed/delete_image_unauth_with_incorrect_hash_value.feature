# language: ru
@imgur
@image
@delete
@negative
@Link=https://apidocs.imgur.com/#949d6cb0-5e55-45f7-8853-8c44a108399c
Функционал: [Image Delete Un-Authed]

  Предыстория: Загрузка изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash   |
      | BODY_JSON | data.deletehash | deleteHash  |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

  Сценарий: Неавторизованное удаление изоборажения с указанием некорректного значения "imageDeleteHash"
    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | imageHash   |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | Unauthorized403 | - | none  | none    |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |