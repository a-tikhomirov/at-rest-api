package ru.at.rest.api.dto.request;

import io.cucumber.datatable.DataTable;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.utils.ResourceLoader;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static ru.at.rest.api.cucumber.ScopedVariables.resolveVars;
import static ru.at.rest.api.cucumber.plugin.AllureCucumber6Jvm.createDataTableAttachment;
import static ru.at.rest.api.utils.PropertyLoader.loadValuePropertyOrVariableOrDefault;
import static ru.at.rest.api.utils.PropertyLoader.tryLoadProperty;
import static ru.at.rest.api.utils.Utils.getDataTableFromFile;

@Log4j2
public class RequestSpecBuilder {

    /**
     * Хранилище заготовок для RequestSpecification в формате Имя, {@link RequestSpecData}
     */
    private static ConcurrentHashMap<String, RequestSpecData> requestSpecs;

    public static void initRequestSpecs(Map<String, RequestSpecData> specs) {
        requestSpecs = new ConcurrentHashMap<>(specs);
    }

    /**
     * Создает пулл заготовок под RequestSpecification из файлов, находящихся по
     * пути, указанному в файле свойств prebuild.request.specs
     * Если свойство prebuild.request.specs не указано - возвращает null
     *
     * @return      карта заготовок под RequestSpecification в формате Map<Имя заготовки, Заготовка>
     */
    public static Map<String, RequestSpecData> buildRequestSpecsFromResources() {
        log.debug("Проверка на необходимость подготовки RequestSpecification");
        String path = tryLoadProperty("prebuild.request.specs");
        Map<String, RequestSpecData> requestSpecDataMap = null;
        if (path != null) {
            log.debug("Установлен параметр prebuild.request.specs = " + path);
            File[] requestFiles = ResourceLoader.getInstance().getResourceFolderFiles(path);
            requestSpecDataMap = new HashMap<>();
            for (File requestFile : requestFiles) {
                log.debug("Подготовка RequestSpecification из файла: " + requestFile.getPath());
                RequestSpecData requestSpecData = getRequestSpecDataFromTable(getDataTableFromFile(requestFile));
                if (requestSpecData != null) {
                    requestSpecDataMap.put(
                            requestFile.getName(),
                            requestSpecData
                    );
                }
            }
        }
        return requestSpecDataMap;
    }

    /**
     * Возвращает заготовку под RequestSpecification {@link RequestSpecData}
     *
     * @param key       имя заготовки под RequestSpecification
     * @return          заготовка под RequestSpecification в формате {@link RequestSpecData}
     */
    public static RequestSpecData getRequestSpec(String key) {
        RequestSpecData requestSpec = requestSpecs.get(key);
        if (requestSpec == null) {
            throw new IllegalArgumentException("Не найдена спецификация запроса с именем: " + key);
        }
        return requestSpec;
    }

    /**
     * Создание заготовки под RequestSpecification из объекта класса DataTable
     *
     * @param table     объект класса DataTable, в котором хранятся данные для создания заготовки под RequestSpecification
     * @return          заготовка под RequestSpecification в формате {@link RequestSpecData}
     */
    public static RequestSpecData getRequestSpecDataFromTable(DataTable table) {
        if (table.isEmpty()) return null;
        RequestSpecData requestSpecData = new RequestSpecData();
        table.cells().stream().map(fields -> new RequestSpecLine(
                RequestSpecBuilder.RequestPart.get(fields.get(0).toUpperCase()),
                fields.get(1),
                loadValuePropertyOrVariableOrDefault(fields.get(2))
        )).forEach(requestSpecData::add);
        return requestSpecData;
    }

    /**
     * Возвращает сформированный на основе данных из таблицы requestSpecData объекта класса RequestSpecification
     *
     * @param requestSpecData       Таблица с указанием элементов Request для установки значений в формате:
     *                              | <часть Request> | <ключ/имя элемента> | <устанавливаемое значение> |
     *                              ...
     *                              | <часть Request> | <ключ/имя элемента> | <устанавливаемое значение> |
     *                              возможные значения <часть Request> cм. {@link RequestPart}
     *                              возможные значения <ключ/имя элемента> зависят от части Request
     *                              возможные значения <устанавливаемое значение> также зависят от части Request
     *                              <устанавливаемое значение> может быть задано как непосредственно значение как имя для значения в property файле/в хранилище переменных
     *                              В элементе <устанавливаемое значение> возможно использовать параметризацию, см. в {@link ru.at.rest.api.cucumber.ScopedVariables#resolveVars(String) resolveVars}
     * @return                      спецификация запроса RequestSpecification
     */
    public static RequestSpecification createRequestSpec(RequestSpecData requestSpecData) {
        RequestSpecification requestSpec = RestAssured.given();
        if (requestSpecData != null) {
            for (RequestSpecLine requestSpecLine : requestSpecData.getList()) {
                setRequestSpecElementValue(
                        requestSpec,
                        requestSpecLine.getRequestPart(),
                        requestSpecLine.getKey(),
                        resolveVars(requestSpecLine.getValue()));
            }
        }
        return requestSpec;
    }

    /**
     * Устанавливает значение элемента для заданной части запроса RequestSpecification
     *
     * @param request           объект класса RequestSpecification для установки значения
     * @param requestPart       часть Request для установки значения. См. {@link RequestPart}
     * @param key               имя/ключ элемента части Request для установки значения
     * @param value             устанавливаемое значение
     */
    private static void setRequestSpecElementValue(RequestSpecification request, RequestPart requestPart, String key, String value) {
        switch (requestPart) {
            case SPEC: {
                RequestSpecData requestSpecData = getRequestSpec(key);
                log.info(format("RequestSpecification %s:\n%s", key, requestSpecData.toDataTable()));
                createDataTableAttachment(Allure.getLifecycle(), "RequestSpecification " + key, requestSpecData);
                request.spec(createRequestSpec(requestSpecData));
                break;
            }
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
                request.multiPart(key, ResourceLoader.getInstance().getFileFromResources(value));
                break;
            }
            case BASE64_FILE: {
                request.multiPart(key, ResourceLoader.getInstance().getFileFromResourcesAsBase64String(value));
                break;
            }
            default: {
                throw new IllegalArgumentException(format("Не задано поведение для части запроса %s", requestPart));
            }
        }
    }

    public enum RequestPart {
        SPEC,
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
