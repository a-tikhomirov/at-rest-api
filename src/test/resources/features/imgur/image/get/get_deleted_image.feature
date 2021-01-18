# language: ru
@imgur
@image
@get
@negative
@Link=https://apidocs.imgur.com/#2078c7e0-c2b8-4bc8-a646-6e544b087d0f
Функционал: [Get Image]

  Предыстория: Загрузка и удаление изображения
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash       |
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC  | BearerAuth    |   |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |

  Сценарий: Получение информации об удаленном изображении
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC  | ClientIDAuth  |     |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | SPEC  | Html404  | - | none | none  |