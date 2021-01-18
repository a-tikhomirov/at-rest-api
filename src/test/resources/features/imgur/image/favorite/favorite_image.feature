# language: ru
@imgur
@image
@favorite
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#5dd1c471-a806-43cb-9067-f5e4fc8f28bd
@Issue=https://link.to.issue.here
Функционал: [Favorite Image]

  Предыстория: Загрузка и удаление изображения
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash   |
      | BODY_JSON | data.deletehash | deleteHash  |

  Сценарий: Добавление изображения в избранное
    Когда выполнен POST запрос на URL "imgur.api.image.fav" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageFavResponse"
      | SPEC            | BearerAuth  |           |
      | PATH_PARAMETER  | hash        | imageHash |
    Тогда ответ Response из переменной "imageFavResponse" соответствует условиям из таблицы
      | BODY_JSON | data          | == | string | favorited |

    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | imageHash   |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageDeleteResponse"
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |

    # TODO @Issue
    # Запрос на добавление изображения в избранное не устанавливает значение параметра data.favorite в true
    # Предполагаю, что это точно можно назвать багом
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id       | == | string   | imageHash |
      | BODY_JSON | data.favorite | == | boolean  | true      |