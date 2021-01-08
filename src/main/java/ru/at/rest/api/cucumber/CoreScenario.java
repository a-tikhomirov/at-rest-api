package ru.at.rest.api.cucumber;

import io.cucumber.java.Scenario;

public class CoreScenario {

    private static final CoreScenario instance = new CoreScenario();

    /**
     * Среда прогона тестов, хранит в себе: Cucumber.Scenario и переменные, объявленные пользователем
     */
    private static final ThreadLocal<CoreEnvironment> environment = new ThreadLocal<>();

    private CoreScenario() {
    }

    public static CoreScenario getInstance() {
        return instance;
    }

    public CoreEnvironment getEnvironment() {
        return environment.get();
    }

    /**
     * Инициализация окружения для хранения переменных сценария
     */
    public void setEnvironment(CoreEnvironment coreEnvironment) {
        environment.set(coreEnvironment);
    }

    /**
     * Возвращает текущий сценарий (Cucumber.api)
     */
    public Scenario getScenario() {
        return this.getEnvironment().getScenario();
    }

    /**
     * Возврщает части пути к файлу сценария начиная с //features/
     *
     * @return      части пути к файлу сценария начиная с //features/
     */
    public String getScenarioPath() {
        String scenarioFullPath = this.getScenario().getId();
        return scenarioFullPath.substring(scenarioFullPath.indexOf("features"), scenarioFullPath.lastIndexOf('/'));
    }

    /**
     * Возврщает сокращенный ID сценария
     *
     * @return      ID сценария в формате feature_file.feature:ID
     */
    public String getScenarioId() {
        String fullID = this.getScenario().getId();
        return fullID.substring(fullID.lastIndexOf('/') + 1).replace(':', '_');
    }

    /**
     * Возврщает значение переменной по имени, заданного пользователем, из пула переменных {@link CoreEnvironment}
     * Если переменная с заданным именем не найдена - кидает исключение
     *
     * @param name  имя переменной
     * @return      значение переменной по заданному имени
     *              если переменная с заданным именем не найдена - будет выброшено исключение
     */
    public Object getVar(String name) {
        Object obj = this.tryGetVar(name);
        if (obj == null) {
            throw new IllegalArgumentException("Переменная " + name + " не найдена");
        }
        return obj;
    }

    /**
     * Возврщает значение переменной по имени, заданного пользователем, из пула переменных {@link CoreEnvironment}
     * Если переменная с заданным именем не найдена - будет возвращено null
     *
     * @param name  имя переменной
     * @return      значение переменной по заданному имени/null
     */
    public Object tryGetVar(String name) {
        return this.getEnvironment().getVar(name);
    }

    /**
     * Добавляет переменную в пул "variables" в классе CoreEnvironment
     *
     * @param name   имя переменной. Является ключом в пуле переменных {@link CoreEnvironment}
     * @param object значение, которое нужно сохранить в переменную
     */
    public void setVar(String name, Object object) {
        this.getEnvironment().setVar(name, object);
    }

}
