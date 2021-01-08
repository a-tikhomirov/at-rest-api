package ru.at.rest.api.setup;

import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.io.PrintStream;

import static io.restassured.config.RestAssuredConfig.config;

@Log4j2
public class RestAssuredLoggerSetup {

    private static final Logger logger = LogManager.getLogger(RestAssuredLoggerSetup.class);
    private static final PrintStream logStream = IoBuilder.forLogger(logger).buildPrintStream();

    @Before(order = 1)
    public void restAssuredLoggerSetup() {
        RestAssured.config = config().logConfig(new LogConfig(logStream, false));
    }

}
