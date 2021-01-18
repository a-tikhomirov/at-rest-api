# language: ru
@imgur
@image
@update-authed
@negative
@RequestSpec=BearerAuth
@Link=https://apidocs.imgur.com/#7db0c13c-bf70-4e87-aecf-047abc65686d
Функционал: [Image Update Auth]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | MULTIPART | image   | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash       |
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify". Полученный ответ сохранен в переменную "imageDeleteResponse"
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |

  Сценарий: Авторизованное обновление информации изоборажения для удаленного изображения
    Когда выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUpdateResponse"
      | MULTIPART       | title | some title  |
    Тогда ответ Response из переменной "imageUpdateResponse" соответствует условиям из таблицы
      | SPEC | Html404  | - | none | none     |