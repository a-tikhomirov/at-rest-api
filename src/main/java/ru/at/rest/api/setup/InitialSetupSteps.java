package ru.at.rest.api.setup;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import ru.at.rest.api.cucumber.CoreEnvironment;
import ru.at.rest.api.cucumber.CoreScenario;
import ru.at.rest.api.dto.request.RequestSpecData;
import ru.at.rest.api.dto.response.ResponseSpecData;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static java.lang.String.format;
import static ru.at.rest.api.cucumber.plugin.AllureCucumber6Jvm.createDataTableAttachment;
import static ru.at.rest.api.dto.request.RequestSpecBuilder.createRequestSpec;
import static ru.at.rest.api.dto.request.RequestSpecBuilder.getRequestSpec;
import static ru.at.rest.api.dto.response.ResponseSpecBuilder.createResponseSpec;
import static ru.at.rest.api.dto.response.ResponseSpecBuilder.getResponseSpec;
import static ru.at.rest.api.utils.PropertyLoader.tryLoadProperty;
import static ru.at.rest.api.utils.Utils.*;

@Log4j2
public class InitialSetupSteps {

    private static final String startDateTime = getCurrentDateTimeAsString("dd-MM-yyyy HH-mm");

    @Delegate
    CoreScenario coreScenario = CoreScenario.getInstance();

    @Before(order = 0)
    @Step("Инициализация сценария")
    public void initTest(Scenario scenario) {
        coreScenario.setEnvironment(new CoreEnvironment(scenario));

        ThreadContext.put("featureId", coreScenario.getScenarioPath());
        ThreadContext.put("testId", coreScenario.getScenarioId());
        ThreadContext.put("startDateTime", startDateTime);

        log.info(format("%s: старт сценария с именем [%s]\n", coreScenario.getScenarioId(), scenario.getName()));
    }

    @Before(order = 1)
    @Step("Настройка RestAssured")
    public void configureRestAssured(Scenario scenario) {
        log.info("Установка общих параметров RestAssured");
        String baseURI = tryLoadProperty("baseURI");
        if (baseURI == null) {
            log.info("baseURI не было найдено ни файле свойств, ни в системных свойствах. RestAssured.baseURI не установлено");
        } else {
            log.info("RestAssured.baseURI установлено: " + baseURI);
            RestAssured.baseURI = baseURI;
        }
        if(System.getProperty("attachHTTP", "true").equals("true")){
            RestAssured.filters(new AllureRestAssured());
        }
        checkForBaseRequestSpec(scenario);
        checkForBaseResponseSpec(scenario);
    }

    @After
    @Step("Прикрепление лога сценария")
    public void attachLogs(Scenario scenario) throws InterruptedException {
        log.info(format("%s: завершение сценария с именем [%s]\n", coreScenario.getScenarioId(), scenario.getName()));
        RestAssured.reset();
        coreScenario.getEnvironment().clearVars();
        coreScenario.getScenario().attach(readFile(this.getScenarioLogPath(), StandardCharsets.UTF_8), "text/plain", "test log");
        Thread.sleep(Integer.parseInt(System.getProperty("delay", "5000")));
    }

    private String getScenarioLogPath() {
        return format("%s/logs/%s/%s/%s.log",
                Paths.get("").toFile().getAbsolutePath(),
                startDateTime,
                coreScenario.getScenarioPath(),
                coreScenario.getScenarioId());
    }

    private void checkForBaseRequestSpec(Scenario scenario) {
        String requestSpecTag = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@RequestSpec="))
                .findAny()
                .orElse(null);
        if (requestSpecTag != null) {
            String requestSpecName = requestSpecTag.replace("@RequestSpec=", "");
            RequestSpecData requestSpecData = getRequestSpec(requestSpecName);
            createDataTableAttachment(Allure.getLifecycle(), "RequestSpecification " + requestSpecName, requestSpecData);
            log.info(format("Для сценария установлена единая спецификация для всех запросов: %s\n%s", requestSpecName, requestSpecData.toDataTable()));
            RestAssured.requestSpecification = createRequestSpec(requestSpecData);
        } else {
            RestAssured.requestSpecification = null;
        }
    }

    private void checkForBaseResponseSpec(Scenario scenario) {
        String responseSpecTag = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.startsWith("@ResponseSpec="))
                .findAny()
                .orElse(null);
        if (responseSpecTag != null) {
            String responseSpecName = responseSpecTag.replace("@ResponseSpec=", "");
            ResponseSpecData responseSpecData = getResponseSpec(responseSpecName);
            createDataTableAttachment(Allure.getLifecycle(), "ResponseSpecification " + responseSpecName, responseSpecData);
            log.info(format("Для сценария установлена единая спецификация для всех ответов: %s\n%s", responseSpecName, responseSpecData.toDataTable()));
            RestAssured.responseSpecification = createResponseSpec(responseSpecData);
        } else {
            RestAssured.responseSpecification = null;
        }
    }

}
