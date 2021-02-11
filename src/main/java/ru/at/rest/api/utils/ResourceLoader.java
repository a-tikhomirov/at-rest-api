package ru.at.rest.api.utils;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class ResourceLoader {

    private static ResourceLoader instance;

    public static ResourceLoader getInstance() {
        if (instance == null) {
            instance = new ResourceLoader();
        }
        return instance;
    }

    /**
     * Возвращает объект класса File из папки resources
     *
     * @param fileName  имя файла в папке resources
     * @return          объект класса File
     */
    public synchronized File getFileFromResources(String fileName) {
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
    public synchronized byte[] getFileFromResourcesAsByteArray(String fileName){
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
    public synchronized String getFileFromResourcesAsBase64String(String fileName){
        return Base64.getEncoder().encodeToString(this.getFileFromResourcesAsByteArray(fileName));
    }

    /**
     * Производит считывание файла из папки resources
     * возвращет считанный файл в списка строк
     *
     * @param file      файл для чтения
     * @return          считанный файл в виде списка строк
     */
    public synchronized List<String> getFileAsList(String path, String file) {
        List<String> lines = new ArrayList<>();
        try (InputStreamReader streamReader =
                     new InputStreamReader(getFileFromResourceAsStream(path + "/" + file), StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private InputStream getFileFromResourceAsStream(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    /**
     * Возвращает список файлов по указанному пути в папке resources
     *
     * @param path      путь в парке resources
     * @return          список файлов по указанному пути в виде массива объектов класса File
     */
    public synchronized List<String> getResourceFolderFiles (String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> fileNames = new ArrayList<>();
        try {
            fileNames = IOUtils.readLines(Objects.requireNonNull(classLoader.getResourceAsStream(path)), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

}
