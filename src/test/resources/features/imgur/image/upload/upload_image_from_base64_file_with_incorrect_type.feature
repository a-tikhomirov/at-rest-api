# language: ru
@imgur
@image
@upload
@negative
@RequestSpec=BearerAuth
@Link=https://apidocs.imgur.com/#c85c9dfc-7487-4de2-9ecd-66f727cf3139
@Issue=https://link.to.issue.here
Функционал: [Image Upload]

  Структура сценария: Загрузка изображения из base64-файла c указанием некорректного значения параметра "type"
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | BASE64_FILE   | image         | images/testImageLt10.jpg  |
      | MULTIPART     | type          | <type>                    |

    # -------------------------------- При предполагаемой корректной работе API Imgur - эти шаги не должны использоваться -------------------------------- #
    # То есть при указании неверного значения параметра type - изображение не должно быть загружено, соответственно и удалять должно быть нечего
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id | hash  |

    Когда выполнен DELETE запрос на URL "imgur.api.image.modify". Полученный ответ сохранен в переменную "imageDeleteResponse"
    Тогда ответ Response из переменной "imageDeleteResponse" соответствует условиям из таблицы
      | SPEC  | CommonSuccess | - | none  | none  |
    # -------------------------------- При предполагаемой корректной работе API Imgur - эти шаги не должны использоваться -------------------------------- #

    # TODO @Issue
    # Отсутствует сообщение об ошибке при загрузке base64-файла и указания некорректного значения параметра "type": URL или file
    # Предполагаю, что это можно назвать багом
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | STATUS_LINE | message             | == | string   | HTTP/1.1 400 Bad Request |
      | BODY_JSON   | data.error.message  | == | string   | imgur.error.invalid.type |
      | BODY_JSON   | success             | == | boolean  | false                    |
      | BODY_JSON   | status              | == | int      | 400                      |

    Примеры:
      | type  |
      | URL   |
      | file  |