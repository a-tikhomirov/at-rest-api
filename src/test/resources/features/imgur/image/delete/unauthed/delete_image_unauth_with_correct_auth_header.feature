# language: ru
@imgur
@image
@delete
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#949d6cb0-5e55-45f7-8853-8c44a108399c
Функционал: [Image Delete Un-Authed]

  Предыстория: Загрузка изображения
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                 |
      | MULTIPART | image       | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.deletehash | deleteHash  |

  Сценарий: Неавторизованное удаление изоборажения с указанием корректного заголовка "Authorization"
    Тогда выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |             |
      | PATH_PARAMETER  | hash          | deleteHash  |