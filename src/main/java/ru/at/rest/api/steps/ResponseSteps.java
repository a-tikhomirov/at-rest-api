package ru.at.rest.api.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.text.MatchesPattern;
import ru.at.rest.api.cucumber.CoreScenario;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.fail;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;

@Log4j2
public class ResponseSteps {

    private static final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Проверка Response на соответствие условиям из таблицы
     *
     * @param responseVarName      имя переменной, в которой хранится Response
     * @param dataTable            Таблица условий проверки.
     *                             Формат таблицы смотреть описании метода {@link #checkResponse(Response, DataTable) checkResponse}
     */
    @И("ответ Response из переменной {string} соответствует условиям из таблицы")
    public void checkResponseElements(String responseVarName, DataTable dataTable) {
        Response response = tryGetResponseFromVar(responseVarName);
        checkResponse(response, dataTable);
    }

    /**
     * Сохранение отдельных элементов Response в заданные переменные в соответствие с таблицей
     *
     * @param responseVarName      имя переменной, в которой хранится Response
     * @param dataTable            Таблица с указанием элементов Response для сохранения и названиями переменных.
     *                             Формат таблицы смотреть описании метода {@link #saveResponse(Response, DataTable) saveResponse}
     */
    @И("выполнено сохранение элементов Response из переменной {string} в соответствии с таблицей")
    public void saveResponseElementsToVars(String responseVarName, DataTable dataTable) {
        Response response = tryGetResponseFromVar(responseVarName);
        saveResponse(response, dataTable);
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
     * Проверка Response на соответствие условиям из таблицы
     *
     * @param response          объект класса Response для проверки
     * @param dataTable         Таблица условий проверки в формате:
     *                          | <часть Response для проверки> | <элемент для проверки> | <операция проверки> | <ожидаемое значение> |
     *                          ...
     *                          | <часть Response для проверки> | <элемент для проверки> | <операция проверки> | <ожидаемое значение> |
     *                          возможные значения <часть Response для проверки> cм. {@link ResponsePart}
     *                          возможные значения <элемент для проверки> зависят от проверяемой части Response
     *                          возможные значения <операция проверки> cм. {@link #defineOperation(OperationType) defineOperation}
     *                          <ожидаемое значение> может быть задано как непосредственно значение для проверки или как имя для значения в property файле/в хранилище переменных
     *                          В элементе <ожидаемое значение> возможно использовать параметризацию, см. в {@link ru.at.rest.api.cucumber.ScopedVariables#resolveVars(String) resolveVars}
     */
    private void checkResponse(Response response, DataTable dataTable) {
        log.info(format("Проверка ответа Response на соответствие таблице:\n%s", dataTable));
        StringBuilder errorMessage = new StringBuilder();
        String actualValue;
        String assertionMessage = "\nФактическое %s: [%s] = [%s] не соответствует выражению [%s] для ожидаемого значения [%s]";
        if (dataTable.isEmpty()) {
            errorMessage.append("Отсутсвуют данные для проверки Response");
        }
        for (List<String> responseParam : dataTable.asLists()) {
            ResponsePart responsePart = ResponsePart.get(responseParam.get(0).toUpperCase());
            String key = responseParam.get(1);
            OperationType operation = OperationType.get(responseParam.get(2));
            String expectedValue = resolveVars(loadValuePropertyOrVariableOrDefault(responseParam.get(3)));
            log.info(format("Проверка Response: [%s]:[%s] [%s] [%s]", responsePart, key, operation, expectedValue));

            Function<String, Matcher> matcher = defineOperation(operation);
            try {
                actualValue = getResponseElementValue(response, responsePart, key);
                assertThat(format(assertionMessage, responsePart.getName(), key, actualValue, operation, expectedValue), actualValue, matcher.apply(expectedValue));
                log.info("--------------------------PASS--------------------------\n");
            } catch (AssertionError e) {
                log.info("--------------------------FAIL--------------------------\n");
                errorMessage.append(e.getMessage());
            }
        }
        if (!errorMessage.toString().isEmpty()) {
            attachErrorMessage(errorMessage.toString());
        }
    }

    /**
     * Сохранение отдельных элементов Response в заданные переменные в соответствие с таблицей
     *
     * @param response          объект класса Response для проверки
     * @param dataTable         Таблица с указанием элементов Response для сохранения и названиями переменных в формате:
     *                          | <часть Response> | <элемент для сохранения> | <имя переменной> |
     *                          ...
     *                          | <часть Response> | <элемент для сохранения> | <имя переменной> |
     *                          возможные значения <часть Response> cм. {@link ResponsePart}
     *                          возможные значения <элемент для сохранения> зависят от указанной части Response
     */
    private void saveResponse(Response response, DataTable dataTable) {
        log.info(format("Сохранение элементов ответа Response в переменные в соответствии таблицей:\n%s", dataTable));
        String value;
        if (dataTable.isEmpty()) {
            throw new IllegalArgumentException("Отсутсвуют данные для сохранения элементов Response");
        }
        for (List<String> responseParam : dataTable.asLists()) {
            ResponsePart responsePart = ResponsePart.get(responseParam.get(0).toUpperCase());
            String key = responseParam.get(1);
            String varName = responseParam.get(2);
            log.info(format("Сохранения элемента Response: [%s]:[%s] в переменную с именем [%s]", responsePart, key, varName));

            value = getResponseElementValue(response, responsePart, key);
            coreScenario.getEnvironment().setVar(varName, value);
        }
    }

    /**
     * Возвращает содержимое элемента Response
     *
     * @param response          объект класса Response для выборки содержимого
     * @param responsePart      часть Response для выборки содержимого. См. {@link ResponsePart}
     * @param key               элемент части Response для выборки содержимого
     * @return                  содержимое элемента части Response
     */
    private String getResponseElementValue(Response response, ResponsePart responsePart, String key) {
        String value = null;
        String errorMessage = "Ошибка: в части ответа %s не найден элемент с ключом %s";
        try {
            switch (responsePart) {
                case STATUS:
                    if (key.equals("message")) {
                        value = response.getStatusLine();
                    } else if (key.equals("code")) {
                        value = String.valueOf(response.getStatusCode());
                    } else {
                        throw new IllegalArgumentException(format("Неверно задан элемент для проверка STATUS: %s\nВозможные варианты: [message, code]", key));
                    }
                    break;
                case HEADER: {
                    value = response.getHeader(key);
                    break;
                }
                case BODY_JSON: {
                    if (response.getBody().jsonPath().get(key) != null) {
                        value = response.getBody().jsonPath().get(key).toString();
                    }
                    break;
                }
                case BODY_HTML: {
                    value = response.htmlPath().getString(key);
                    break;
                }
                default: {
                    throw new IllegalArgumentException(format("Не задано поведение для части ответа %s", responsePart));
                }
            }
        } catch (IllegalArgumentException e) {
            attachErrorMessage(format(errorMessage, responsePart, key));
        }
        return value;
    }

    /**
     * Выводит в лог сообщение об ошибке и прикрепляет сообщение к шагу в отчете allure
     *
     * @param message           сообщение об ошибке
     */
    private void attachErrorMessage(String message) {
        log.error(message);
        coreScenario.getScenario().attach(message, "text/plain", "error message");
        fail(message);
    }

    /**
     * Определение Hamcrest-матчера по оператору сравнения:
     * '==' - равенство, '!=' - неравенство, '~' - соответствие регулярному выражению, '!~' - несоответствие регулярному выражению
     * 'null' - проверка на null, 'not null' - проверка на not(null)
     *
     * @param operation         тип операции {@link OperationType}
     * @return                  Hamcrest-матчер для проверки
     */
    private static Function<String, Matcher> defineOperation(OperationType operation) {
        Function<String, Matcher> matcher;
        switch (operation) {
            case EQUAL:
                matcher = Matchers::equalTo;
                break;
            case NOT_EQUAL:
                matcher = s -> not(equalTo(s));
                break;
            case MATCH:
                matcher = MatchesPattern::matchesPattern;
                break;
            case NOT_MATCH:
                matcher = s -> not(matchesPattern(s));
                break;
            case NULL:
                matcher = s -> is(nullValue());
                break;
            case NOT_NULL:
                matcher = s -> is(notNullValue());
                break;
            default:
                throw new IllegalArgumentException("Не задано поведение для операции: " + operation);
        }
        return matcher;
    }

    @AllArgsConstructor
    public enum ResponsePart {
        STATUS("содержимое статуса"),
        HEADER("содержимое заголовка"),
        BODY_JSON("содержимое тела в формате json"),
        BODY_HTML("содержимое тела в формате html");

        @Getter
        private final String name;

        public static ResponsePart get(String value) {
            for(ResponsePart v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException(format("Не найдена часть Response: %s\nВозможные значения: %s",
                    value, Arrays.asList(ResponsePart.values())));
        }
    }

    @AllArgsConstructor
    public enum OperationType {
        EQUAL("=="),
        NOT_EQUAL("!="),
        MATCH("~"),
        NOT_MATCH("!~"),
        NULL("null"),
        NOT_NULL("not null");

        @Getter
        private final String value;

        public static OperationType get(String value) {
            for(OperationType v : values())
                if(v.getValue().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException(format("Не найден тип операции: %s\nВозможные значения: %s",
                    value, Arrays.asList(OperationType.values())));
        }

        @Override public String toString() {
            return value;
        }
    }
}
