package com.tienda.donarosa.repository;

import com.tienda.donarosa.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su nombre exacto
     */
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Busca productos que contengan el término en el nombre
     */
    List<Producto> findByNombreContainingIgnoreCase(String termino);

    /**
     * Encuentra productos con cantidad menor o igual al porcentaje especificado
     * de su cantidad inicial (productos cerca de agotarse)
     */
    @Query("SELECT p FROM Producto p WHERE p.cantidad <= (p.cantidadInicial * 0.1)")
    List<Producto> findProductosCercaDeAgotarse();

    /**
     * Encuentra el producto con menor cantidad disponible
     */
    @Query("SELECT p FROM Producto p ORDER BY p.cantidad ASC LIMIT 1")
    Optional<Producto> findProductoConMenorCantidad();

    /**
     * Calcula el valor total del inventario
     */
    @Query("SELECT SUM(p.precio * p.cantidad) FROM Producto p")
    Optional<Double> calcularValorTotalInventario();

    /**
     * Encuentra productos con cantidad mayor a cero
     */
    List<Producto> findByCantidadGreaterThan(Integer cantidad);

    /**
     * Encuentra productos agotados
     */
    List<Producto> findByCantidadEquals(Integer cantidad);
}
