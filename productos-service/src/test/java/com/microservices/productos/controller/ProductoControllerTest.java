package com.microservices.productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.productos.controller.ProductoController;
import com.microservices.productos.exception.ProductoNotFoundException;
import com.microservices.productos.model.Producto;
import com.microservices.productos.model.jsonapi.JsonApiData;
import com.microservices.productos.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests para ProductoController")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    private Producto producto;
    private JsonApiData<Producto> jsonApiData;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop HP");
        producto.setPrecio(new BigDecimal("1500.00"));

        jsonApiData = JsonApiData.<Producto>builder()
                .type("productos")
                .attributes(producto)
                .build();
    }

    @Test
    @DisplayName("POST /api/productos - Debe crear un producto exitosamente")
    void crearProducto_DebeRetornar201YProductoCreado() throws Exception {
        // Arrange
        when(productoService.crearProducto(any(Producto.class))).thenReturn(producto);

        // Act & Assert
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jsonApiData)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("productos"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop HP"))
                .andExpect(jsonPath("$.data.attributes.precio").value(1500.00))
                .andExpect(jsonPath("$.data.links.self").value("http://localhost:8081/api/productos/1"));

        verify(productoService, times(1)).crearProducto(any(Producto.class));
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe obtener un producto por ID")
    void obtenerProducto_CuandoExiste_DebeRetornar200YProducto() throws Exception {
        // Arrange
        when(productoService.obtenerProductoPorId(1L)).thenReturn(producto);

        // Act & Assert
        mockMvc.perform(get("/api/productos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("productos"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop HP"))
                .andExpect(jsonPath("$.data.attributes.precio").value(1500.00));

        verify(productoService, times(1)).obtenerProductoPorId(1L);
    }

    @Test
    @DisplayName("GET /api/productos/{id} - Debe retornar 404 cuando el producto no existe")
    void obtenerProducto_CuandoNoExiste_DebeRetornar404() throws Exception {
        // Arrange
        when(productoService.obtenerProductoPorId(999L))
                .thenThrow(new ProductoNotFoundException("Producto no encontrado con ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/productos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).obtenerProductoPorId(999L);
    }

    @Test
    @DisplayName("GET /api/productos - Debe listar productos con paginación")
    void listarProductos_DebeRetornar200YListaDePaginada() throws Exception {
        // Arrange
        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Mouse Logitech");
        producto2.setPrecio(new BigDecimal("50.00"));

        List<Producto> productos = Arrays.asList(producto, producto2);
        Page<Producto> page = new PageImpl<>(productos, PageRequest.of(0, 10), 2);

        when(productoService.listarProductos(any(PageRequest.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/productos")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].type").value("productos"))
                .andExpect(jsonPath("$.data[0].attributes.nombre").value("Laptop HP"))
                .andExpect(jsonPath("$.data[1].attributes.nombre").value("Mouse Logitech"))
                .andExpect(jsonPath("$.meta.totalPages").value(1))
                .andExpect(jsonPath("$.meta.totalElements").value(2))
                .andExpect(jsonPath("$.meta.currentPage").value(0))
                .andExpect(jsonPath("$.links.self").exists())
                .andExpect(jsonPath("$.links.first").exists())
                .andExpect(jsonPath("$.links.last").exists());

        verify(productoService, times(1)).listarProductos(any(PageRequest.class));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - Debe actualizar un producto existente")
    void actualizarProducto_CuandoExiste_DebeRetornar200YProductoActualizado() throws Exception {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setId(1L);
        productoActualizado.setNombre("Laptop Dell Actualizada");
        productoActualizado.setPrecio(new BigDecimal("2000.00"));

        when(productoService.actualizarProducto(eq(1L), any(Producto.class))).thenReturn(productoActualizado);

        JsonApiData<Producto> requestData = JsonApiData.<Producto>builder()
                .type("productos")
                .attributes(productoActualizado)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/productos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.type").value("productos"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop Dell Actualizada"))
                .andExpect(jsonPath("$.data.attributes.precio").value(2000.00));

        verify(productoService, times(1)).actualizarProducto(eq(1L), any(Producto.class));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Debe eliminar un producto existente")
    void eliminarProducto_CuandoExiste_DebeRetornar204() throws Exception {
        // Arrange
        doNothing().when(productoService).eliminarProducto(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminarProducto(1L);
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - Debe retornar 404 cuando el producto no existe")
    void eliminarProducto_CuandoNoExiste_DebeRetornar404() throws Exception {
        // Arrange
        doThrow(new ProductoNotFoundException("Producto no encontrado con ID: 999"))
                .when(productoService).eliminarProducto(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/productos/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).eliminarProducto(999L);
    }
}
