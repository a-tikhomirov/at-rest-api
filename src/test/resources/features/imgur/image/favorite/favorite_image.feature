# language: ru
@imgur
@image
@favorite
@positive
@Link=https://apidocs.imgur.com/#5dd1c471-a806-43cb-9067-f5e4fc8f28bd
@Issue=https://link.to.issue.here
Функционал: [Favorite Image]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | ACCESS_TOKEN  | Authorization | imgur.api.bearer  |
      | MULTIPART     | image         | image.url.lt10    |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | imageDeleteHash |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS    | message         | == | HTTP/1.1 200 OK          |
      | BODY_JSON | data.id         | ~  | imgur.correct.image.id   |
      | BODY_JSON | data.deletehash | ~  | imgur.correct.deletehash |
      | BODY_JSON | data.favorite   | == | false                    |
      | BODY_JSON | data.link       | == | imgur.correct.link       |
      | BODY_JSON | success         | == | true                     |
      | BODY_JSON | status          | == | 200                      |

  Сценарий: Добавление изображения в избранное
    Когда выполнен POST запрос на URL "imgur.api.image.fav" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageFavResponse"
      | ACCESS_TOKEN    | Authorization | imgur.api.bearer  |
      | PATH_PARAMETER  | hash          | imageHash         |
    Тогда ответ Response из переменной "imageFavResponse" соответствует условиям из таблицы
      | STATUS    | message           | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data              | == | favorited        |
      | BODY_JSON | success           | == | true             |
      | BODY_JSON | status            | == | 200              |

    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageHash                       |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | HEADER          | Authorization | Client-ID {imgur.api.client.id} |
      | PATH_PARAMETER  | hash          | imageDeleteHash                 |
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | STATUS    | message | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data    | == | true             |
      | BODY_JSON | success | == | true             |
      | BODY_JSON | status  | == | 200              |

    # TODO @Issue
    # Запрос на добавление изображения в избранное не устанавливает значение параметра data.favorite в true
    # Предполагаю, что это точно можно назвать багом
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | STATUS    | message       | == | HTTP/1.1 200 OK  |
      | BODY_JSON | data.id       | == | imageHash        |
      | BODY_JSON | data.favorite | == | true             |
      | BODY_JSON | success       | == | true             |
      | BODY_JSON | status        | == | 200              |