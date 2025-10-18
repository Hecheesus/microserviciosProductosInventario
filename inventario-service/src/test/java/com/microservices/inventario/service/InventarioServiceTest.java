package com.microservices.inventario.service;

import com.microservices.inventario.exception.InventarioNotFoundException;
import com.microservices.inventario.model.Inventario;
import com.microservices.inventario.model.InventarioConProductoDTO;
import com.microservices.inventario.model.ProductoDTO;
import com.microservices.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para InventarioService")
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoClientService productoClientService;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private ProductoDTO productoDTO;
    private static final Long PRODUCTO_ID = 1L;
    private static final Integer CANTIDAD_INICIAL = 100;
    private static final Integer CANTIDAD_NUEVA = 150;

    @BeforeEach
    void setUp() {
        inventario = new Inventario(1L, PRODUCTO_ID, CANTIDAD_INICIAL);

        productoDTO = new ProductoDTO();
        productoDTO.setId(PRODUCTO_ID);
        productoDTO.setNombre("Laptop HP");
        productoDTO.setPrecio(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Debe consultar inventario exitosamente")
    void consultarInventario_DebeRetornarInventarioConProducto() {
        // Arrange
        when(inventarioRepository.findByProductoId(PRODUCTO_ID))
                .thenReturn(Optional.of(inventario));
        when(productoClientService.obtenerProducto(PRODUCTO_ID))
                .thenReturn(productoDTO);

        // Act
        InventarioConProductoDTO resultado = inventarioService.consultarInventario(PRODUCTO_ID);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getProductoId()).isEqualTo(PRODUCTO_ID);
        assertThat(resultado.getCantidad()).isEqualTo(CANTIDAD_INICIAL);
        assertThat(resultado.getProducto()).isNotNull();
        assertThat(resultado.getProducto().getNombre()).isEqualTo("Laptop HP");

        verify(inventarioRepository, times(1)).findByProductoId(PRODUCTO_ID);
        verify(productoClientService, times(1)).obtenerProducto(PRODUCTO_ID);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando inventario no existe")
    void consultarInventario_NoExiste_DebeLanzarExcepcion() {
        // Arrange
        when(inventarioRepository.findByProductoId(PRODUCTO_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventarioService.consultarInventario(PRODUCTO_ID))
                .isInstanceOf(InventarioNotFoundException.class)
                .hasMessageContaining("Inventario no encontrado para producto ID: " + PRODUCTO_ID);

        verify(inventarioRepository, times(1)).findByProductoId(PRODUCTO_ID);
        verify(productoClientService, never()).obtenerProducto(any());
    }

    @Test
    @DisplayName("Debe actualizar cantidad exitosamente")
    void actualizarCantidad_DebeActualizarYRetornarInventario() {
        // Arrange
        when(inventarioRepository.findByProductoId(PRODUCTO_ID))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class)))
                .thenReturn(inventario);
        when(productoClientService.obtenerProducto(PRODUCTO_ID))
                .thenReturn(productoDTO);

        // Act
        InventarioConProductoDTO resultado = inventarioService.actualizarCantidad(PRODUCTO_ID, CANTIDAD_NUEVA);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getProductoId()).isEqualTo(PRODUCTO_ID);
        assertThat(resultado.getCantidad()).isEqualTo(CANTIDAD_NUEVA);
        assertThat(resultado.getProducto()).isNotNull();

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getCantidad()).isEqualTo(CANTIDAD_NUEVA);

        verify(inventarioRepository, times(1)).findByProductoId(PRODUCTO_ID);
        verify(productoClientService, times(1)).obtenerProducto(PRODUCTO_ID);
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar inventario inexistente")
    void actualizarCantidad_InventarioNoExiste_DebeLanzarExcepcion() {
        // Arrange
        when(inventarioRepository.findByProductoId(PRODUCTO_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventarioService.actualizarCantidad(PRODUCTO_ID, CANTIDAD_NUEVA))
                .isInstanceOf(InventarioNotFoundException.class)
                .hasMessageContaining("Inventario no encontrado para producto ID: " + PRODUCTO_ID);

        verify(inventarioRepository, times(1)).findByProductoId(PRODUCTO_ID);
        verify(inventarioRepository, never()).save(any());
        verify(productoClientService, never()).obtenerProducto(any());
    }

    @Test
    @DisplayName("Debe crear inventario exitosamente")
    void crearInventario_DebeCrearYRetornarInventario() {
        // Arrange
        Inventario nuevoInventario = new Inventario(null, PRODUCTO_ID, CANTIDAD_INICIAL);
        Inventario inventarioGuardado = new Inventario(1L, PRODUCTO_ID, CANTIDAD_INICIAL);

        when(productoClientService.obtenerProducto(PRODUCTO_ID))
                .thenReturn(productoDTO);
        when(inventarioRepository.save(any(Inventario.class)))
                .thenReturn(inventarioGuardado);

        // Act
        Inventario resultado = inventarioService.crearInventario(PRODUCTO_ID, CANTIDAD_INICIAL);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getProductoId()).isEqualTo(PRODUCTO_ID);
        assertThat(resultado.getCantidad()).isEqualTo(CANTIDAD_INICIAL);

        verify(productoClientService, times(1)).obtenerProducto(PRODUCTO_ID);

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getProductoId()).isEqualTo(PRODUCTO_ID);
        assertThat(captor.getValue().getCantidad()).isEqualTo(CANTIDAD_INICIAL);
    }

    @Test
    @DisplayName("Debe actualizar cantidad a cero")
    void actualizarCantidad_CantidadCero_DebeActualizar() {
        // Arrange
        Integer cantidadCero = 0;
        when(inventarioRepository.findByProductoId(PRODUCTO_ID))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class)))
                .thenReturn(inventario);
        when(productoClientService.obtenerProducto(PRODUCTO_ID))
                .thenReturn(productoDTO);

        // Act
        InventarioConProductoDTO resultado = inventarioService.actualizarCantidad(PRODUCTO_ID, cantidadCero);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCantidad()).isEqualTo(cantidadCero);

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getCantidad()).isEqualTo(cantidadCero);
    }


}
