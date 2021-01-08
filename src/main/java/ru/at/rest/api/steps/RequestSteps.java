package ru.at.rest.api.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.ru.И;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSender;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.utils.Utils;
import ru.at.rest.api.cucumber.CoreScenario;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;
import static ru.at.rest.api.utils.Utils.requestSpecToString;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;

@Log4j2
public class RequestSteps {

    private static final CoreScenario coreScenario = CoreScenario.getInstance();
    private static final String REQUEST_URL = "выполнен {method} запрос на URL {string}";

    @ParameterType("GET|PUT|POST|DELETE|HEAD|TRACE|OPTIONS|PATCH")
    public Method method(String method){
        return Method.valueOf(method);
    }

    /**
     * Отправка http запроса по заданному url с параметрами из таблицы.
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     * @param dataTable            таблица параметров для http запроса. Формат таблицы см. в {@link #createRequest(DataTable) createRequest}
     */
    @И(REQUEST_URL + " с headers и parameters из таблицы")
    public void sendHttpRequest(Method method, String address, DataTable dataTable) {
        sendRequest(method, address, dataTable);
    }

    /**
     * Отправка http запроса по заданному url с параметрами из таблицы.
     * Результат сохраняется в заданную переменную
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     * @param responseNameVariable имя переменной в которую сохраняется ответ
     * @param dataTable            таблица параметров для http запроса. Формат таблицы см. в {@link #createRequest(DataTable) createRequest}
     */
    @И(REQUEST_URL + " с headers и parameters из таблицы. Полученный ответ сохранен в переменную {string}")
    public void sendHttpRequestSaveResponse(Method method,
                                            String address,
                                            String responseNameVariable,
                                            DataTable dataTable) {
        Response response = sendRequest(method, address, dataTable);
        coreScenario.setVar(responseNameVariable, response);
    }

    /**
     * Отправка HTTP запроса
     *
     * @param method                метод HTTP запроса
     * @param address               url, на который будет направлен запрос
     * @param dataTable             таблица параметров для http запроса. Формат таблицы см. в {@link #createRequest(DataTable) createRequest}
     * @return Response             ответ Response на отправленный запрос
     */
    @SneakyThrows
    private Response sendRequest(Method method, String address, DataTable dataTable) {
        log.info("Формирование запроса для отправки...");
        address = resolveVars(loadValuePropertyOrVariableOrDefault(address));
        log.info(format("Отправка %s запроса на url %s с параметрами:\n%s", method, address, dataTable.toString()));

        RequestSender request = createRequest(dataTable);
        Response response = request.request(method, address);

        log.info("Отправлен запрос:\n" + requestSpecToString(request, method, address));
        log.info(format("Получен ответ:\n%s\n%s", response.getStatusLine(), response.asPrettyString()));

        return response;
    }

    /**
     * Возвращает сформированный на основе данных из таблицы dataTable объекта класса RequestSender
     *
     * @param dataTable             Таблица с указанием элементов Request для установки значений в формате:
     *                              | <часть Request> | <ключ/имя элемента> | <устанавливаемое значение> |
     *                              ...
     *                              | <часть Request> | <ключ/имя элемента> | <устанавливаемое значение> |
     *                              возможные значения <часть Request> cм. {@link RequestPart}
     *                              возможные значения <ключ/имя элемента> зависят от части Request
     *                              возможные значения <устанавливаемое значение> также зависят от части Request
     *                              <устанавливаемое значение> может быть задано как непосредственно значение как имя для значения в property файле/в хранилище переменных
     *                              В элементе <устанавливаемое значение> возможно использовать параметризацию, см. в {@link ru.at.rest.api.cucumber.ScopedVariables#resolveVars(String) resolveVars}
     * @return                      сформированный запрос в виде объекта класса RequestSender
     */
    private RequestSender createRequest(DataTable dataTable) {
        RequestSpecification request = RestAssured.given();
        if (dataTable != null) {
            for (List<String> requestParam : dataTable.asLists()) {
                RequestPart requestPart = RequestPart.get(requestParam.get(0).toUpperCase());
                String key = requestParam.get(1);
                String value = resolveVars(loadValuePropertyOrVariableOrDefault(requestParam.get(2)));

                this.setRequestElementValue(request, requestPart, key, value);
            }
        }
        return request;
    }

    /**
     * Устанавливает значение элемента для заданной части запроса Request
     *
     * @param request           объект класса RequestSpecification для установки значения
     * @param requestPart       часть Request для установки значения. См. {@link RequestPart}
     * @param key               имя/ключ элемента части Request для установки значения
     * @param value             устанавливаемое значение
     */
    private void setRequestElementValue(RequestSpecification request, RequestPart requestPart, String key, String value) {
        switch (requestPart) {
            case HEADER: {
                request.header(key, value);
                break;
            }
            case BASIC_AUTHENTICATION: {
                request.auth().basic(key, value);
                break;
            }
            case ACCESS_TOKEN: {
                request.header(key, "Bearer " + value);
                break;
            }
            case PARAMETER: {
                request.queryParam(key, value);
                break;
            }
            case FORM_PARAMETER: {
                request.formParam(key, value);
                break;
            }
            case PATH_PARAMETER: {
                request.pathParam(key, value);
                break;
            }
            case MULTIPART: {
                request.multiPart(key, value);
                break;
            }
            case FILE: {
                request.multiPart(key, new Utils().getFileFromResources(value));
                break;
            }
            case BASE64_FILE: {
                request.multiPart(key, new Utils().getFileFromResourcesAsBase64String(value));
                break;
            }
            default: {
                throw new IllegalArgumentException(format("Не задано поведение для части запроса %s", requestPart));
            }
        }
    }

    public enum RequestPart {
        HEADER,
        BASIC_AUTHENTICATION,
        ACCESS_TOKEN,
        PARAMETER,
        FORM_PARAMETER,
        PATH_PARAMETER,
        MULTIPART,
        FILE,
        BASE64_FILE;

        public static RequestPart get(String value) {
            for(RequestPart v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException(format("Не найдена часть RequestPart: %s\nВозможные значения: %s",
                    value, Arrays.asList(RequestPart.values())));
        }
    }

}