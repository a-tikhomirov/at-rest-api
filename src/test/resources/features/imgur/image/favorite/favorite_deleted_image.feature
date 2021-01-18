# language: ru
@imgur
@image
@favorite
@negative
@RequestSpec=BearerAuth
@Link=https://apidocs.imgur.com/#5dd1c471-a806-43cb-9067-f5e4fc8f28bd
Функционал: [Favorite Image]

  Предыстория: Загрузка и удаление изображения
    Пусть выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | MULTIPART | image   | image.url.lt10  |
    И выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | hash            |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify". Полученный ответ сохранен в переменную "imageDeleteResponse"
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |

  Сценарий: Добавление удаленного изображения в избранное
    Когда выполнен POST запрос на URL "imgur.api.image.fav". Полученный ответ сохранен в переменную "imageFavResponse"
    Тогда ответ Response из переменной "imageFavResponse" соответствует условиям из таблицы
      | SPEC | Html404  | - | none | none |