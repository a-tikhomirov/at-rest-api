# language: ru
@imgur
@image
@update-authed
@positive
@RequestSpec=BearerAuth
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#7db0c13c-bf70-4e87-aecf-047abc65686d
Функционал: [Image Update Auth]

  Предыстория: Загрузка изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | MULTIPART | image   | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash       |
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

  Сценарий: Авторизованное обновление информации изоборажения с указанием корректного заголовка "Authorization"
    Тогда выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | MULTIPART | title | some title  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify"