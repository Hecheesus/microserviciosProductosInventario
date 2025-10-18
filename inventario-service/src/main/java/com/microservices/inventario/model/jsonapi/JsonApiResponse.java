package com.microservices.inventario.model.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonApiResponse<T> {

    private T data;
    private List<JsonApiError> errors;
    private Map<String, Object> meta;
    private Map<String, String> links;

    @Builder.Default
    private JsonApiInfo jsonapi = new JsonApiInfo("1.0");

    @Data
    @AllArgsConstructor
    public static class JsonApiInfo {
        private String version;
    }
}
