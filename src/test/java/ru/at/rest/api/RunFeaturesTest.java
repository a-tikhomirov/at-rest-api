package ru.at.rest.api;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber6jvm.AllureCucumber6Jvm"},
        tags = "@image",
        features = "src/test/resources/features",
        glue = {"ru"}
)
public class RunFeaturesTest {
}
