package ru.at.rest.api.utils;

import com.google.common.io.ByteStreams;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Method;
import io.restassured.specification.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import static io.restassured.internal.print.RequestPrinter.print;
import static java.util.Collections.emptySet;

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
     * Возвращает объект класса File из папки resources
     *
     * @param fileName  имя файла в папке resources
     * @return          объект класса File
     */
    public File getFileFromResources(String fileName) {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("Файл не найден:" + fileName);
        }
        File file = null;
        try {
            file = Paths.get(resource.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Производит считывание файла из папки resources
     * и возвращет считанный файл в виде массива байт
     *
     * @param fileName  имя файла в папке resources
     * @return          считанный файл в виде массива байт
     */
    public byte[] getFileFromResourcesAsByteArray(String fileName){
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        byte[] result = null;

        if (inputStream == null) {
            throw new IllegalArgumentException("Файл не найден: " + fileName);
        } else {
            try {
                result = ByteStreams.toByteArray(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Производит считывание файла из папки resources
     * и возвращет считанный файл в виде Base64 строки
     *
     * @param fileName  имя файла в папке resources
     * @return          считанный файл в виде Base64 строки
     */
    public String getFileFromResourcesAsBase64String(String fileName){
        return Base64.getEncoder().encodeToString(this.getFileFromResourcesAsByteArray(fileName));
    }

}
