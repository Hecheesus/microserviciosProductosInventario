package com.microservices.productos.model.jsonapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonApiError {

    private String status;
    private String title;
    private String detail;
}
