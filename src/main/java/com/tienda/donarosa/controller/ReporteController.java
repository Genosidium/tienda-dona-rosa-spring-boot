package com.tienda.donarosa.controller;

import com.tienda.donarosa.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ProductoService productoService;

    @Autowired
    public ReporteController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Reporte de inventario general
     */
    @GetMapping("/inventario")
    public String reporteInventario(Model model) {
        model.addAttribute("productos", productoService.obtenerTodosLosProductos());
        model.addAttribute("valorTotal", productoService.calcularValorTotalInventario());
        model.addAttribute("productosDisponibles", productoService.obtenerProductosDisponibles());
        model.addAttribute("productosAgotados", productoService.obtenerProductosAgotados());
        model.addAttribute("productosCercaDeAgotarse", productoService.obtenerProductosCercaDeAgotarse());

        return "reportes/inventario";
    }

    /**
     * Reporte de productos cerca de agotarse
     */
    @GetMapping("/agotandose")
    public String reporteProductosAgotandose(Model model) {
        model.addAttribute("productosAgotandose", productoService.obtenerProductosCercaDeAgotarse());
        return "reportes/agotandose";
    }

    /**
     * Reporte de productos agotados
     */
    @GetMapping("/agotados")
    public String reporteProductosAgotados(Model model) {
        model.addAttribute("productosAgotados", productoService.obtenerProductosAgotados());
        return "reportes/agotados";
    }

    /**
     * Redirect para "reporte completo" desde el index
     */
    @GetMapping("/completo")
    public String reporteCompleto() {
        return "redirect:/reportes/inventario";
    }

    /**
     * Redirect para "productos críticos" desde el index
     */
    @GetMapping("/criticos")
    public String reporteCriticos() {
        return "redirect:/reportes/agotandose";
    }
}
