package com.tienda.donarosa;

import com.tienda.donarosa.model.Producto;
import com.tienda.donarosa.repository.ProductoRepository;
import com.tienda.donarosa.service.ProductoService;
import com.tienda.donarosa.service.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ProductoService
 * Estas pruebas validan la funcionalidad del servicio y garantizan
 * que los datos de Doña Rosa estén seguros y funcionando correctamente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas del Servicio de Productos")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto1;
    private Producto producto2;
    private Producto productoAgotandose;

    @BeforeEach
    void setUp() {
        // Productos de prueba basados en los datos de Doña Rosa
        producto1 = new Producto("Peras", new BigDecimal("4000.00"), 65);
        producto1.setId(1L);
        producto1.setCantidadInicial(65);

        producto2 = new Producto("Limones", new BigDecimal("1500.00"), 25);
        producto2.setId(2L);
        producto2.setCantidadInicial(25);

        // Producto cerca de agotarse (10% o menos del stock inicial)
        productoAgotandose = new Producto("Fresas", new BigDecimal("3000.00"), 1);
        productoAgotandose.setId(3L);
        productoAgotandose.setCantidadInicial(12); // 1/12 = 8.3% < 10%
    }

    @Test
    @DisplayName("Debe obtener todos los productos correctamente")
    void debeObtenerTodosLosProductos() {
        // Given
        List<Producto> productosEsperados = Arrays.asList(producto1, producto2);
        when(productoRepository.findAll()).thenReturn(productosEsperados);

        // When
        List<Producto> productos = productoService.obtenerTodosLosProductos();

        // Then
        assertNotNull(productos);
        assertEquals(2, productos.size());
        assertEquals("Peras", productos.get(0).getNombre());
        assertEquals("Limones", productos.get(1).getNombre());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener un producto por ID correctamente")
    void debeObtenerProductoPorId() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // When
        Optional<Producto> resultado = productoService.obtenerProductoPorId(1L);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Peras", resultado.get().getNombre());
        assertEquals(new BigDecimal("4000.00"), resultado.get().getPrecio());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe guardar un nuevo producto correctamente")
    void debeGuardarNuevoProducto() {
        // Given
        Producto nuevoProducto = new Producto("Manzanas", new BigDecimal("2500.00"), 40);
        when(productoRepository.save(any(Producto.class))).thenReturn(nuevoProducto);

        // When
        Producto resultado = productoService.guardarProducto(nuevoProducto);

        // Then
        assertNotNull(resultado);
        assertEquals("Manzanas", resultado.getNombre());
        assertEquals(40, resultado.getCantidadInicial()); // Debe establecer cantidad inicial
        verify(productoRepository, times(1)).save(nuevoProducto);
    }

    @Test
    @DisplayName("Debe actualizar un producto existente correctamente")
    void debeActualizarProductoExistente() {
        // Given
        Producto productoActualizado = new Producto("Peras", new BigDecimal("4500.00"), 70);
        productoActualizado.setId(1L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        // When
        Producto resultado = productoService.actualizarProducto(productoActualizado);

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("4500.00"), resultado.getPrecio());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar producto inexistente")
    void debeLanzarExcepcionAlActualizarProductoInexistente() {
        // Given
        Producto productoInexistente = new Producto("Producto Inexistente", new BigDecimal("1000.00"), 10);
        productoInexistente.setId(999L);
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.actualizarProducto(productoInexistente);
        });

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository, times(1)).findById(999L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe eliminar un producto correctamente")
    void debeEliminarProductoCorrectamente() {
        // Given
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // When
        assertDoesNotThrow(() -> productoService.eliminarProducto(1L));

        // Then
        verify(productoRepository, times(1)).existsById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar producto inexistente")
    void debeLanzarExcepcionAlEliminarProductoInexistente() {
        // Given
        when(productoRepository.existsById(999L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.eliminarProducto(999L);
        });

        assertEquals("Producto no encontrado con ID: 999", exception.getMessage());
        verify(productoRepository, times(1)).existsById(999L);
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe encontrar productos cerca de agotarse")
    void debeEncontrarProductosCercaDeAgotarse() {
        // Given
        List<Producto> productosAgotandose = Arrays.asList(productoAgotandose);
        when(productoRepository.findProductosCercaDeAgotarse()).thenReturn(productosAgotandose);

        // When
        List<Producto> resultado = productoService.obtenerProductosCercaDeAgotarse();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Fresas", resultado.get(0).getNombre());
        assertTrue(resultado.get(0).estaCercaDeAgotarse());
        verify(productoRepository, times(1)).findProductosCercaDeAgotarse();
    }

    @Test
    @DisplayName("Debe encontrar el producto más cerca de agotarse")
    void debeEncontrarProductoMasCercaDeAgotarse() {
        // Given
        Producto producto1Agotandose = new Producto("Producto1", new BigDecimal("1000.00"), 5);
        producto1Agotandose.setCantidadInicial(100); // 5% restante

        Producto producto2Agotandose = new Producto("Producto2", new BigDecimal("2000.00"), 15);
        producto2Agotandose.setCantidadInicial(100); // 15% restante

        List<Producto> productosAgotandose = Arrays.asList(producto1Agotandose, producto2Agotandose);
        when(productoRepository.findProductosCercaDeAgotarse()).thenReturn(productosAgotandose);

        // When
        Optional<Producto> resultado = productoService.obtenerProductoMasCercaDeAgotarse();

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Producto1", resultado.get().getNombre()); // El que tiene menor porcentaje
        verify(productoRepository, times(1)).findProductosCercaDeAgotarse();
    }

    @Test
    @DisplayName("Debe calcular el valor total del inventario correctamente")
    void debeCalcularValorTotalInventario() {
        // Given
        Double valorEsperado = 525000.0; // (4000*65) + (1500*25) + (3000*1) = 260000 + 37500 + 3000
        when(productoRepository.calcularValorTotalInventario()).thenReturn(Optional.of(valorEsperado));

        // When
        BigDecimal resultado = productoService.calcularValorTotalInventario();

        // Then
        assertNotNull(resultado);
        assertEquals(new BigDecimal("525000.0"), resultado);
        verify(productoRepository, times(1)).calcularValorTotalInventario();
    }

    @Test
    @DisplayName("Debe retornar cero cuando no hay productos en inventario")
    void debeRetornarCeroCuandoNoHayProductos() {
        // Given
        when(productoRepository.calcularValorTotalInventario()).thenReturn(Optional.empty());

        // When
        BigDecimal resultado = productoService.calcularValorTotalInventario();

        // Then
        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado);
    }

    @Test
    @DisplayName("Debe actualizar la cantidad de un producto correctamente")
    void debeActualizarCantidadProducto() {
        // Given
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);

        // When
        boolean resultado = productoService.actualizarCantidadProducto(1L, 50);

        // Then
        assertTrue(resultado);
        assertEquals(50, producto1.getCantidad());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto1);
    }

    @Test
    @DisplayName("Debe rechazar cantidad negativa al actualizar")
    void debeRechazarCantidadNegativa() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productoService.actualizarCantidadProducto(1L, -5);
        });

        assertEquals("La cantidad no puede ser negativa", exception.getMessage());
        verify(productoRepository, never()).findById(anyLong());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe verificar si existe un producto con el mismo nombre")
    void debeVerificarSiExisteProductoConNombre() {
        // Given
        when(productoRepository.findByNombreIgnoreCase("Peras")).thenReturn(Optional.of(producto1));
        when(productoRepository.findByNombreIgnoreCase("Bananas")).thenReturn(Optional.empty());

        // When & Then
        assertTrue(productoService.existeProductoConNombre("Peras"));
        assertFalse(productoService.existeProductoConNombre("Bananas"));

        verify(productoRepository, times(1)).findByNombreIgnoreCase("Peras");
        verify(productoRepository, times(1)).findByNombreIgnoreCase("Bananas");
    }

    @Test
    @DisplayName("Debe buscar productos por nombre correctamente")
    void debeBuscarProductosPorNombre() {
        // Given
        List<Producto> productosEncontrados = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCase("Per")).thenReturn(productosEncontrados);

        // When
        List<Producto> resultado = productoService.buscarProductosPorNombre("Per");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Peras", resultado.get(0).getNombre());
        verify(productoRepository, times(1)).findByNombreContainingIgnoreCase("Per");
    }

    @Test
    @DisplayName("Debe obtener productos disponibles correctamente")
    void debeObtenerProductosDisponibles() {
        // Given
        List<Producto> productosDisponibles = Arrays.asList(producto1, producto2);
        when(productoRepository.findByCantidadGreaterThan(0)).thenReturn(productosDisponibles);

        // When
        List<Producto> resultado = productoService.obtenerProductosDisponibles();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findByCantidadGreaterThan(0);
    }

    @Test
    @DisplayName("Debe obtener productos agotados correctamente")
    void debeObtenerProductosAgotados() {
        // Given
        Producto productoAgotado = new Producto("Producto Agotado", new BigDecimal("1000.00"), 0);
        List<Producto> productosAgotados = Arrays.asList(productoAgotado);
        when(productoRepository.findByCantidadEquals(0)).thenReturn(productosAgotados);

        // When
        List<Producto> resultado = productoService.obtenerProductosAgotados();

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(0, resultado.get(0).getCantidad());
        verify(productoRepository, times(1)).findByCantidadEquals(0);
    }
}

