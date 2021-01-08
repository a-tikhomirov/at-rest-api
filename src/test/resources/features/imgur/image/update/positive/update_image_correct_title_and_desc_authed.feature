# language: ru
Функционал: [Image Update]

  Сценарий: Обновление информации изоборажения с корректными title и  description
    Когда выполнен POST запрос на URL "imgur.api.image.update" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUpdateResponse"
      | ACCESS_TOKEN    | Authorization | imgur.api.bearer |
      | PATH_PARAMETER  | hash          | someHash         |
    Когда ответ Response из переменной "imageUpdateResponse" соответствует условиям из таблицы
      | BODY_HTML  | html.head.title  | == | imgur: the simple 404 page             |