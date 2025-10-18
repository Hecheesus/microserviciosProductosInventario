package com.microservices.inventario.service;

import com.microservices.inventario.exception.InventarioNotFoundException;
import com.microservices.inventario.model.Inventario;
import com.microservices.inventario.model.InventarioConProductoDTO;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoClientService productoClientService;

    @Transactional(readOnly = true)
    public InventarioConProductoDTO consultarInventario(Long productoId) {
        log.info("Consultando inventario para producto ID: {}", productoId);

        // Obtener inventario
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioNotFoundException(
                        "Inventario no encontrado para producto ID: " + productoId));

        // Obtener información del producto desde el otro microservicio
        ProductoDTO producto = productoClientService.obtenerProducto(productoId);

        // Construir DTO con ambas informaciones
        InventarioConProductoDTO resultado = new InventarioConProductoDTO();
        resultado.setProductoId(inventario.getProductoId());
        resultado.setCantidad(inventario.getCantidad());
        resultado.setProducto(producto);

        log.info("Inventario consultado exitosamente: Producto={}, Cantidad={}", 
                producto.getNombre(), inventario.getCantidad());

        return resultado;
    }

    @Transactional
    public InventarioConProductoDTO actualizarCantidad(Long productoId, Integer nuevaCantidad) {
        log.info("Actualizando inventario para producto ID: {} a cantidad: {}", productoId, nuevaCantidad);

        // Obtener inventario existente
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioNotFoundException(
                        "Inventario no encontrado para producto ID: " + productoId));

        Integer cantidadAnterior = inventario.getCantidad();

        // Actualizar cantidad
        inventario.setCantidad(nuevaCantidad);
        inventarioRepository.save(inventario);

        // Emitir evento
        emitirEventoInventario(productoId, cantidadAnterior, nuevaCantidad);

        // Obtener información del producto
        ProductoDTO producto = productoClientService.obtenerProducto(productoId);

        // Construir respuesta
        InventarioConProductoDTO resultado = new InventarioConProductoDTO();
        resultado.setProductoId(inventario.getProductoId());
        resultado.setCantidad(inventario.getCantidad());
        resultado.setProducto(producto);

        log.info("Inventario actualizado exitosamente");

        return resultado;
    }

    @Transactional
    public Inventario crearInventario(Long productoId, Integer cantidadInicial) {
        log.info("Creando inventario para producto ID: {} con cantidad inicial: {}", 
                productoId, cantidadInicial);

        // Verificar que el producto existe
        productoClientService.obtenerProducto(productoId);

        // Crear inventario
        Inventario inventario = new Inventario(null, productoId, cantidadInicial);
        inventario = inventarioRepository.save(inventario);

        log.info("Inventario creado exitosamente");

        return inventario;
    }

    private void emitirEventoInventario(Long productoId, Integer cantidadAnterior, Integer cantidadNueva) {
        // Implementación básica: log del evento
        log.info("[INVENTARIO_EVENTO] Inventario actualizado - ProductoId: {}, " +
                "Cantidad anterior: {}, Cantidad nueva: {}", 
                productoId, cantidadAnterior, cantidadNueva);
    }
}
