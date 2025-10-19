package com.microservices.inventario.service;

import com.microservices.inventario.exception.ProductoServiceException;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.model.jsonapi.JsonApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
            // ✅ Usar exchange() con ParameterizedTypeReference para manejar genéricos correctamente
            ResponseEntity<JsonApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<JsonApiResponse>() {}
            );
            if (response == null || response.getBody() == null) {
                throw new ProductoServiceException("Respuesta vacía del servicio de productos");
            }

            if (response.getBody() != null && response.getBody().getData() != null) {
                Object data = response.getBody().getData();

                // Convertir de LinkedHashMap (que es como Jackson deserializa tipos genéricos)
                if (data instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> dataMap = (LinkedHashMap<String, Object>) data;
                    ProductoDTO producto = convertFromMap(dataMap);
                    log.info("Producto obtenido exitosamente: {}", producto);
                    return producto;
                }

                throw new ProductoServiceException("Formato inesperado en respuesta del servicio");
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

    private ProductoDTO convertFromMap(LinkedHashMap<String, Object> dataMap) {
        try {
            // Extraer el id del nivel "data"
            String idString = dataMap.get("id").toString();

            // Extraer los attributes que contienen los datos del producto
            LinkedHashMap<String, Object> attributes =
                    (LinkedHashMap<String, Object>) dataMap.get("attributes");

            if (attributes == null) {
                throw new ProductoServiceException("Atributos del producto no encontrados en la respuesta");
            }

            ProductoDTO producto = new ProductoDTO();
            producto.setId(Long.parseLong(idString));
            producto.setNombre((String) attributes.get("nombre"));

            // Manejar precio que puede venir como Number, String, etc.
            Object precioObj = attributes.get("precio");
            if (precioObj != null) {
                producto.setPrecio(new BigDecimal(precioObj.toString()));
            }

            log.debug("Producto convertido: ID={}, Nombre={}", producto.getId(), producto.getNombre());
            return producto;

        } catch (Exception e) {
            log.error("Error en conversión de LinkedHashMap a ProductoDTO: {}", e.getMessage(), e);
            throw new ProductoServiceException("Error al convertir datos del producto: " + e.getMessage(), e);
        }
    }
}
