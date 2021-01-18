package ru.at.rest.api.dto.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.plugin.event.DataTableArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс для формирования заготовк под ResponseSpecification
 * Хранит в себе список объектов класса {@link ResponseSpecLine}
 * Используется для формирования ResponseSpecification из Gherkin DataTable
 * и для отображения данных заготовки в отчете allure и в логе
 */
public class ResponseSpecData implements DataTableArgument {
    private final List<ResponseSpecLine> responseSpecLineList = new ArrayList<>();

    public void add(ResponseSpecLine responseSpecLine) {
        responseSpecLineList.add(responseSpecLine);
    }

    public List<ResponseSpecLine> getList(){
        return Collections.unmodifiableList(this.responseSpecLineList);
    }

    public DataTable toDataTable() {
        return DataTable.create(this.cells());
    }

    @Override
    public List<List<String>> cells() {
        List<List<String>> responseSpecDataAsLists = new ArrayList<>();
        responseSpecLineList.forEach(line -> responseSpecDataAsLists.add(line.toList()));
        return responseSpecDataAsLists;
    }

    @Override
    public int getLine() {
        return 0;
    }
}
