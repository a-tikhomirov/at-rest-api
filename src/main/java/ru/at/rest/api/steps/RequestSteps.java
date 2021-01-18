package ru.at.rest.api.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import io.cucumber.java.ru.И;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSender;
import io.restassured.specification.ResponseSpecification;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.cucumber.CoreScenario;
import ru.at.rest.api.dto.request.RequestSpecBuilder;
import ru.at.rest.api.dto.request.RequestSpecData;

import static java.lang.String.format;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;
import static ru.at.rest.api.dto.request.RequestSpecBuilder.createRequestSpec;
import static ru.at.rest.api.dto.request.RequestSpecBuilder.getRequestSpecDataFromTable;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;
import static ru.at.rest.api.utils.Utils.attachErrorMessage;
import static ru.at.rest.api.utils.Utils.requestSpecToString;

@Log4j2
public class RequestSteps {

    private static final CoreScenario coreScenario = CoreScenario.getInstance();
    private static final String REQUEST_URL = "выполнен {method} запрос на URL {string}";

    @ParameterType("GET|PUT|POST|DELETE|HEAD|TRACE|OPTIONS|PATCH")
    public Method method(String method){
        return Method.valueOf(method);
    }

    @DataTableType
    public RequestSpecData getRequestSpecData(DataTable table) {
        return getRequestSpecDataFromTable(table);
    }

    /**
     * Отправка http запроса по заданному url.
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     */
    @И(REQUEST_URL)
    public void sendSimpleHttpRequest(Method method, String address) {
        sendRequest(method, address, null);
    }

    /**
     * Отправка http запроса по заданному url.
     * Полученный ответ сохраняется в заданную переменную
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     * @param responseNameVariable имя переменной в которую сохраняется ответ
     */
    @И(REQUEST_URL + ". Полученный ответ сохранен в переменную {string}")
    public void sendSimpleHttpRequestSaveResponse(Method method, String address, String responseNameVariable) {
        Response response = sendRequest(method, address, null);
        coreScenario.setVar(responseNameVariable, response);
        log.info("Ответ сохранен в переменную с именем: " + responseNameVariable);
    }

    /**
     * Отправка http запроса по заданному url с параметрами из таблицы.
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     * @param dataTable            таблица параметров для http запроса. Формат таблицы см. в {@link RequestSpecBuilder#createRequestSpec(RequestSpecData) createRequest}
     */
    @И(REQUEST_URL + " с headers и parameters из таблицы")
    public void sendHttpRequest(Method method, String address, RequestSpecData dataTable) {
        sendRequest(method, address, dataTable);
    }

    /**
     * Отправка http запроса по заданному url с параметрами из таблицы.
     * Полученный ответ сохраняется в заданную переменную
     *
     * @param method               метод HTTP запроса
     * @param address              url запроса
     * @param responseNameVariable имя переменной в которую сохраняется ответ
     * @param dataTable            таблица параметров для http запроса. Формат таблицы см. в {@link RequestSpecBuilder#createRequestSpec(RequestSpecData) createRequest}
     */
    @И(REQUEST_URL + " с headers и parameters из таблицы. Полученный ответ сохранен в переменную {string}")
    public void sendHttpRequestSaveResponse(Method method,
                                            String address,
                                            String responseNameVariable,
                                            RequestSpecData dataTable) {
        Response response = sendRequest(method, address, dataTable);
        coreScenario.setVar(responseNameVariable, response);
        log.info("Ответ сохранен в переменную с именем: " + responseNameVariable);
    }

    /**
     * Отправка HTTP на основе ResponseSpecification сформированного по данным из таблицы requestSpecData
     *
     * @param method                метод HTTP запроса
     * @param address               url, на который будет направлен запрос
     * @param requestSpecData       таблица параметров для http запроса. {@link RequestSpecData}
     *                              Формат таблицы см. в {@link RequestSpecBuilder#createRequestSpec(RequestSpecData) createRequest}
     * @return Response             ответ Response на отправленный запрос
     */
    private Response sendRequest(Method method, String address, RequestSpecData requestSpecData) {
        log.info("Формирование запроса для отправки...");
        address = resolveVars(loadValuePropertyOrVariableOrDefault(address));
        if (requestSpecData != null) {
            log.info(format("Подготовка %s запроса на url %s с параметрами:\n%s", method, address, requestSpecData.toDataTable()));
            //createDataTableAttachment(requestSpecData);
        }

        /*
        Сделан сброс RestAssured.responseSpecification чтобы логировать запрос и ответ в Log4j2
        Если оставить RestAssured.responseSpecification и делать лог через RestAssured .log() и
        RestAssured.config = config().logConfig(new LogConfig(logStream, false));
        где logStream - PrintStream логгера Log4j2
        то, в лог файлах отдельных сценариев вывод логов запроса/ответа происходит вперемешку с остальными вызовами log.info()
        Возможно это как то можно победить, но решил оставить так - тоже работает, и вроде красиво :)
        */
        ResponseSpecification responseSpec = RestAssured.responseSpecification;
        RestAssured.responseSpecification = null;

        RequestSender request = createRequestSpec(requestSpecData);
        Response response = request.request(method, address);
        log.info("Отправлен запрос:\n" + requestSpecToString(request, method, address));
        log.info(format("Получен ответ:\n%s\n%s", response.getStatusLine(), response.asPrettyString()));

        if (responseSpec != null) {
            RestAssured.responseSpecification = responseSpec;
            try {
                response.then().spec(responseSpec);
            } catch (AssertionError e) {
                attachErrorMessage(e.getMessage());
                return null;
            }
        }
        return response;
    }

}