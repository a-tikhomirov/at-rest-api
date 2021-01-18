# language: ru
@imgur
@image
@favorite
@negative
@Link=https://apidocs.imgur.com/#5dd1c471-a806-43cb-9067-f5e4fc8f28bd
Функционал: [Favorite Image]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash   |
      | BODY_JSON | data.deletehash | deleteHash  |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

  Сценарий: Добавление изображения в избранное с указанием некорректного заголовка "Authorization"
    Когда выполнен POST запрос на URL "imgur.api.image.fav" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageFavResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | imageHash   |
    Тогда ответ Response из переменной "imageFavResponse" соответствует условиям из таблицы
      | SPEC  | Unauthorized403 | - | none  | none  |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |