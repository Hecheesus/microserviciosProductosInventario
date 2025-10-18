package com.microservices.productos.controller;

import com.microservices.productos.model.Producto;
import com.microservices.productos.model.jsonapi.JsonApiData;
import com.microservices.productos.model.jsonapi.JsonApiResponse;
import com.microservices.productos.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para gestión de productos")
@SecurityRequirement(name = "apiKey")
public class ProductoController {

    @Autowired
    private  ProductoService productoService;
    private static final String BASE_URL = "http://localhost:8081/api/productos";

    @PostMapping
    @Operation(summary = "Crear un nuevo producto")
    public ResponseEntity<JsonApiResponse<JsonApiData<Producto>>> crearProducto(
            @Valid @RequestBody JsonApiData<Producto> requestData) {

        Producto producto = productoService.crearProducto(requestData.getAttributes());

        JsonApiData<Producto> data = JsonApiData.<Producto>builder()
                .type("productos")
                .id(producto.getId().toString())
                .attributes(producto)
                .links(Map.of("self", BASE_URL + "/" + producto.getId()))
                .build();

        JsonApiResponse<JsonApiData<Producto>> response = JsonApiResponse.<JsonApiData<Producto>>builder()
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID")
    public ResponseEntity<JsonApiResponse<JsonApiData<Producto>>> obtenerProducto(
            @PathVariable Long id) {

        Producto producto = productoService.obtenerProductoPorId(id);

        JsonApiData<Producto> data = JsonApiData.<Producto>builder()
                .type("productos")
                .id(producto.getId().toString())
                .attributes(producto)
                .links(Map.of("self", BASE_URL + "/" + producto.getId()))
                .build();

        JsonApiResponse<JsonApiData<Producto>> response = JsonApiResponse.<JsonApiData<Producto>>builder()
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos con paginación")
    public ResponseEntity<JsonApiResponse<List<JsonApiData<Producto>>>> listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosPage = productoService.listarProductos(pageable);

        List<JsonApiData<Producto>> dataList = productosPage.getContent().stream()
                .map(producto -> JsonApiData.<Producto>builder()
                        .type("productos")
                        .id(producto.getId().toString())
                        .attributes(producto)
                        .links(Map.of("self", BASE_URL + "/" + producto.getId()))
                        .build())
                .collect(Collectors.toList());

        Map<String, String> links = new HashMap<>();
        links.put("self", BASE_URL + "?page=" + page + "&size=" + size);
        links.put("first", BASE_URL + "?page=0&size=" + size);
        links.put("last", BASE_URL + "?page=" + (productosPage.getTotalPages() - 1) + "&size=" + size);

        if (productosPage.hasNext()) {
            links.put("next", BASE_URL + "?page=" + (page + 1) + "&size=" + size);
        }
        if (productosPage.hasPrevious()) {
            links.put("prev", BASE_URL + "?page=" + (page - 1) + "&size=" + size);
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("totalPages", productosPage.getTotalPages());
        meta.put("totalElements", productosPage.getTotalElements());
        meta.put("currentPage", page);
        meta.put("pageSize", size);

        JsonApiResponse<List<JsonApiData<Producto>>> response = JsonApiResponse.<List<JsonApiData<Producto>>>builder()
                .data(dataList)
                .links(links)
                .meta(meta)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto")
    public ResponseEntity<JsonApiResponse<JsonApiData<Producto>>> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody JsonApiData<Producto> requestData) {

        Producto producto = productoService.actualizarProducto(id, requestData.getAttributes());

        JsonApiData<Producto> data = JsonApiData.<Producto>builder()
                .type("productos")
                .id(producto.getId().toString())
                .attributes(producto)
                .links(Map.of("self", BASE_URL + "/" + producto.getId()))
                .build();

        JsonApiResponse<JsonApiData<Producto>> response = JsonApiResponse.<JsonApiData<Producto>>builder()
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
