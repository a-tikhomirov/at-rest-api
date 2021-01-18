# language: ru
@imgur
@image
@delete-auth
@negative
@RequestSpec=BearerAuth
@Link=https://apidocs.imgur.com/#ca48883b-6964-4ab8-b87f-c274e32a970d
@Issue=https://link.to.issue.here
Функционал: [Image Delete Auth]

  Предыстория: Загрузка и удаление изображения
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | MULTIPART | image   | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | imageHash       |
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | UploadSuccess | - | none  | none  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify". Полученный ответ сохранен в переменную "imageDeleteResponse"
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |

  Сценарий: Авторизованное удаление уже удаленного изображения
    # TODO @Issue
    # Отсутствует сообщение об ошибке при запросе удаления уже удаленного изображения
    # Предполагаю, что это можно назвать багом
    Когда выполнен DELETE запрос на URL "imgur.api.image.modify". Полученный ответ сохранен в переменную "imageDeleteResponse"
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC | Html404  | - | none | none |