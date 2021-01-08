package ru.at.rest.api.cucumber;

import io.cucumber.java.Scenario;
import lombok.Getter;

public class CoreEnvironment {
    /**
     * Сценарий (Cucumber.api), с которым связана среда
     */
    @Getter
    private final Scenario scenario;

    /**
     * Пулл переменных, объявленных пользователем внутри сценария
     * ThreadLocal обеспечивает отсутствие коллизий при многопоточном запуске
     * {@link ScopedVariables}
     */
    private final ThreadLocal<ScopedVariables> variables = new ThreadLocal<>();

    public CoreEnvironment(Scenario scenario) {
        this.scenario = scenario;
    }

    /**
     * Возврщает значение переменной по имени, заданного пользователем, из пула переменных {@link #variables}
     * Если переменная с заданным именем не найдена - будет возвращено null
     *
     * @param name  имя переменной
     * @return      значение переменной или null
     */
    public Object getVar(String name) {
        return getVariables().get(name);
    }

    /**
     * Добавляет переменную в пул {@link #variables}
     *
     * @param name   имя переменной
     * @param object значение, которое нужно сохранить в переменную
     */
    public void setVar(String name, Object object) {
        getVariables().put(name, object);
    }

    private ScopedVariables getVariables() {
        if (variables.get() == null) {
            variables.set(new ScopedVariables());
        }
        return variables.get();
    }

}
