package com.tienda.donarosa.service;

import com.tienda.donarosa.model.Producto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoService {

    /**
     * Obtiene todos los productos
     */
    List<Producto> obtenerTodosLosProductos();

    /**
     * Obtiene un producto por su ID
     */
    Optional<Producto> obtenerProductoPorId(Long id);

    /**
     * Guarda un nuevo producto
     */
    Producto guardarProducto(Producto producto);

    /**
     * Actualiza un producto existente
     */
    Producto actualizarProducto(Producto producto);

    /**
     * Elimina un producto por su ID
     */
    void eliminarProducto(Long id);

    /**
     * Busca productos por nombre
     */
    List<Producto> buscarProductosPorNombre(String nombre);

    /**
     * Obtiene el producto más cerca de agotarse
     */
    Optional<Producto> obtenerProductoMasCercaDeAgotarse();

    /**
     * Obtiene todos los productos cerca de agotarse
     */
    List<Producto> obtenerProductosCercaDeAgotarse();

    /**
     * Calcula el valor total del inventario
     */
    BigDecimal calcularValorTotalInventario();

    /**
     * Actualiza la cantidad de un producto (para ventas)
     */
    boolean actualizarCantidadProducto(Long id, Integer nuevaCantidad);

    /**
     * Verifica si existe un producto con el mismo nombre
     */
    boolean existeProductoConNombre(String nombre);

    /**
     * Obtiene productos disponibles (cantidad > 0)
     */
    List<Producto> obtenerProductosDisponibles();

    /**
     * Obtiene productos agotados (cantidad = 0)
     */
    List<Producto> obtenerProductosAgotados();
}
