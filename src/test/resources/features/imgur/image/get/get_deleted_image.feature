# language: ru
@imgur
@image
@get
@negative
@Link=https://apidocs.imgur.com/#2078c7e0-c2b8-4bc8-a646-6e544b087d0f
Функционал: [Get Image]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | MULTIPART     | image         | image.url.lt10    |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | imageDeleteHash |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message           | == | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id           | ~  | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash   | ~  | imgur.correct.deletehash |
      | BODY_JSON | data.link         | == | imgur.correct.link       |
      | BODY_JSON | success           | == | true                     |
      | BODY_JSON | status            | == | 200                      |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageDeleteHash                 |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |

  Сценарий: Получение информации об удаленном изображении
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageHash                       |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | STATUS    | message         | == | HTTP/1.1 404 Not Found     |
      | BODY_HTML | html.head.title | == | imgur: the simple 404 page |