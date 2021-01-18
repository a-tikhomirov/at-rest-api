# language: ru
@imgur
@image
@update-authed
@positive
@ResponseSpec=CommonSuccess
@Link=https://apidocs.imgur.com/#7db0c13c-bf70-4e87-aecf-047abc65686d
Функционал: [Image Update Auth]

  Предыстория: Подготовка данных и Загрузка изображения
    Пусть выполнена генерация 1 случайных EN символов и результат сохранен в переменную "oneCharString"
    И выполнена генерация 5 случайных EN символов и результат сохранен в переменную "oneWordString"
    И выполнена генерация 128 случайных EN символов и результат сохранен в переменную "seq128Chars"
    И выполнена генерация 256 случайных EN символов и результат сохранен в переменную "seq256Chars"
    
    Когда выполнен POST запрос на URL "imgur.api.image" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageUploadResponse"
      | SPEC      | BearerAuth  |                     |
      | MULTIPART | image       | image.url.lt10      |
    Затем выполнено сохранение элементов Response из переменной "imageUploadResponse" в соответствии с таблицей
      | BODY_JSON | data.id         | imageHash       |
      | BODY_JSON | data.deletehash | deleteHash      |
    Тогда ответ Response из переменной "imageUploadResponse" соответствует условиям из таблицы
      | SPEC      | UploadSuccess     | -  | none   | none                |

  Структура сценария: Авторизованное обновление информации изоборажения с указанием корректных значений параметров "title" и "description"
    Пусть выполнен POST запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | BearerAuth    |                  |
      | PATH_PARAMETER  | hash          | imageHash        |
      | MULTIPART       | title         | <title>          |
      | MULTIPART       | description   | <description>    |
    
    Когда выполнен GET запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы. Полученный ответ сохранен в переменную "imageGetResponse"
      | SPEC            | ClientIDAuth  |           |
      | PATH_PARAMETER  | hash          | imageHash |
    Тогда ответ Response из переменной "imageGetResponse" соответствует условиям из таблицы
      | BODY_JSON | data.id           | == | string | imageHash     |
      | BODY_JSON | data.title        | == | string | <title>       |
      | BODY_JSON | data.description  | == | string | <description> |

    Затем выполнен DELETE запрос на URL "imgur.api.image.modify" с headers и parameters из таблицы
      | SPEC            | ClientIDAuth  |            |
      | PATH_PARAMETER  | hash          | deleteHash |

    Примеры:
      | title               | description         |
      | oneCharString       | text.special.chars  |
      | oneWordString       | seq256Chars         |
      | text.phrase.en      | text.phrase.en      |
      | seq128Chars         | oneWordString       |
      | text.special.chars  | oneCharString       |