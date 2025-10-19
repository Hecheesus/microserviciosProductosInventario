package com.microservices.inventario.model.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)  // ✅ Solo serializa campos no-null
public class JsonApiResponse<T> {
    private T data;
    private List<JsonApiError> errors;
    private Object meta;
    private Object links;

    // ✅ Opcional pero recomendado para cumplir JSON:API completo
    @JsonProperty("jsonapi")
    @Builder.Default
    private Map<String, String> jsonapi = Map.of("version", "1.0");
}
