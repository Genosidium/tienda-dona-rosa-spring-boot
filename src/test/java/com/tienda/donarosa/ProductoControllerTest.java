package com.tienda.donarosa;

import com.tienda.donarosa.controller.ProductoController;
import com.tienda.donarosa.model.Producto;
import com.tienda.donarosa.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para ProductoController
 * Estas pruebas validan que la interfaz web funciona correctamente
 * y que Doña Rosa puede usar la aplicación sin problemas.
 */
@WebMvcTest(ProductoController.class)
@DisplayName("Pruebas del Controlador de Productos")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;
    private List<Producto> productos;

    @BeforeEach
    void setUp() {
        producto1 = new Producto("Peras", new BigDecimal("4000.00"), 65);
        producto1.setId(1L);
        producto1.setCantidadInicial(65);

        producto2 = new Producto("Limones", new BigDecimal("1500.00"), 25);
        producto2.setId(2L);
        producto2.setCantidadInicial(25);

        productos = Arrays.asList(producto1, producto2);
    }

    @Test
    @DisplayName("Debe mostrar la página principal correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeMostrarPaginaPrincipalCorrectamente() throws Exception {
        // Given
        when(productoService.obtenerTodosLosProductos()).thenReturn(productos);
        when(productoService.calcularValorTotalInventario()).thenReturn(new BigDecimal("297500"));
        when(productoService.obtenerProductoMasCercaDeAgotarse()).thenReturn(Optional.of(producto2));

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attributeExists("valorTotal"))
                .andExpect(model().attributeExists("productoAgotandose"));

        verify(productoService, times(1)).obtenerTodosLosProductos();
        verify(productoService, times(1)).calcularValorTotalInventario();
        verify(productoService, times(1)).obtenerProductoMasCercaDeAgotarse();
    }

    @Test
    @DisplayName("Debe mostrar la lista de productos correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeMostrarListaProductosCorrectamente() throws Exception {
        // Given
        when(productoService.obtenerTodosLosProductos()).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/lista"))
                .andExpect(model().attributeExists("productos"));

        verify(productoService, times(1)).obtenerTodosLosProductos();
    }

    @Test
    @DisplayName("Debe buscar productos por nombre correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeBuscarProductosPorNombreCorrectamente() throws Exception {
        // Given
        List<Producto> productosEncontrados = Arrays.asList(producto1);
        when(productoService.buscarProductosPorNombre("Peras")).thenReturn(productosEncontrados);

        // When & Then
        mockMvc.perform(get("/productos").param("buscar", "Peras"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/lista"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("busqueda", "Peras"));

        verify(productoService, times(1)).buscarProductosPorNombre("Peras");
    }

    @Test
    @DisplayName("Debe mostrar formulario para nuevo producto")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeMostrarFormularioNuevoProducto() throws Exception {
        // When & Then
        mockMvc.perform(get("/productos/nuevo"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().attributeExists("producto"))
                .andExpect(model().attribute("accion", "Agregar"));
    }

    @Test
    @DisplayName("Debe guardar nuevo producto correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeGuardarNuevoProductoCorrectamente() throws Exception {
        // Given
        when(productoService.existeProductoConNombre("Manzanas")).thenReturn(false);
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(producto1);

        // When & Then
        mockMvc.perform(post("/productos/nuevo")
                        .with(csrf())
                        .param("nombre", "Manzanas")
                        .param("precio", "2500.00")
                        .param("cantidad", "40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).existeProductoConNombre("Manzanas");
        verify(productoService, times(1)).guardarProducto(any(Producto.class));
    }

    @Test
    @DisplayName("Debe rechazar producto con nombre duplicado")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeRechazarProductoConNombreDuplicado() throws Exception {
        // Given
        when(productoService.existeProductoConNombre("Peras")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/productos/nuevo")
                        .with(csrf())
                        .param("nombre", "Peras")
                        .param("precio", "4000.00")
                        .param("cantidad", "50"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().hasErrors());

        verify(productoService, times(1)).existeProductoConNombre("Peras");
        verify(productoService, never()).guardarProducto(any(Producto.class));
    }

    @Test
    @DisplayName("Debe validar datos del producto al guardar")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeValidarDatosProductoAlGuardar() throws Exception {
        // When & Then - Nombre vacío
        mockMvc.perform(post("/productos/nuevo")
                        .with(csrf())
                        .param("nombre", "")
                        .param("precio", "1000.00")
                        .param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().hasErrors());

        // When & Then - Precio negativo
        mockMvc.perform(post("/productos/nuevo")
                        .with(csrf())
                        .param("nombre", "ProductoTest")
                        .param("precio", "-500.00")
                        .param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().hasErrors());

        // When & Then - Cantidad negativa
        mockMvc.perform(post("/productos/nuevo")
                        .with(csrf())
                        .param("nombre", "ProductoTest")
                        .param("precio", "1000.00")
                        .param("cantidad", "-5"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().hasErrors());

        verify(productoService, never()).guardarProducto(any(Producto.class));
    }

    @Test
    @DisplayName("Debe mostrar formulario de edición correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeMostrarFormularioEdicionCorrectamente() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(1L)).thenReturn(Optional.of(producto1));

        // When & Then
        mockMvc.perform(get("/productos/editar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("productos/formulario"))
                .andExpect(model().attributeExists("producto"))
                .andExpect(model().attribute("accion", "Editar"));

        verify(productoService, times(1)).obtenerProductoPorId(1L);
    }

    @Test
    @DisplayName("Debe redirigir si producto a editar no existe")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeRedirigirSiProductoAEditarNoExiste() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/productos/editar/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).obtenerProductoPorId(999L);
    }

    @Test
    @DisplayName("Debe actualizar producto correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeActualizarProductoCorrectamente() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(1L)).thenReturn(Optional.of(producto1));
        when(productoService.actualizarProducto(any(Producto.class))).thenReturn(producto1);

        // When & Then
        mockMvc.perform(post("/productos/editar/1")
                        .with(csrf())
                        .param("nombre", "Peras Premium")
                        .param("precio", "4500.00")
                        .param("cantidad", "70"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).obtenerProductoPorId(1L);
        verify(productoService, times(1)).actualizarProducto(any(Producto.class));
    }

    @Test
    @DisplayName("Debe eliminar producto correctamente")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeEliminarProductoCorrectamente() throws Exception {
        // Given
        when(productoService.obtenerProductoPorId(1L)).thenReturn(Optional.of(producto1));
        doNothing().when(productoService).eliminarProducto(1L);

        // When & Then
        mockMvc.perform(get("/productos/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));

        verify(productoService, times(1)).obtenerProductoPorId(1L);
        verify(productoService, times(1)).eliminarProducto(1L);
    }

    @Test
    @DisplayName("Debe actualizar cantidad de producto vía AJAX")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeActualizarCantidadProductoViaAjax() throws Exception {
        // Given
        when(productoService.actualizarCantidadProducto(1L, 45)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/productos/actualizar-cantidad/1")
                        .with(csrf())
                        .param("cantidad", "45"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cantidad actualizada exitosamente"));

        verify(productoService, times(1)).actualizarCantidadProducto(1L, 45);
    }

    @Test
    @DisplayName("Debe rechazar cantidad negativa vía AJAX")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeRechazarCantidadNegativaViaAjax() throws Exception {
        // When & Then
        mockMvc.perform(post("/productos/actualizar-cantidad/1")
                        .with(csrf())
                        .param("cantidad", "-10"))
                .andExpect(status().isOk())
                .andExpect(content().string("La cantidad no puede ser negativa"));

        verify(productoService, never()).actualizarCantidadProducto(anyLong(), any(Integer.class));
    }

    @Test
    @DisplayName("Debe manejar producto no encontrado en actualización de cantidad")
    @WithMockUser(username = "dona_rosa", roles = "ADMIN")
    void debeManejarProductoNoEncontradoEnActualizacionCantidad() throws Exception {
        // Given
        when(productoService.actualizarCantidadProducto(999L, 10)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/productos/actualizar-cantidad/999")
                        .with(csrf())
                        .param("cantidad", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Producto no encontrado"));

        verify(productoService, times(1)).actualizarCantidadProducto(999L, 10);
    }
}
