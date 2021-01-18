# language: ru
@imgur
@image
@delete-auth
@positive
@RequestSpec=BearerAuth
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#ca48883b-6964-4ab8-b87f-c274e32a970d
Функционал: [Image Delete Auth]

  Предыстория: Загрузка изображения
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | MULTIPART | image   | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | hash            |

  Сценарий: Авторизованное удаление изоборажения с указанием корректного заголовка "Authorization"
    Тогда выполнен DELETE запрос на URL "imgur.api.image.modify"