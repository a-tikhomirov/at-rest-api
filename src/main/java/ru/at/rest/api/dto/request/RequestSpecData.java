package ru.at.rest.api.dto.request;

import io.cucumber.datatable.DataTable;
import io.cucumber.plugin.event.DataTableArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс для формирования заготовк под RequestSpecification
 * Хранит в себе список объектов класса {@link RequestSpecLine}
 * Используется для формирования RequestSpecification из Gherkin DataTable
 * и для отображения данных заготовки в отчете allure и в логе
 */
public class RequestSpecData implements DataTableArgument {
    private final List<RequestSpecLine> requestSpecLineList = new ArrayList<>();

    public void add(RequestSpecLine requestSpecLine) {
        requestSpecLineList.add(requestSpecLine);
    }

    public List<RequestSpecLine> getList(){ return Collections.unmodifiableList(this.requestSpecLineList); }

    public DataTable toDataTable() {
        return DataTable.create(this.cells());
    }

    @Override
    public List<List<String>> cells() {
        List<List<String>> requestSpecDataAsLists = new ArrayList<>();
        requestSpecLineList.forEach(line -> requestSpecDataAsLists.add(line.toList()));
        return requestSpecDataAsLists;
    }

    @Override
    public int getLine() {
        return 0;
    }
}
