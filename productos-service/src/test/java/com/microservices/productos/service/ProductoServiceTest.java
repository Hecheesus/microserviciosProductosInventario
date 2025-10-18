package com.microservices.productos.service;

import com.microservices.productos.exception.ProductoNotFoundException;
import com.microservices.productos.model.Producto;
import com.microservices.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para ProductoService")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop HP");
        producto.setPrecio(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Debe crear un producto exitosamente")
    void crearProducto_DebeRetornarProductoGuardado() {
        // Arrange
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        Producto resultado = productoService.crearProducto(producto);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Laptop HP");
        assertThat(resultado.getPrecio()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe obtener un producto por ID cuando existe")
    void obtenerProductoPorId_CuandoExiste_DebeRetornarProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Producto resultado = productoService.obtenerProductoPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Laptop HP");
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException cuando el producto no existe")
    void obtenerProductoPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.obtenerProductoPorId(999L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("Producto no encontrado con ID: 999");
        verify(productoRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe listar productos con paginación")
    void listarProductos_DebeRetornarPaginaDeProductos() {
        // Arrange
        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Mouse Logitech");
        producto2.setPrecio(new BigDecimal("50.00"));

        List<Producto> listaProductos = Arrays.asList(producto, producto2);
        Page<Producto> pageProductos = new PageImpl<>(listaProductos, PageRequest.of(0, 10), listaProductos.size());

        Pageable pageable = PageRequest.of(0, 10);
        when(productoRepository.findAll(pageable)).thenReturn(pageProductos);

        // Act
        Page<Producto> resultado = productoService.listarProductos(pageable);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Laptop HP");
        assertThat(resultado.getContent().get(1).getNombre()).isEqualTo("Mouse Logitech");
        verify(productoRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Debe actualizar un producto existente")
    void actualizarProducto_CuandoExiste_DebeRetornarProductoActualizado() {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Laptop Dell Actualizada");
        productoActualizado.setPrecio(new BigDecimal("2000.00"));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        Producto resultado = productoService.actualizarProducto(1L, productoActualizado);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Laptop Dell Actualizada");
        assertThat(resultado.getPrecio()).isEqualByComparingTo(new BigDecimal("2000.00"));
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar producto inexistente")
    void actualizarProducto_CuandoNoExiste_DebeLanzarExcepcion() {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Producto Inexistente");
        productoActualizado.setPrecio(new BigDecimal("100.00"));

        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.actualizarProducto(999L, productoActualizado))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("Producto no encontrado con ID: 999");
        verify(productoRepository, times(1)).findById(999L);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar un producto existente")
    void eliminarProducto_CuandoExiste_DebeEliminarProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).delete(producto);

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).delete(producto);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar producto inexistente")
    void eliminarProducto_CuandoNoExiste_DebeLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productoService.eliminarProducto(999L))
                .isInstanceOf(ProductoNotFoundException.class)
                .hasMessageContaining("Producto no encontrado con ID: 999");
        verify(productoRepository, times(1)).findById(999L);
        verify(productoRepository, never()).delete(any());
    }
}