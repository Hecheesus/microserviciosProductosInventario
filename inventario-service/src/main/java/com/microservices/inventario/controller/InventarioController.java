package com.microservices.inventario.controller;

import com.microservices.inventario.model.Inventario;
import com.microservices.inventario.model.InventarioConProductoDTO;
import com.microservices.inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para gestión de inventario
 * Se comunica con el microservicio de productos para obtener información completa
 */
@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventario", description = "API para gestión de inventario de productos")
@CrossOrigin(origins = "*")
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * Consultar inventario de un producto con su información completa
     * Este endpoint consulta el inventario local y obtiene los datos del producto
     * desde el microservicio de productos
     *
     * @param productoId ID del producto a consultar
     * @return Inventario con información completa del producto
     */
    @Operation(
            summary = "Consultar inventario por ID de producto",
            description = "Obtiene la cantidad disponible de un producto junto con su información básica. " +
                    "Realiza una llamada al microservicio de productos para enriquecer la respuesta."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventarioConProductoDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado para el producto especificado"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Servicio de productos no disponible"
            )
    })
    @GetMapping("/{productoId}")
    public ResponseEntity<InventarioConProductoDTO> consultarInventario(
            @Parameter(description = "ID del producto a consultar", required = true, example = "1")
            @PathVariable Long productoId) {

        log.info("REST request para consultar inventario del producto ID: {}", productoId);

        InventarioConProductoDTO inventario = inventarioService.consultarInventario(productoId);

        return ResponseEntity.ok(inventario);
    }

    /**
     * Actualizar la cantidad de inventario de un producto
     * Emite un evento en consola cuando el inventario cambia
     *
     * @param productoId ID del producto
     * @param request Map con la nueva cantidad
     * @return Inventario actualizado con información del producto
     */
    @Operation(
            summary = "Actualizar cantidad de inventario",
            description = "Actualiza la cantidad disponible de un producto. " +
                    "Verifica que el producto existe antes de actualizar. " +
                    "Emite un evento en logs cuando el inventario cambia."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioConProductoDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos (cantidad negativa o campo faltante)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario o producto no encontrado"
            )
    })
    @PutMapping("/{productoId}")
    public ResponseEntity<InventarioConProductoDTO> actualizarInventario(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long productoId,

            @Parameter(
                    description = "Nueva cantidad de inventario",
                    required = true,
                    schema = @Schema(example = "{\"cantidad\": 50}")
            )
            @Valid @RequestBody Map<String, Integer> request) {

        log.info("REST request para actualizar inventario del producto ID: {}", productoId);

        // Validar que venga el campo cantidad
        if (!request.containsKey("cantidad")) {
            throw new IllegalArgumentException("El campo 'cantidad' es obligatorio en el body");
        }

        Integer nuevaCantidad = request.get("cantidad");

        // Validar que la cantidad sea válida
        if (nuevaCantidad == null) {
            throw new IllegalArgumentException("La cantidad no puede ser null");
        }

        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        InventarioConProductoDTO inventarioActualizado =
                inventarioService.actualizarCantidad(productoId, nuevaCantidad);

        return ResponseEntity.ok(inventarioActualizado);
    }

    /**
     * Crear inventario para un nuevo producto
     * Verifica que el producto existe en el microservicio de productos
     *
     * @param request Map con productoId y cantidadInicial
     * @return Inventario creado
     */
    @Operation(
            summary = "Crear inventario para un producto",
            description = "Crea un nuevo registro de inventario para un producto existente. " +
                    "Verifica que el producto existe en el microservicio de productos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Inventario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Inventario.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe inventario para este producto"
            )
    })
    @PostMapping
    public ResponseEntity<Inventario> crearInventario(
            @Parameter(
                    description = "Datos del inventario a crear",
                    required = true,
                    schema = @Schema(example = "{\"productoId\": 1, \"cantidadInicial\": 100}")
            )
            @Valid @RequestBody Map<String, Object> request) {

        log.info("REST request para crear inventario");

        // Validar campos requeridos
        if (!request.containsKey("productoId") || !request.containsKey("cantidadInicial")) {
            throw new IllegalArgumentException(
                    "Los campos 'productoId' y 'cantidadInicial' son obligatorios");
        }

        // Extraer datos (manejar tanto Integer como Long para productoId)
        Long productoId;
        Object productoIdObj = request.get("productoId");
        if (productoIdObj instanceof Integer) {
            productoId = ((Integer) productoIdObj).longValue();
        } else {
            productoId = (Long) productoIdObj;
        }

        Integer cantidadInicial = (Integer) request.get("cantidadInicial");

        // Validaciones
        if (cantidadInicial < 0) {
            throw new IllegalArgumentException("La cantidad inicial no puede ser negativa");
        }

        Inventario inventarioCreado =
                inventarioService.crearInventario(productoId, cantidadInicial);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventarioCreado);
    }

    /**
     * Decrementar stock tras una compra o venta
     * Útil para simular transacciones de venta
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad a decrementar
     * @return Inventario actualizado
     */
    @Operation(
            summary = "Decrementar stock tras compra",
            description = "Disminuye la cantidad de inventario simulando una venta. " +
                    "Valida que haya stock suficiente antes de decrementar."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock decrementado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Stock insuficiente o cantidad inválida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado"
            )
    })
    @PatchMapping("/{productoId}/decrementar")
    public ResponseEntity<InventarioConProductoDTO> decrementarStock(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long productoId,

            @Parameter(
                    description = "Cantidad a decrementar (debe ser mayor a 0)",
                    required = true,
                    example = "5"
            )
            @RequestParam @Min(value = 1, message = "La cantidad debe ser mayor a 0")
            Integer cantidad) {

        log.info("REST request para decrementar stock del producto ID: {} en {} unidades",
                productoId, cantidad);

        // Obtener inventario actual
        InventarioConProductoDTO inventarioActual =
                inventarioService.consultarInventario(productoId);

        // Validar stock suficiente
        if (inventarioActual.getCantidad() < cantidad) {
            throw new IllegalArgumentException(
                    String.format("Stock insuficiente. Disponible: %d, Solicitado: %d",
                            inventarioActual.getCantidad(), cantidad));
        }

        // Calcular nueva cantidad
        Integer nuevaCantidad = inventarioActual.getCantidad() - cantidad;

        // Actualizar inventario
        InventarioConProductoDTO inventarioActualizado =
                inventarioService.actualizarCantidad(productoId, nuevaCantidad);

        return ResponseEntity.ok(inventarioActualizado);
    }

    /**
     * Incrementar stock (por ejemplo, al recibir mercancía)
     *
     * @param productoId ID del producto
     * @param cantidad Cantidad a incrementar
     * @return Inventario actualizado
     */
    @Operation(
            summary = "Incrementar stock",
            description = "Aumenta la cantidad de inventario (ej: recepción de mercancía nueva)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock incrementado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado"
            )
    })
    @PatchMapping("/{productoId}/incrementar")
    public ResponseEntity<InventarioConProductoDTO> incrementarStock(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,

            @Parameter(description = "Cantidad a incrementar", required = true)
            @RequestParam @Min(value = 1, message = "La cantidad debe ser mayor a 0")
            Integer cantidad) {

        log.info("REST request para incrementar stock del producto ID: {} en {} unidades",
                productoId, cantidad);

        // Obtener inventario actual
        InventarioConProductoDTO inventarioActual =
                inventarioService.consultarInventario(productoId);

        // Calcular nueva cantidad
        Integer nuevaCantidad = inventarioActual.getCantidad() + cantidad;

        // Actualizar inventario
        InventarioConProductoDTO inventarioActualizado =
                inventarioService.actualizarCantidad(productoId, nuevaCantidad);

        return ResponseEntity.ok(inventarioActualizado);
    }

    /**
     * Verificar disponibilidad de stock
     *
     * @param productoId ID del producto
     * @param cantidadRequerida Cantidad que se necesita verificar
     * @return true si hay stock suficiente, false en caso contrario
     */
    @Operation(
            summary = "Verificar disponibilidad de stock",
            description = "Verifica si hay suficiente stock disponible para una cantidad específica"
    )
    @GetMapping("/{productoId}/disponibilidad")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(
            @Parameter(description = "ID del producto")
            @PathVariable Long productoId,

            @Parameter(description = "Cantidad requerida a verificar")
            @RequestParam Integer cantidadRequerida) {

        log.info("Verificando disponibilidad de {} unidades para producto ID: {}",
                cantidadRequerida, productoId);

        InventarioConProductoDTO inventario =
                inventarioService.consultarInventario(productoId);

        boolean disponible = inventario.getCantidad() >= cantidadRequerida;

        return ResponseEntity.ok(Map.of(
                "productoId", productoId,
                "cantidadRequerida", cantidadRequerida,
                "cantidadDisponible", inventario.getCantidad(),
                "disponible", disponible,
                "productoNombre", inventario.getProducto().getNombre()
        ));
    }
}
