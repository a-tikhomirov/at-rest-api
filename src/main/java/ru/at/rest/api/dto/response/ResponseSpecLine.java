package ru.at.rest.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class ResponseSpecLine {
    private ResponseSpecBuilder.ResponsePart responsePart;
    private String key;
    private ResponseSpecBuilder.OperationType operation;
    private ResponseSpecBuilder.ValueType type;
    private String expectedValue;

    @Override
    public String toString() {
        return "responsePart: " + responsePart +
                ", key: '" + key +
                "', operation: '" + operation +
                "', type: " + type +
                ", expectedValue: '" + expectedValue +
                '\'';
    }

    public List<String> toList() {
        return Arrays.asList(responsePart.toString(), key, operation.toString(), type.toString(), expectedValue);
    }
}
