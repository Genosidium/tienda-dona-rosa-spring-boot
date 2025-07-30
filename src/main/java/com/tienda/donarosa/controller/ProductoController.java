package com.tienda.donarosa.controller;

import com.tienda.donarosa.model.Producto;
import com.tienda.donarosa.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Página principal - Lista de productos
     */
    @GetMapping("/")
    public String index(Model model) {
        try {
            List<Producto> productos = productoService.obtenerTodosLosProductos();
            model.addAttribute("productos", productos);
            model.addAttribute("valorTotal", productoService.calcularValorTotalInventario());

            Optional<Producto> productoAgotandose = productoService.obtenerProductoMasCercaDeAgotarse();
            if (productoAgotandose.isPresent()) {
                model.addAttribute("productoAgotandose", productoAgotandose.get());
            }

            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
            return "index";
        }
    }

    /**
     * Mostrar lista de productos
     */
    @GetMapping("/productos")
    public String listarProductos(Model model, @RequestParam(required = false) String buscar) {
        if (buscar != null && !buscar.trim().isEmpty()) {
            model.addAttribute("productos", productoService.buscarProductosPorNombre(buscar));
            model.addAttribute("busqueda", buscar);
        } else {
            model.addAttribute("productos", productoService.obtenerTodosLosProductos());
        }
        return "productos/lista";
    }

    /**
     * Mostrar formulario para nuevo producto
     */
    @GetMapping("/productos/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("accion", "Agregar");
        return "productos/formulario";
    }

    /**
     * Procesar el formulario de nuevo producto
     */
    @PostMapping("/productos/nuevo")
    public String guardarNuevoProducto(@Valid @ModelAttribute("producto") Producto producto,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        // Validar que no existe otro producto con el mismo nombre
        if (productoService.existeProductoConNombre(producto.getNombre())) {
            result.rejectValue("nombre", "error.producto", "Ya existe un producto con este nombre");
        }

        if (result.hasErrors()) {
            model.addAttribute("accion", "Agregar");
            return "productos/formulario";
        }

        try {
            productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al agregar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/productos";
    }

    /**
     * Mostrar formulario para editar producto
     */
    @GetMapping("/productos/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);

        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("accion", "Editar");
            return "productos/formulario";
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/productos";
        }
    }

    /**
     * Procesar el formulario de edición
     */
    @PostMapping("/productos/editar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @Valid @ModelAttribute("producto") Producto producto,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        producto.setId(id);

        // Validar que no existe otro producto con el mismo nombre (excepto el actual)
        Optional<Producto> productoExistente = productoService.obtenerProductoPorId(id);
        if (productoExistente.isPresent()) {
            String nombreOriginal = productoExistente.get().getNombre();
            if (!nombreOriginal.equalsIgnoreCase(producto.getNombre()) &&
                    productoService.existeProductoConNombre(producto.getNombre())) {
                result.rejectValue("nombre", "error.producto", "Ya existe un producto con este nombre");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("accion", "Editar");
            return "productos/formulario";
        }

        try {
            productoService.actualizarProducto(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/productos";
    }

    /**
     * Eliminar producto
     */
    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Producto> producto = productoService.obtenerProductoPorId(id);
            if (producto.isPresent()) {
                productoService.eliminarProducto(id);
                redirectAttributes.addFlashAttribute("mensaje",
                        "Producto '" + producto.get().getNombre() + "' eliminado exitosamente");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Producto no encontrado");
                redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/productos";
    }

    /**
     * Insertar datos iniciales de la tienda de Doña Rosa
     */
    @GetMapping("/productos/datos-iniciales")
    public String insertarDatosIniciales(RedirectAttributes redirectAttributes) {
        try {
            // Verificar si ya hay datos
            if (!productoService.obtenerTodosLosProductos().isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Los datos iniciales ya están cargados");
                redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
                return "redirect:/";
            }

            // Crear productos iniciales de la tienda de Doña Rosa
            productoService.guardarProducto(new Producto("Peras", new BigDecimal("4000.00"), 65));
            productoService.guardarProducto(new Producto("Limones", new BigDecimal("1500.00"), 25));
            productoService.guardarProducto(new Producto("Moras", new BigDecimal("2000.00"), 30));
            productoService.guardarProducto(new Producto("Piñas", new BigDecimal("3000.00"), 15));
            productoService.guardarProducto(new Producto("Tomates", new BigDecimal("1000.00"), 30));
            productoService.guardarProducto(new Producto("Fresas", new BigDecimal("3000.00"), 12));
            productoService.guardarProducto(new Producto("Frunas", new BigDecimal("300.00"), 50));
            productoService.guardarProducto(new Producto("Galletas", new BigDecimal("500.00"), 400));
            productoService.guardarProducto(new Producto("Chocolates", new BigDecimal("1200.00"), 500));
            productoService.guardarProducto(new Producto("Arroz", new BigDecimal("1200.00"), 60));

            redirectAttributes.addFlashAttribute("mensaje", "¡Inventario inicial cargado exitosamente! Se agregaron 10 productos base de la tienda");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cargar datos iniciales: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/";
    }

    /**
     * Actualizar cantidad de producto (para ventas)
     */
    @PostMapping("/productos/actualizar-cantidad/{id}")
    @ResponseBody
    public String actualizarCantidad(@PathVariable Long id, @RequestParam Integer cantidad) {
        try {
            if (cantidad < 0) {
                return "La cantidad no puede ser negativa";
            }

            boolean actualizado = productoService.actualizarCantidadProducto(id, cantidad);
            if (actualizado) {
                return "Cantidad actualizada exitosamente";
            } else {
                return "Producto no encontrado";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}



