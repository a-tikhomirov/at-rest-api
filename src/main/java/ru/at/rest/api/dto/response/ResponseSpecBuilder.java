package ru.at.rest.api.dto.response;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.specification.ResponseSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import ru.at.rest.api.utils.ResourceLoader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;
import static ru.at.rest.api.cucumber.plugin.AllureCucumber6Jvm.createDataTableAttachment;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;
import static ru.at.rest.api.utils.PropertyLoader.tryLoadProperty;
import static ru.at.rest.api.utils.Utils.getDataTableFromFile;

@Log4j2
public class ResponseSpecBuilder {

    /**
     * Хранилище заготовок для ResponseSpecification в формате Имя, {@link ResponseSpecData}
     */
    private static ConcurrentHashMap<String, ResponseSpecData> responseSpecs;

    public static void initResponseSpecs(Map<String, ResponseSpecData> specs) {
        responseSpecs = new ConcurrentHashMap<>(specs);
    }

    /**
     * Создает пулл заготовок под ResponseSpecification из файлов, находящихся по
     * пути, указанному в файле свойств prebuild.response.specs
     * Если свойство pprebuild.response.specs не указано - возвращает null
     *
     * @return      карта заготовок под ResponseSpecification в формате Map<Имя заготовки, Заготовка>
     */
    public static Map<String, ResponseSpecData> buildResponseSpecsFromResources() {
        log.debug("Проверка на необходимость подготовки ResponseSpecification");
        String path = tryLoadProperty("prebuild.response.specs");
        Map<String, ResponseSpecData> responseSpecDataMap = null;
        if (path != null) {
            log.debug("Установлен параметр prebuild.response.specs = " + path);
            File[] responseFiles = ResourceLoader.getInstance().getResourceFolderFiles(path);
            responseSpecDataMap = new HashMap<>();
            for (File responseFile : responseFiles) {
                log.debug("Подготовка ResponseSpecification из файла: " + responseFile.getPath());
                ResponseSpecData responseSpecData = getResponseSpecDataFromTable(getDataTableFromFile(responseFile));
                if (responseSpecData != null) {
                    responseSpecDataMap.put(
                            responseFile.getName(),
                            responseSpecData
                    );
                }
            }
        }
        return responseSpecDataMap;
    }

    /**
     * Возвращает заготовку под ResponseSpecification {@link ResponseSpecData}
     *
     * @param key       имя заготовки под ResponseSpecification
     * @return          заготовка под ResponseSpecification в формате {@link ResponseSpecData}
     */
    public static ResponseSpecData getResponseSpec(String key) {
        ResponseSpecData responseSpec = responseSpecs.get(key);
        if (responseSpec == null) {
            throw new IllegalArgumentException("Не найдена спецификация ответа с именем: " + key);
        }
        return responseSpec;
    }

    /**
     * Создание заготовки под ResponseSpecification из объекта класса DataTable
     *
     * @param table     объект класса DataTable, в котором хранятся данные для создания заготовки под ResponseSpecification
     * @return          заготовка под ResponseSpecification в формате {@link ResponseSpecData}
     */
    public static ResponseSpecData getResponseSpecDataFromTable(DataTable table) {
        if (table.isEmpty()) return null;
        ResponseSpecData responseSpecData = new ResponseSpecData();
        table.cells().stream().map(fields -> new ResponseSpecLine(
                ResponsePart.get(fields.get(0).toUpperCase()),
                fields.get(1),
                OperationType.get(fields.get(2)),
                ValueType.get(fields.get(3).toUpperCase()),
                loadValuePropertyOrVariableOrDefault(fields.get(4))
        )).forEach(responseSpecData::add);
        return responseSpecData;
    }

    /**
     * Возвращает сформированный на основе данных из таблицы responseSpecData объекта класса ResponseSpecification
     *
     * @param responseSpecData  Таблица условий проверки в формате:
     *                          | <часть Response для проверки> | <элемент для проверки> | <операция проверки> | <тип проверяемого значения> | <ожидаемое значение> |
     *                          ...
     *                          | <часть Response для проверки> | <элемент для проверки> | <операция проверки> | <тип проверяемого значения> | <ожидаемое значение> |
     *                          возможные значения <часть Response для проверки> cм. {@link ResponsePart}
     *                          возможные значения <элемент для проверки> зависят от проверяемой части Response
     *                          возможные значения <операция проверки> cм. {@link OperationType}
     *                          возможные значения <тип проверяемого значения> cм. {@link ValueType}
     *                          <ожидаемое значение> может быть задано как непосредственно значение для проверки или как имя для значения в property файле/в хранилище переменных
     *                          В элементе <ожидаемое значение> возможно использовать параметризацию, см. в {@link ru.at.rest.api.cucumber.ScopedVariables#resolveVars(String) resolveVars}
     * @return                  спецификация ответа ResponseSpecification
     */
    public static ResponseSpecification createResponseSpec(ResponseSpecData responseSpecData) {
        ResponseSpecification responseSpec = RestAssured.expect();
        if (responseSpecData != null) {
            for (ResponseSpecLine responseSpecLine : responseSpecData.getList()) {
                setResponseSpecElementValue(responseSpec,
                        responseSpecLine.getResponsePart(),
                        responseSpecLine.getKey(),
                        responseSpecLine.getOperation(),
                        responseSpecLine.getType(),
                        resolveVars(responseSpecLine.getExpectedValue()));
            }
        }
        return responseSpec;
    }

    /**
     * Устанавливает значение проверки для заданной части ResponseSpecification
     *
     * @param response          объект класса ResponseSpecification для проверки значения
     * @param responsePart      часть Response для проверки значения. См. {@link ResponsePart}
     * @param key               имя/ключ элемента части Request для установки значения
     * @param operation         операция проверки значения. См. {@link OperationType}
     * @param value             устанавливаемое значение
     */
    @SuppressWarnings("unchecked")
    public static void setResponseSpecElementValue(ResponseSpecification response, ResponsePart responsePart, String key, OperationType operation, ValueType type, String value) {
        Function<Object, Matcher> matcher = getMatcher(operation);
        Function<String, Object> mapper = getMapper(type);
        Object expectedValue = null;
        if (mapper != null) {
            expectedValue = mapper.apply(value);
        }
        switch (responsePart) {
            case SPEC: {
                ResponseSpecData responseSpecData = getResponseSpec(key);
                log.info(format("ResponseSpecification %s:\n%s", key, responseSpecData.toDataTable()));
                createDataTableAttachment(Allure.getLifecycle(),"ResponseSpecification " + key, responseSpecData);
                response.spec(createResponseSpec(responseSpecData));
                break;
            }
            case STATUS_CODE: {
                response.statusCode(matcher.apply(expectedValue));
                break;
            }
            case STATUS_LINE: {
                response.statusLine(matcher.apply(expectedValue));
                break;
            }
            case HEADER: {
                response.header(key, matcher.apply(expectedValue));
                break;
            }
            case BODY_JSON:
            case BODY_HTML:
            case BODY: {
                response.body(key, matcher.apply(expectedValue));
                break;
            }
            default: {
                throw new IllegalArgumentException(format("Не задано поведение для части ответа %s", responsePart));
            }
        }
    }

    /**
     * Определение Hamcrest-матчера по оператору сравнения:
     * '==' - равенство, '!=' - неравенство, '~' - соответствие регулярному выражению, '!~' - несоответствие регулярному выражению
     * 'null' - проверка на null, 'not null' - проверка на not(null)
     *
     * @param operation         тип операции {@link OperationType}
     * @return                  Hamcrest-матчер для проверки
     */
    private static Function<Object, Matcher> getMatcher(OperationType operation) {
        Function<Object, Matcher> matcher = null;
        switch (operation) {
            case EQUAL:
                matcher = Matchers::equalTo;
                break;
            case NOT_EQUAL:
                matcher = s -> not(equalTo(s));
                break;
            case MATCH:
                matcher = s -> matchesPattern((String) s);
                break;
            case NOT_MATCH:
                matcher = s -> not(matchesPattern((String) s));
                break;
            case NULL:
                matcher = s -> is(nullValue());
                break;
            case NOT_NULL:
                matcher = s -> is(notNullValue());
                break;
            case NONE:
                break;
            default:
                throw new IllegalArgumentException("Не задано поведение для операции: " + operation);
        }
        return matcher;
    }

    /**
     * Определение операции преобразования объекта в заданный тип
     *
     * @param type          тип для преобразования
     * @return              функция-преобразователь
     */
    private static Function<String, Object> getMapper(ValueType type) {
        Function<String, Object> mapper = null;
        switch (type) {
            case STRING:
                mapper = String::valueOf;
                break;
            case INT:
                mapper = Integer::parseInt;
                break;
            case BOOLEAN:
                mapper = Boolean::valueOf;
                break;
            case NONE:
                break;
            default:
                throw new IllegalArgumentException(format("Не задано поведение типа %s", type));
        }
        return mapper;
    }

    public enum ResponsePart {
        SPEC,
        STATUS_CODE,
        STATUS_LINE,
        HEADER,
        BODY,
        BODY_JSON,
        BODY_HTML;

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
        NOT_NULL("not null"),
        NONE("-");

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

    public enum ValueType {
        STRING,
        INT,
        BOOLEAN,
        NONE;

        public static ValueType get(String value) {
            for(ValueType v : values())
                if(v.toString().equalsIgnoreCase(value)) return v;
            throw new IllegalArgumentException(format("Не найден тип переменной: %s\nВозможные значения: %s",
                    value, Arrays.asList(ValueType.values())));
        }
    }

}
