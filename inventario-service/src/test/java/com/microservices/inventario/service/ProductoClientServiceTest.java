package com.microservices.inventario.service;

import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.model.jsonapi.JsonApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductoClientServiceTest {

    private RestTemplate restTemplate;
    private ProductoClientService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        service = new ProductoClientService(restTemplate);
        // Inyectar valor de propiedad simulada
        // usando reflexión simple dado que el campo es @Value
        try {
            var f = ProductoClientService.class.getDeclaredField("productosServiceUrl");
            f.setAccessible(true);
            f.set(service, "http://productos");
        } catch (Exception e) {
            fail("No se pudo setear productosServiceUrl");
        }
    }

    private ResponseEntity<JsonApiResponse> okResponse(String id, String nombre, Object precio) {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", nombre);
        attributes.put("precio", precio);

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("attributes", attributes);

        JsonApiResponse body = new JsonApiResponse();
        body.setData(data);

        return ResponseEntity.ok(body);
    }

    @Test
    void obtenerProducto_ok_devuelveDTO() {
        Long id = 10L;
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(okResponse(String.valueOf(id), "Teclado", "123.45"));

        ProductoDTO dto = service.obtenerProducto(id);

        assertEquals(id, dto.getId());
        assertEquals("Teclado", dto.getNombre());
        assertEquals(new BigDecimal("123.45"), dto.getPrecio());
        // Verificar URL y método
        ArgumentCaptor<String> urlCap = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCap.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
        assertTrue(urlCap.getValue().endsWith("/api/productos/10"));
    }

    @Test
    void obtenerProducto_404_lanzaProductoServiceException() {
        Long id = 99L;
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(HttpClientErrorException.NotFound.create( HttpStatus.NOT_FOUND, null, null, null, null));

        ProductoServiceException ex = assertThrows(ProductoServiceException.class, () -> service.obtenerProducto(id));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void obtenerProducto_respuesta_vacia_lanzaProductoServiceException() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(null));

        assertThrows(ProductoServiceException.class, () -> service.obtenerProducto(1L));
    }

    @Test
    void obtenerProducto_formato_inesperado_sin_attributes_lanzaProductoServiceException() {
        // data sin attributes
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("id", "1");
        JsonApiResponse body = new JsonApiResponse();
        body.setData(data);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(body));

        assertThrows(ProductoServiceException.class, () -> service.obtenerProducto(1L));
    }
}
