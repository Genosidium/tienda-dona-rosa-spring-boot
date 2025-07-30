package com.tienda.donarosa;

import com.tienda.donarosa.model.Producto;
import com.tienda.donarosa.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para ProductoRepository
 * Estas pruebas validan que los datos se almacenan y recuperan correctamente
 * garantizando la seguridad de la información de Doña Rosa.
 */
@DataJpaTest
@DisplayName("Pruebas del Repositorio de Productos")
class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto producto1;
    private Producto producto2;
    private Producto productoAgotandose;
    private Producto productoAgotado;

    @BeforeEach
    void setUp() {
        // Productos de prueba
        producto1 = new Producto("Peras", new BigDecimal("4000.00"), 65);
        producto1.setCantidadInicial(65);

        producto2 = new Producto("Limones", new BigDecimal("1500.00"), 25);
        producto2.setCantidadInicial(25);

        // Producto cerca de agotarse (menos del 10% del stock inicial)
        productoAgotandose = new Producto("Fresas", new BigDecimal("3000.00"), 1);
        productoAgotandose.setCantidadInicial(12); // 1/12 = 8.3% < 10%

        // Producto agotado
        productoAgotado = new Producto("Uvas", new BigDecimal("2500.00"), 0);
        productoAgotado.setCantidadInicial(20);

        // Persistir en base de datos de prueba
        entityManager.persistAndFlush(producto1);
        entityManager.persistAndFlush(producto2);
        entityManager.persistAndFlush(productoAgotandose);
        entityManager.persistAndFlush(productoAgotado);
    }

    @Test
    @DisplayName("Debe encontrar producto por nombre ignorando mayúsculas/minúsculas")
    void debeEncontrarProductoPorNombreIgnorandoCaso() {
        // When
        Optional<Producto> resultado1 = productoRepository.findByNombreIgnoreCase("peras");
        Optional<Producto> resultado2 = productoRepository.findByNombreIgnoreCase("PERAS");
        Optional<Producto> resultado3 = productoRepository.findByNombreIgnoreCase("Peras");

        // Then
        assertTrue(resultado1.isPresent());
        assertTrue(resultado2.isPresent());
        assertTrue(resultado3.isPresent());

        assertEquals("Peras", resultado1.get().getNombre());
        assertEquals("Peras", resultado2.get().getNombre());
        assertEquals("Peras", resultado3.get().getNombre());
    }

    @Test
    @DisplayName("Debe encontrar productos que contengan término en el nombre")
    void debeEncontrarProductosQueContenganTermino() {
        // When
        List<Producto> resultadosPer = productoRepository.findByNombreContainingIgnoreCase("per");
        List<Producto> resultadosLim = productoRepository.findByNombreContainingIgnoreCase("lim");

        // Then
        assertEquals(1, resultadosPer.size());
        assertEquals("Peras", resultadosPer.get(0).getNombre());

        assertEquals(1, resultadosLim.size());
        assertEquals("Limones", resultadosLim.get(0).getNombre());
    }

    @Test
    @DisplayName("Debe encontrar productos cerca de agotarse")
    void debeEncontrarProductosCercaDeAgotarse() {
        // When
        List<Producto> productosAgotandose = productoRepository.findProductosCercaDeAgotarse();

        // Then
        assertNotNull(productosAgotandose);
        assertFalse(productosAgotandose.isEmpty());

        // Verificar que el producto con 1 unidad de 12 iniciales está en la lista
        boolean fesasEncontradas = productosAgotandose.stream()
                .anyMatch(p -> "Fresas".equals(p.getNombre()));
        assertTrue(fesasEncontradas);

        // Verificar que productos con stock suficiente no están en la lista
        boolean perasEncontradas = productosAgotandose.stream()
                .anyMatch(p -> "Peras".equals(p.getNombre()));
        assertFalse(perasEncontradas);
    }

    @Test
    @DisplayName("Debe encontrar el producto con menor cantidad")
    void debeEncontrarProductoConMenorCantidad() {
        // When
        Optional<Producto> resultado = productoRepository.findProductoConMenorCantidad();

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Uvas", resultado.get().getNombre()); // El producto agotado (cantidad = 0)
        assertEquals(0, resultado.get().getCantidad());
    }

    @Test
    @DisplayName("Debe calcular el valor total del inventario correctamente")
    void debeCalcularValorTotalInventarioCorrectamente() {
        // When
        Optional<Double> valorTotal = productoRepository.calcularValorTotalInventario();

        // Then
        assertTrue(valorTotal.isPresent());

        // Calcular valor esperado:
        // Peras: 4000 * 65 = 260,000
        // Limones: 1500 * 25 = 37,500
        // Fresas: 3000 * 1 = 3,000
        // Uvas: 2500 * 0 = 0
        // Total esperado: 300,500
        double valorEsperado = 300500.0;
        assertEquals(valorEsperado, valorTotal.get(), 0.01);
    }

    @Test
    @DisplayName("Debe encontrar productos con cantidad mayor a un valor")
    void debeEncontrarProductosConCantidadMayorA() {
        // When
        List<Producto> productosDisponibles = productoRepository.findByCantidadGreaterThan(0);
        List<Producto> productosConStock = productoRepository.findByCantidadGreaterThan(10);

        // Then
        assertEquals(3, productosDisponibles.size()); // Todos excepto Uvas (cantidad = 0)
        assertEquals(2, productosConStock.size()); // Peras (65) y Limones (25)

        // Verificar que no incluye el producto agotado
        boolean uvasEncontradas = productosDisponibles.stream()
                .anyMatch(p -> "Uvas".equals(p.getNombre()));
        assertFalse(uvasEncontradas);
    }

    @Test
    @DisplayName("Debe encontrar productos agotados")
    void debeEncontrarProductosAgotados() {
        // When
        List<Producto> productosAgotados = productoRepository.findByCantidadEquals(0);

        // Then
        assertEquals(1, productosAgotados.size());
        assertEquals("Uvas", productosAgotados.get(0).getNombre());
        assertEquals(0, productosAgotados.get(0).getCantidad());
    }

    @Test
    @DisplayName("Debe persistir producto con cantidad inicial automática")
    void debePersistirProductoConCantidadInicialAutomatica() {
        // Given
        Producto nuevoProducto = new Producto("Manzanas", new BigDecimal("2200.00"), 30);
        // No establecer cantidad inicial manualmente

        // When
        Producto productoGuardado = productoRepository.save(nuevoProducto);
        entityManager.flush();

        // Then
        assertNotNull(productoGuardado.getId());
        assertEquals(30, productoGuardado.getCantidad());
        assertEquals(30, productoGuardado.getCantidadInicial()); // Se debe establecer automáticamente
    }

    @Test
    @DisplayName("Debe validar restricciones de la entidad Producto")
    void debeValidarRestriccionesEntidadProducto() {
        // Given - Producto con nombre duplicado
        Producto productoDuplicado = new Producto("Peras", new BigDecimal("3500.00"), 40);

        // When & Then - Debe lanzar excepción por nombre duplicado
        assertThrows(Exception.class, () -> {
            productoRepository.save(productoDuplicado);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Debe mantener integridad de datos después de operaciones CRUD")
    void debeMantenerIntegridadDatosDespuesOperacionesCRUD() {
        // Given
        long cantidadInicial = productoRepository.count();

        // When - Agregar nuevo producto
        Producto nuevoProducto = new Producto("Bananas", new BigDecimal("1800.00"), 45);
        productoRepository.save(nuevoProducto);
        entityManager.flush();

        // Then - Verificar que se agregó correctamente
        assertEquals(cantidadInicial + 1, productoRepository.count());

        // When - Actualizar producto existente
        producto1.setPrecio(new BigDecimal("4200.00"));
        producto1.setCantidad(60);
        productoRepository.save(producto1);
        entityManager.flush();

        // Then - Verificar que se actualizó correctamente
        Optional<Producto> productoActualizado = productoRepository.findById(producto1.getId());
        assertTrue(productoActualizado.isPresent());
        assertEquals(new BigDecimal("4200.00"), productoActualizado.get().getPrecio());
        assertEquals(60, productoActualizado.get().getCantidad());

        // When - Eliminar producto
        productoRepository.delete(nuevoProducto);
        entityManager.flush();

        // Then - Verificar que se eliminó correctamente
        assertEquals(cantidadInicial, productoRepository.count());
        assertFalse(productoRepository.findById(nuevoProducto.getId()).isPresent());
    }

    @Test
    @DisplayName("Debe manejar búsquedas de productos inexistentes")
    void debeManejarBusquedasProductosInexistentes() {
        // When
        Optional<Producto> productoInexistente = productoRepository.findByNombreIgnoreCase("ProductoQueNoExiste");
        List<Producto> busquedaVacia = productoRepository.findByNombreContainingIgnoreCase("TerminoInexistente");

        // Then
        assertFalse(productoInexistente.isPresent());
        assertTrue(busquedaVacia.isEmpty());
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje restante de productos")
    void debeCalcularCorrectamentePorcentajeRestante() {
        // When & Then
        assertEquals(100.0, producto1.getPorcentajeRestante(), 0.01); // 65/65 = 100%
        assertEquals(100.0, producto2.getPorcentajeRestante(), 0.01); // 25/25 = 100%
        assertEquals(8.33, productoAgotandose.getPorcentajeRestante(), 0.01); // 1/12 = 8.33%
        assertEquals(0.0, productoAgotado.getPorcentajeRestante(), 0.01); // 0/20 = 0%
    }

    @Test
    @DisplayName("Debe identificar correctamente productos cerca de agotarse")
    void debeIdentificarCorrectamenteProductosCercaDeAgotarse() {
        // When & Then
        assertFalse(producto1.estaCercaDeAgotarse()); // 65/65 = 100% > 10%
        assertFalse(producto2.estaCercaDeAgotarse()); // 25/25 = 100% > 10%
        assertTrue(productoAgotandose.estaCercaDeAgotarse()); // 1/12 = 8.33% < 10%
        assertTrue(productoAgotado.estaCercaDeAgotarse()); // 0/20 = 0% < 10%
    }

    @Test
    @DisplayName("Debe calcular correctamente el valor total por producto")
    void debeCalcularCorrectamenteValorTotalPorProducto() {
        // When & Then
        assertEquals(new BigDecimal("260000.00"), producto1.getValorTotal()); // 4000 * 65
        assertEquals(new BigDecimal("37500.00"), producto2.getValorTotal()); // 1500 * 25
        assertEquals(new BigDecimal("3000.00"), productoAgotandose.getValorTotal()); // 3000 * 1
        assertEquals(new BigDecimal("0.00"), productoAgotado.getValorTotal()); // 2500 * 0
    }
}
