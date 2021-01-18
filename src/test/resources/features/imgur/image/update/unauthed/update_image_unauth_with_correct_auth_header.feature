# language: ru
@imgur
@image
@update-unauthed
@positive
@ResponseSpec=CommonSuccess
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

  Сценарий: Неавторизованное обновление информации изоборажения с указанием корректного заголовка "Authorization"
    Пусть выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |
      | MULTIPART       | title         | some title  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |