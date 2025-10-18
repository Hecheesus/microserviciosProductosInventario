package com.microservices.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventario.exception.InventarioNotFoundException;
import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.Inventario;
import com.microservices.inventario.model.InventarioConProductoDTO;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.service.InventarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para InventarioController")
class InventarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventarioService inventarioService;

    private ObjectMapper objectMapper;
    private InventarioConProductoDTO inventarioConProducto;
    private Inventario inventario;
    private ProductoDTO productoDTO;
    private static final Long PRODUCTO_ID = 1L;
    private static final Integer CANTIDAD = 100;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Configurar MockMvc con standalone setup y exception handler
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InventarioController(inventarioService))
                .setControllerAdvice(new TestExceptionHandler())
                .build();

        // Crear ProductoDTO
        productoDTO = new ProductoDTO();
        productoDTO.setId(PRODUCTO_ID);
        productoDTO.setNombre("Laptop HP");
        productoDTO.setPrecio(new BigDecimal("1500.00"));

        // Crear Inventario
        inventario = new Inventario(1L, PRODUCTO_ID, CANTIDAD);

        // Crear InventarioConProductoDTO
        inventarioConProducto = new InventarioConProductoDTO();
        inventarioConProducto.setProductoId(PRODUCTO_ID);
        inventarioConProducto.setCantidad(CANTIDAD);
        inventarioConProducto.setProducto(productoDTO);
    }

    // Clase interna para manejo de excepciones en tests
    @ControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(InventarioNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ResponseEntity<Map<String, Object>> handleInventarioNotFoundException(
                InventarioNotFoundException ex, WebRequest request) {

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Not Found");
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ProductoServiceException.class)
        @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
        public ResponseEntity<Map<String, Object>> handleProductoServiceException(
                ProductoServiceException ex, WebRequest request) {

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            body.put("error", "Service Unavailable");
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
                IllegalArgumentException ex, WebRequest request) {

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Bad Request");
            body.put("message", ex.getMessage());
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ResponseEntity<Map<String, Object>> handleGlobalException(
                Exception ex, WebRequest request) {

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("error", "Internal Server Error");
            body.put("message", "Ha ocurrido un error inesperado");
            body.put("path", request.getDescription(false).replace("uri=", ""));

            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Test
    @DisplayName("GET /api/inventarios/{productoId} - Debe consultar inventario exitosamente")
    void consultarInventario_DebeRetornar200YInventarioConProducto() throws Exception {
        // Arrange
        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);

        // Act & Assert
        mockMvc.perform(get("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID))
                .andExpect(jsonPath("$.cantidad").value(CANTIDAD))
                .andExpect(jsonPath("$.producto.nombre").value("Laptop HP"))
                .andExpect(jsonPath("$.producto.precio").value(1500.00));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
    }

    @Test
    @DisplayName("GET /api/inventarios/{productoId} - Debe retornar 404 cuando no existe")
    void consultarInventario_NoExiste_DebeRetornar404() throws Exception {
        // Arrange
        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenThrow(new InventarioNotFoundException("Inventario no encontrado para producto ID: " + PRODUCTO_ID));

        // Act & Assert
        mockMvc.perform(get("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
    }

    @Test
    @DisplayName("PUT /api/inventarios/{productoId} - Debe actualizar inventario exitosamente")
    void actualizarInventario_DebeRetornar200YInventarioActualizado() throws Exception {
        // Arrange
        Integer nuevaCantidad = 150;
        inventarioConProducto.setCantidad(nuevaCantidad);

        Map<String, Integer> request = new HashMap<>();
        request.put("cantidad", nuevaCantidad);

        when(inventarioService.actualizarCantidad(PRODUCTO_ID, nuevaCantidad))
                .thenReturn(inventarioConProducto);

        // Act & Assert
        mockMvc.perform(put("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID))
                .andExpect(jsonPath("$.cantidad").value(nuevaCantidad))
                .andExpect(jsonPath("$.producto.nombre").value("Laptop HP"));

        verify(inventarioService, times(1)).actualizarCantidad(PRODUCTO_ID, nuevaCantidad);
    }

    @Test
    @DisplayName("PUT /api/inventarios/{productoId} - Debe rechazar cantidad negativa")
    void actualizarInventario_CantidadNegativa_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, Integer> request = new HashMap<>();
        request.put("cantidad", -10);

        // Act & Assert
        mockMvc.perform(put("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(inventarioService, never()).actualizarCantidad(any(), any());
    }

    @Test
    @DisplayName("PUT /api/inventarios/{productoId} - Debe rechazar cantidad null")
    void actualizarInventario_CantidadNull_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, Integer> request = new HashMap<>();
        request.put("cantidad", null);

        // Act & Assert
        mockMvc.perform(put("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).actualizarCantidad(any(), any());
    }

    @Test
    @DisplayName("PUT /api/inventarios/{productoId} - Debe rechazar body sin campo cantidad")
    void actualizarInventario_SinCampoCantidad_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, Integer> request = new HashMap<>();

        // Act & Assert
        mockMvc.perform(put("/api/inventarios/{productoId}", PRODUCTO_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).actualizarCantidad(any(), any());
    }

    @Test
    @DisplayName("POST /api/inventarios - Debe crear inventario exitosamente")
    void crearInventario_DebeRetornar201YInventarioCreado() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", PRODUCTO_ID);
        request.put("cantidadInicial", CANTIDAD);

        when(inventarioService.crearInventario(PRODUCTO_ID, CANTIDAD))
                .thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID))
                .andExpect(jsonPath("$.cantidad").value(CANTIDAD));

        verify(inventarioService, times(1)).crearInventario(PRODUCTO_ID, CANTIDAD);
    }

    @Test
    @DisplayName("POST /api/inventarios - Debe rechazar cantidad inicial negativa")
    void crearInventario_CantidadNegativa_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", PRODUCTO_ID);
        request.put("cantidadInicial", -10);

        // Act & Assert
        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(), any());
    }

    @Test
    @DisplayName("POST /api/inventarios - Debe rechazar campos faltantes")
    void crearInventario_CamposFaltantes_DebeRetornar400() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", PRODUCTO_ID);

        // Act & Assert
        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(inventarioService, never()).crearInventario(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/inventarios/{productoId}/decrementar - Debe decrementar stock exitosamente")
    void decrementarStock_DebeRetornar200() throws Exception {
        // Arrange
        Integer cantidadDecrementar = 10;
        Integer nuevaCantidad = CANTIDAD - cantidadDecrementar;

        InventarioConProductoDTO inventarioActualizado = new InventarioConProductoDTO();
        inventarioActualizado.setProductoId(PRODUCTO_ID);
        inventarioActualizado.setCantidad(nuevaCantidad);
        inventarioActualizado.setProducto(productoDTO);

        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);
        when(inventarioService.actualizarCantidad(PRODUCTO_ID, nuevaCantidad))
                .thenReturn(inventarioActualizado);

        // Act & Assert
        mockMvc.perform(patch("/api/inventarios/{productoId}/decrementar", PRODUCTO_ID)
                        .param("cantidad", String.valueOf(cantidadDecrementar))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(nuevaCantidad));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
        verify(inventarioService, times(1)).actualizarCantidad(PRODUCTO_ID, nuevaCantidad);
    }

    @Test
    @DisplayName("PATCH /api/inventarios/{productoId}/decrementar - Debe rechazar stock insuficiente")
    void decrementarStock_StockInsuficiente_DebeRetornar400() throws Exception {
        // Arrange
        Integer cantidadDecrementar = 150;

        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);

        // Act & Assert
        mockMvc.perform(patch("/api/inventarios/{productoId}/decrementar", PRODUCTO_ID)
                        .param("cantidad", String.valueOf(cantidadDecrementar))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
        verify(inventarioService, never()).actualizarCantidad(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/inventarios/{productoId}/incrementar - Debe incrementar stock exitosamente")
    void incrementarStock_DebeRetornar200() throws Exception {
        // Arrange
        Integer cantidadIncrementar = 50;
        Integer nuevaCantidad = CANTIDAD + cantidadIncrementar;

        InventarioConProductoDTO inventarioActualizado = new InventarioConProductoDTO();
        inventarioActualizado.setProductoId(PRODUCTO_ID);
        inventarioActualizado.setCantidad(nuevaCantidad);
        inventarioActualizado.setProducto(productoDTO);

        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);
        when(inventarioService.actualizarCantidad(PRODUCTO_ID, nuevaCantidad))
                .thenReturn(inventarioActualizado);

        // Act & Assert
        mockMvc.perform(patch("/api/inventarios/{productoId}/incrementar", PRODUCTO_ID)
                        .param("cantidad", String.valueOf(cantidadIncrementar))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(nuevaCantidad));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
        verify(inventarioService, times(1)).actualizarCantidad(PRODUCTO_ID, nuevaCantidad);
    }

    @Test
    @DisplayName("GET /api/inventarios/{productoId}/disponibilidad - Debe verificar disponibilidad con stock suficiente")
    void verificarDisponibilidad_StockSuficiente_DebeRetornarDisponibleTrue() throws Exception {
        // Arrange
        Integer cantidadRequerida = 50;

        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);

        // Act & Assert
        mockMvc.perform(get("/api/inventarios/{productoId}/disponibilidad", PRODUCTO_ID)
                        .param("cantidadRequerida", String.valueOf(cantidadRequerida))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID))
                .andExpect(jsonPath("$.cantidadRequerida").value(cantidadRequerida))
                .andExpect(jsonPath("$.cantidadDisponible").value(CANTIDAD))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.productoNombre").value("Laptop HP"));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
    }

    @Test
    @DisplayName("GET /api/inventarios/{productoId}/disponibilidad - Debe verificar disponibilidad con stock insuficiente")
    void verificarDisponibilidad_StockInsuficiente_DebeRetornarDisponibleFalse() throws Exception {
        // Arrange
        Integer cantidadRequerida = 150;

        when(inventarioService.consultarInventario(PRODUCTO_ID))
                .thenReturn(inventarioConProducto);

        // Act & Assert
        mockMvc.perform(get("/api/inventarios/{productoId}/disponibilidad", PRODUCTO_ID)
                        .param("cantidadRequerida", String.valueOf(cantidadRequerida))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID))
                .andExpect(jsonPath("$.cantidadRequerida").value(cantidadRequerida))
                .andExpect(jsonPath("$.cantidadDisponible").value(CANTIDAD))
                .andExpect(jsonPath("$.disponible").value(false))
                .andExpect(jsonPath("$.productoNombre").value("Laptop HP"));

        verify(inventarioService, times(1)).consultarInventario(PRODUCTO_ID);
    }

    @Test
    @DisplayName("POST /api/inventarios - Debe manejar productoId como Integer")
    void crearInventario_ProductoIdComoInteger_DebeCrear() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("productoId", 1);
        request.put("cantidadInicial", CANTIDAD);

        when(inventarioService.crearInventario(1L, CANTIDAD))
                .thenReturn(inventario);

        // Act & Assert
        mockMvc.perform(post("/api/inventarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productoId").value(PRODUCTO_ID));

        verify(inventarioService, times(1)).crearInventario(1L, CANTIDAD);
    }
}
