package com.microservices.inventario.service;

import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.model.jsonapi.JsonApiData;
import com.microservices.inventario.model.jsonapi.JsonApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ProductoClientService")
class ProductoClientServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductoClientService productoClientService;

    private static final String PRODUCTOS_SERVICE_URL = "http://localhost:8081";
    private static final Long PRODUCTO_ID = 1L;
    private static final String PRODUCTO_NOMBRE = "Laptop HP";
    private static final BigDecimal PRODUCTO_PRECIO = new BigDecimal("1500.00");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productoClientService, "productosServiceUrl", PRODUCTOS_SERVICE_URL);
    }

    @Test
    @DisplayName("Debe obtener producto exitosamente")
    void obtenerProducto_DebeRetornarProductoDTO() {
        // Arrange
        JsonApiResponse response = crearJsonApiResponse();
        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;

        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act
        ProductoDTO resultado = productoClientService.obtenerProducto(PRODUCTO_ID);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(PRODUCTO_ID);
        assertThat(resultado.getNombre()).isEqualTo(PRODUCTO_NOMBRE);
        assertThat(resultado.getPrecio()).isEqualByComparingTo(PRODUCTO_PRECIO);

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando respuesta es null")
    void obtenerProducto_RespuestaNull_DebeLanzarExcepcion() {
        // Arrange
        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .cause()
                .isInstanceOf(ProductoServiceException.class)
                .hasMessage("Respuesta vacía del servicio de productos");

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando data es null")
    void obtenerProducto_DataNull_DebeLanzarExcepcion() {
        // Arrange
        JsonApiResponse response = new JsonApiResponse();
        response.setData(null);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .cause()
                .isInstanceOf(ProductoServiceException.class)
                .hasMessage("Respuesta vacía del servicio de productos");

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe (404)")
    void obtenerProducto_NotFound_DebeLanzarExcepcion() {
        // Arrange
        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        org.springframework.http.HttpHeaders.EMPTY,
                        null,
                        null));

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Producto no encontrado")
                .hasMessageContaining(PRODUCTO_ID.toString())
                .hasNoCause();

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hay error de conexión")
    void obtenerProducto_ResourceAccessException_DebeLanzarExcepcion() {
        // Arrange
        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error de comunicación con servicio de productos")
                .hasCauseInstanceOf(ResourceAccessException.class);

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando hay error genérico")
    void obtenerProducto_ErrorGenerico_DebeLanzarExcepcion() {
        // Arrange
        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenThrow(new RuntimeException("Error inesperado"));

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .hasCauseInstanceOf(RuntimeException.class);

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando attributes no es LinkedHashMap")
    void obtenerProducto_AttributesInvalido_DebeLanzarExcepcion() {
        // Arrange
        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes("String invalido")
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .cause()
                .isInstanceOf(ProductoServiceException.class)
                .hasMessage("Error al convertir datos del producto");

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando falla parseo de precio")
    void obtenerProducto_ErrorParseoPrecio_DebeLanzarExcepcion() {
        // Arrange
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", PRODUCTO_NOMBRE);
        attributes.put("precio", "precio_invalido");

        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes(attributes)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .hasRootCauseInstanceOf(NumberFormatException.class);

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando falla parseo de ID")
    void obtenerProducto_ErrorParseoId_DebeLanzarExcepcion() {
        // Arrange
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", PRODUCTO_NOMBRE);
        attributes.put("precio", PRODUCTO_PRECIO);

        JsonApiData data = JsonApiData.builder()
                .id("id_invalido")
                .type("productos")
                .attributes(attributes)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .hasRootCauseInstanceOf(NumberFormatException.class)
                .hasRootCauseMessage("For input string: \"id_invalido\"");

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe manejar precio con decimales")
    void obtenerProducto_PrecioConDecimales_DebeConvertirCorrectamente() {
        // Arrange
        BigDecimal precioConDecimales = new BigDecimal("1599.99");

        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", PRODUCTO_NOMBRE);
        attributes.put("precio", precioConDecimales);

        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes(attributes)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act
        ProductoDTO resultado = productoClientService.obtenerProducto(PRODUCTO_ID);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPrecio()).isEqualByComparingTo(precioConDecimales);

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe manejar precio como Integer en el JSON")
    void obtenerProducto_PrecioComoInteger_DebeConvertirCorrectamente() {
        // Arrange
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", PRODUCTO_NOMBRE);
        attributes.put("precio", 1500);

        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes(attributes)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act
        ProductoDTO resultado = productoClientService.obtenerProducto(PRODUCTO_ID);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPrecio()).isEqualByComparingTo(new BigDecimal("1500"));

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    @Test
    @DisplayName("Debe manejar attributes null")
    void obtenerProducto_AttributesNull_DebeLanzarExcepcion() {
        // Arrange
        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes(null)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        String expectedUrl = PRODUCTOS_SERVICE_URL + "/api/productos/" + PRODUCTO_ID;
        when(restTemplate.getForObject(expectedUrl, JsonApiResponse.class))
                .thenReturn(response);

        // Act & Assert
        assertThatThrownBy(() -> productoClientService.obtenerProducto(PRODUCTO_ID))
                .isInstanceOf(ProductoServiceException.class)
                .hasMessageContaining("Error al comunicarse con servicio de productos")
                .cause()
                .isInstanceOf(ProductoServiceException.class)
                .hasMessage("Error al convertir datos del producto");

        verify(restTemplate, times(1)).getForObject(expectedUrl, JsonApiResponse.class);
    }

    private JsonApiResponse crearJsonApiResponse() {
        LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("nombre", PRODUCTO_NOMBRE);
        attributes.put("precio", PRODUCTO_PRECIO);

        JsonApiData data = JsonApiData.builder()
                .id(PRODUCTO_ID.toString())
                .type("productos")
                .attributes(attributes)
                .build();

        JsonApiResponse response = new JsonApiResponse();
        response.setData(data);

        return response;
    }
}
