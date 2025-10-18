package com.microservices.inventario.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioConProductoDTO {
    private Long productoId;
    private Integer cantidad;
    private ProductoDTO producto;
}
