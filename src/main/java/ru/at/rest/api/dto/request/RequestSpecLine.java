package ru.at.rest.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
public class RequestSpecLine {
    private RequestSpecBuilder.RequestPart requestPart;
    private String key;
    private String value;

    @Override
    public String toString() {
        return "requestPart=" + requestPart +
               ", key='" + key + '\'' +
               ", value='" + value + '\'';
    }

    public List<String> toList() {
        return Arrays.asList(requestPart.toString(), key, value);
    }
}
