package ru.at.rest.api.utils;

import io.restassured.http.Method;
import io.restassured.specification.RequestSender;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Вспомогательный класс. Используется для вывода запроса Request в виде строки без печати в консоль
 * См. {@link Utils#requestSpecToString(RequestSender, Method, String) requestSpecToString}
 */
public class NullPrintStream extends PrintStream {

    public NullPrintStream() {
        super(new NullByteArrayOutputStream());
    }

    private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

        @Override
        public void write(int b) {}

        @Override
        public void write(byte[] b, int off, int len) {}

        @Override
        public void writeTo(OutputStream out) {}

    }

}
