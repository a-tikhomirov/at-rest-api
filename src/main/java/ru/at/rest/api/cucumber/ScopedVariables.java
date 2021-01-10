package ru.at.rest.api.cucumber;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static ru.at.rest.api.utils.PropertyLoader.loadProperty;

@Log4j2
public class ScopedVariables {

    private static final String CURVE_BRACES_PATTERN = "\\{([^{}]+)\\}";
    private Map<String, Object> variables = Maps.newHashMap();

    /**
     * Проверяет заданную строку на возможность подставновки параметров.
     * В случае нахождения параметра в строке заменяет его значение на значение из properties или хранилища переменных.
     * Пример: в файле property есть запись: 'prop.value = test'
     * При обработке строки: 'some_{prop.value}' будет получена строка: 'some_test'
     *
     * @param inputString       заданная строка
     * @return                  новая строка
     */
    public static String resolveVars(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }
        log.info(format("Проверка строки %s на возможность подстановки параметров", inputString));
        Pattern p = Pattern.compile(CURVE_BRACES_PATTERN);
        Matcher m = p.matcher(inputString);
        String newString = "";
        while (m.find()) {
            String varName = m.group(1);
            String value = loadProperty(varName, (String) CoreScenario.getInstance().tryGetVar(varName));
            if (value == null) {
                log.info(format("Значение %s не было найдено ни в properties, ни в environment переменной", varName));
                newString = m.replaceFirst("__[__" + m.group(1) + "__]__");
            } else {
                newString = m.replaceFirst(value);
            }
            m = p.matcher(newString);
        }
        newString = newString.replaceAll("__\\[__", "{").replaceAll("__\\]__", "}");
        if (newString.isEmpty()) {
            newString = inputString;
        } else {
            log.info(format("Найден параметр для подстановки. Новое значение строки %s = %s", inputString, newString));
        }
        return newString;
    }

    public void put(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        return variables.get(name);
    }

    public void clear() {
        variables.clear();
    }

    public Object remove(String key) {
        return variables.remove(key);
    }

}
