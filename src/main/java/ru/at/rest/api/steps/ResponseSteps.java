package ru.at.rest.api.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ru.И;
import io.cucumber.plugin.event.DataTableArgument;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.cucumber.CoreScenario;
import ru.at.rest.api.dto.response.ResponseSpecBuilder;
import ru.at.rest.api.dto.response.ResponseSpecData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static ru.at.rest.api.cucumber.plugin.AllureCucumber6Jvm.createDataTableAttachment;
import static ru.at.rest.api.dto.response.ResponseSpecBuilder.createResponseSpec;
import static ru.at.rest.api.dto.response.ResponseSpecBuilder.getResponseSpecDataFromTable;
import static ru.at.rest.api.utils.Utils.attachErrorMessage;

@Log4j2
public class ResponseSteps {

    private static final CoreScenario coreScenario = CoreScenario.getInstance();

    @DataTableType
    public ResponseSpecData getResponseSpecData(DataTable table) {
        return getResponseSpecDataFromTable(table);
    }

    /**
     * Проверка Response на соответствие условиям из таблицы
     *
     * @param responseVarName      имя переменной, в которой хранится Response
     * @param responseSpecData     Таблица условий проверки.
     *                             Формат таблицы смотреть описании метода {@link #checkResponse(Response, String, ResponseSpecData) checkResponse}
     */
    @И("ответ Response из переменной {string} соответствует условиям из таблицы")
    public void checkResponseElements(String responseVarName, ResponseSpecData responseSpecData) {
        Response response = tryGetResponseFromVar(responseVarName);
        checkResponse(response, responseVarName, responseSpecData);
    }

    /**
     * Сохранение отдельных элементов Response в заданные переменные в соответствие с таблицей
     *
     * @param responseVarName      имя переменной, в которой хранится Response
     * @param dataTable            Таблица с указанием элементов Response для сохранения и названиями переменных.
     *                             Формат таблицы смотреть описании метода {@link #saveResponse(Response, String, DataTable) saveResponse}
     */
    @И("выполнено сохранение элементов Response из переменной {string} в соответствии с таблицей")
    public void saveResponseElementsToVars(String responseVarName, DataTable dataTable) {
        Response response = tryGetResponseFromVar(responseVarName);
        saveResponse(response, responseVarName, dataTable);
    }

    /**
     * Получение объекта класса Response из хранилища переменных
     * Если ничего не найдено или в перменной хранится объект классса, отчлиного от Response - будет выброшено исключение
     *
     * @param responseVarName      имя переменной в хранилище переменных
     * @return                     объект класса Response
     */
    private Response tryGetResponseFromVar(String responseVarName) {
        Object response = coreScenario.getEnvironment().getVar(responseVarName);
        if (!(response instanceof Response)) {
            throw new IllegalArgumentException(String.format("Переменная с именем %s содержит null или объект класса, отличного от Response", responseVarName));
        }
        return (Response) response;
    }

    /**
     * Проверка Response на соответствие условиям из таблицы с помощью формирования ResponseSpecification
     *
     * @param response          объект класса Response для проверки
     * @param responseName      имя ответа Response для отображения в логе
     * @param responseSpecData  Таблица для формирования ResponseSpecification. {@link ResponseSpecData}
     *                          Формат таблицы - См. {@link ru.at.rest.api.dto.response.ResponseSpecBuilder#createResponseSpec(ResponseSpecData) createResponseSpec}
     */
    private void checkResponse(Response response, String responseName, ResponseSpecData responseSpecData) {
        if (responseSpecData != null) {
            log.info(format("Проверка ответа %s на соответствие таблице:\n%s", responseName, responseSpecData.toDataTable()));
            try {
                response.then().spec(createResponseSpec(responseSpecData));
                log.info("--------------------------PASS--------------------------\n");
            } catch (AssertionError e) {
                log.info("--------------------------FAIL--------------------------\n");
                attachErrorMessage(e.getMessage());
            }
        }
    }

    /**
     * Сохранение отдельных элементов Response в заданные переменные в соответствие с таблицей
     *
     * @param response          объект класса Response для проверки
     * @param responseName      имя ответа Response для отображения в логе
     * @param dataTable         Таблица с указанием элементов Response для сохранения и названиями переменных в формате:
     *                          | <часть Response> | <элемент для сохранения> | <имя переменной> |
     *                          ...
     *                          | <часть Response> | <элемент для сохранения> | <имя переменной> |
     *                          возможные значения <часть Response> cм. {@link ResponseSpecBuilder.ResponsePart}
     *                          возможные значения <элемент для сохранения> зависят от указанной части Response
     */
    private void saveResponse(Response response, String responseName, DataTable dataTable) {
        log.info(format("Сохранение элементов ответа %s в переменные в соответствии таблицей:\n%s", responseName, dataTable));
        if (dataTable.isEmpty()) {
            throw new IllegalArgumentException("Отсутсвуют данные для сохранения элементов Response");
        }
        List<List<String>> savedValues = new ArrayList<>();
        for (List<String> responseParam : dataTable.asLists()) {
            ResponseSpecBuilder.ResponsePart responsePart = ResponseSpecBuilder.ResponsePart.get(responseParam.get(0).toUpperCase());
            String key = responseParam.get(1);
            String varName = responseParam.get(2);
            log.info(format("Сохранения элемента Response: [%s]:[%s] в переменную с именем [%s]", responsePart, key, varName));

            Object value = getResponseElementValue(response, responsePart, key);
            savedValues.add(Arrays.asList(responsePart.toString(), key, varName, String.valueOf(value)));
            coreScenario.getEnvironment().setVar(varName, value);
        }
        log.info("Результат сохранения:\n" + DataTable.create(savedValues));
        createDataTableAttachment(Allure.getLifecycle(), "Результат сохранения", new SavedValuesDataTable(savedValues));
    }

    @AllArgsConstructor
    static class SavedValuesDataTable implements DataTableArgument {
        private final List<List<String>> content;

        @Override
        public List<List<String>> cells() {
            return content;
        }

        @Override
        public int getLine() {
            return 0;
        }
    }

    /**
     * Возвращает содержимое элемента Response
     *
     * @param response          объект класса Response для выборки содержимого
     * @param responsePart      часть Response для выборки содержимого. См. {@link ResponseSpecBuilder.ResponsePart}
     * @param key               элемент части Response для выборки содержимого
     * @return                  содержимое элемента части Response
     */
    private Object getResponseElementValue(Response response, ResponseSpecBuilder.ResponsePart responsePart, String key) {
        Object value = null;
        try {
            switch (responsePart) {
                case STATUS_CODE: {
                    value = response.getStatusCode();
                    break;
                }
                case STATUS_LINE: {
                    value = response.getStatusLine();
                    break;
                }
                case HEADER: {
                    value = response.getHeader(key);
                    break;
                }
                case BODY_JSON: {
                    value = response.getBody().jsonPath().get(key);
                    break;
                }
                case BODY_HTML: {
                    value = response.htmlPath().getString(key);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(format("Не задано поведение для сохранения части ответа %s", responsePart));
                }
            }
        } catch (IllegalArgumentException e) {
            attachErrorMessage(format("Ошибка: в части ответа %s не найден элемент с ключом %s", responsePart, key));
        }
        return value;
    }

}
