package ru.at.rest.api.setup;

import io.cucumber.java.*;
import io.restassured.RestAssured;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import ru.at.rest.api.cucumber.CoreEnvironment;
import ru.at.rest.api.cucumber.CoreScenario;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static ru.at.rest.api.utils.PropertyLoader.tryLoadProperty;
import static ru.at.rest.api.utils.Utils.getCurrentDateTimeAsString;
import static ru.at.rest.api.utils.Utils.readFile;

@Log4j2
public class InitialSetupSteps {

    private static final String startDateTime = getCurrentDateTimeAsString("dd-MM-yyyy HH-mm");

    @Delegate
    CoreScenario coreScenario = CoreScenario.getInstance();

    @Before()
    public void initTest(Scenario scenario) {
        coreScenario.setEnvironment(new CoreEnvironment(scenario));

        ThreadContext.put("featureId", coreScenario.getScenarioPath());
        ThreadContext.put("testId", coreScenario.getScenarioId());
        ThreadContext.put("startDateTime", startDateTime);

        log.info(String.format("%s: старт сценария с именем [%s]\n", coreScenario.getScenarioId(), scenario.getName()));
        String baseURI = tryLoadProperty("baseURI");
        if (baseURI == null) {
            log.info("baseURI не было найдено ни файле свойств, ни в системных свойствах. RestAssured.baseURI не установлено");
        } else {
            log.info("RestAssured.baseURI установлено: " + baseURI);
            RestAssured.baseURI = baseURI;
        }
    }

    @After
    public void attachLogs(Scenario scenario) throws InterruptedException {
        log.info(String.format("%s: завершение сценария с именем [%s]\n", coreScenario.getScenarioId(), scenario.getName()));
        coreScenario.getScenario().attach(readFile(this.getScenarioLogPath(), StandardCharsets.UTF_8), "text/plain", "test log");
        Thread.sleep(Integer.parseInt(System.getProperty("delay", "5000")));
    }

    private String getScenarioLogPath() {
        return String.format("%s/logs/%s/%s/%s.log",
                Paths.get("").toFile().getAbsolutePath(),
                startDateTime,
                coreScenario.getScenarioPath(),
                coreScenario.getScenarioId());
    }

}
