package ru.at.rest.api.steps;

import io.cucumber.java.ParameterType;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.cucumber.CoreScenario;

import java.util.Random;

import static java.lang.String.format;
import static ru.at.rest.api.utils.PropertyLoader.loadProperty;

@Log4j2
public class DataGenerationSteps {

    private static final CoreScenario coreScenario = CoreScenario.getInstance();

    @ParameterType("RU|EN")
    public Lang lang(String lang){
        return Lang.valueOf(lang);
    }

    @И("установлено значение переменной {string} равным {string}")
    public void setVariable(String variableName, String value) {
        value = loadProperty(value, value);
        coreScenario.setVar(variableName, value);
        log.info(format("Установлено значение переменной с именем %s равным \"%s\"", variableName, value));
    }

    @И("выполнена генерация {int} случайных {lang} символов и результат сохранен в переменную {string}")
    public void setRandomCharSequence(int seqLength, Lang lang, String varName) {
        String charSeq = getRandCharSequence(seqLength, lang);
        coreScenario.setVar(varName, charSeq);
        log.info(format("Последовательсность случайных %s симовлов длины %s сохранена в переменную %s: %s", seqLength, lang, varName, charSeq));
    }

    @И("в строке из переменной {string} выполнена замена части {string} на {string}. Результат сохранен в переменную {string}")
    public void replaceStringInVarToNewVar(String varName, String regexp, String replacement, String newVar) {
        Object varValue = coreScenario.getVar(varName);
        if (!(varValue instanceof String)) {
            throw new IllegalArgumentException("Данный шаг применим только переменных в которых хранится объект класса String");
        }
        String newValue = String.valueOf(varValue).replaceAll(regexp,replacement);
        coreScenario.setVar(newVar, newValue);
        log.info(format("В строке %s из переменной %s была заменена часть %s на %s. Итоговое значение сохранено в переменную: %s = %s",
                varValue, varName, regexp, replacement, newVar, newValue));
    }

    /**
     * Возвращает последовательность случайных символов переданных алфавита и длины
     *
     * @param length    длина последовательности
     * @param lang      варианты языков, см. {@link Lang}
     */
    public static String getRandCharSequence(int length, Lang lang) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char symbol = charGenerator(lang);
            builder.append(symbol);
        }
        return builder.toString();
    }

    /**
     * Возвращает случайный символ переданного алфавита
     *
     * @param lang      варианты языков, см. {@link Lang}
     */
    public static char charGenerator(Lang lang) {
        Random random = new Random();
        char c;
        switch (lang){
            case RU:
                c = (char) (1072 + random.nextInt(32));
                break;
            case EN:
                c = (char) (97 + random.nextInt(26));
                break;
            default:
                throw new IllegalArgumentException("Не реализована генерация случайного символа для языка: " + lang);
        }
        return c;
    }

    public enum Lang {
        RU,
        EN
    }
}
