package com.microservices.productos.service;

import com.microservices.productos.exception.ProductoNotFoundException;
import com.microservices.productos.model.Producto;
import com.microservices.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    @Autowired
    private  ProductoRepository productoRepository;

    @Transactional
    public Producto crearProducto(Producto producto) {
        log.info("Creando producto: {}", producto.getNombre());
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long id) {
        log.info("Obteniendo producto con ID: {}", id);
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Producto> listarProductos(Pageable pageable) {
        log.info("Listando productos - Página: {}, Tamaño: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return productoRepository.findAll(pageable);
    }

    @Transactional
    public Producto actualizarProducto(Long id, Producto producto) {
        log.info("Actualizando producto con ID: {}", id);
        Producto productoExistente = obtenerProductoPorId(id);
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setPrecio(producto.getPrecio());
        return productoRepository.save(productoExistente);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto con ID: {}", id);
        Producto producto = obtenerProductoPorId(id);
        productoRepository.delete(producto);
    }
}
