package com.microservices.inventario.service;

import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.model.jsonapi.JsonApiResponse;
import com.microservices.inventario.model.jsonapi.JsonApiData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoClientService {

    private final RestTemplate restTemplate;

    @Value("${productos.service.url}")
    private String productosServiceUrl;

    @Retryable(
            retryFor = {ResourceAccessException.class, ProductoServiceException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public ProductoDTO obtenerProducto(Long productoId) {
        String url = productosServiceUrl + "/api/productos/" + productoId;
        log.info("Consultando producto ID: {} en URL: {}", productoId, url);

        try {
            JsonApiResponse response = restTemplate.getForObject(url, JsonApiResponse.class);

            if (response != null && response.getData() != null) {
                JsonApiData data = (JsonApiData) response.getData();
                ProductoDTO producto = convertToProductoDTO(data);
                log.info("Producto obtenido exitosamente: {}", producto);
                return producto;
            }

            throw new ProductoServiceException("Respuesta vacía del servicio de productos");

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Producto no encontrado con ID: {}", productoId);
            throw new ProductoServiceException("Producto no encontrado: " + productoId);
        } catch (ResourceAccessException e) {
            log.error("Timeout o error de conexión al servicio de productos: {}", e.getMessage());
            throw new ProductoServiceException("Error de comunicación con servicio de productos", e);
        } catch (Exception e) {
            log.error("Error al obtener producto: {}", e.getMessage(), e);
            throw new ProductoServiceException("Error al comunicarse con servicio de productos", e);
        }
    }

    private ProductoDTO convertToProductoDTO(JsonApiData data) {
        try {
            Object attributes = data.getAttributes();

            if (attributes instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) attributes;
                ProductoDTO producto = new ProductoDTO();
                producto.setId(Long.parseLong(data.getId()));
                producto.setNombre((String) map.get("nombre"));
                producto.setPrecio(new BigDecimal(map.get("precio").toString()));
                return producto;
            }

            throw new ProductoServiceException("Error al convertir datos del producto");
        } catch (Exception e) {
            log.error("Error en conversión: {}", e.getMessage());
            throw new ProductoServiceException("Error al convertir datos del producto", e);
        }
    }
}
