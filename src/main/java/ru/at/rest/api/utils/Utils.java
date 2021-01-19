package ru.at.rest.api.utils;

import io.cucumber.datatable.DataTable;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Method;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSender;
import lombok.extern.log4j.Log4j2;
import ru.at.rest.api.cucumber.CoreScenario;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.internal.print.RequestPrinter.print;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.fail;

@Log4j2
public class Utils {

    /**
     * Возвращает текущую дату/время в виде строки
     *
     * @param format    формат для даты/времени
     * @return          текущая дата/время в заданном формате в виде строки
     */
    public static String getCurrentDateTimeAsString(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    /**
     * Производит считывание файла по указанному пути
     * и возвращает считанный файл в виде строки с заданной кодировкой
     *
     * @param path      путь к файлу для считывания
     * @param encoding  кодировка
     * @return          считанный файл в виде строки с заданной кодировкой
     */
    public static String readFile(String path, Charset encoding) {
        Path file = Paths.get(path);
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, encoding);
    }

    /**
     * Возвращает запрос Request в виде строки
     *
     * @param request   объект класса RequestSender для вывода в виде строки
     * @param method    метод запроса
     * @param address   адрес запроса
     * @return          запрос Request в виде строки
     */
    public static String requestSpecToString(RequestSender request, Method method, String address) {
        return print((FilterableRequestSpecification) request,
                method.toString(),
                address,
                LogDetail.ALL,
                emptySet(),
                new NullPrintStream(),
                false);
    }

    /**
     * Формирует объект класса DataTable на основе данных из файла
     *
     * @param file          файл для формирования DataTable
     * @return              объект класса DataTable
     */
    public static DataTable getDataTableFromFile(File file) {
        List<List<String>> content = new ArrayList<>();
        for(String line:ResourceLoader.getInstance().getFileAsList(file)) {
            content.add(Arrays.stream(line.split("\\|"))
                    .map(s -> s = s.trim())
                    .collect(Collectors.toList())
            );
        }
        return DataTable.create(content);
    }

    /**
     * Выводит в лог сообщение об ошибке и прикрепляет сообщение к шагу в отчете allure
     *
     * @param message           сообщение об ошибке
     */
    public static void attachErrorMessage(String message) {
        log.error(message);
        CoreScenario.getInstance().getScenario().attach(message, "text/plain", "error message");
        fail(message);
    }

}
