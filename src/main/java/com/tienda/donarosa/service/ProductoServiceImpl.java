package com.tienda.donarosa.service;

import com.tienda.donarosa.model.Producto;
import com.tienda.donarosa.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    public Producto guardarProducto(Producto producto) {
        if (producto.getCantidadInicial() == null) {
            producto.setCantidadInicial(producto.getCantidad());
        }
        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizarProducto(Producto producto) {
        if (producto.getId() == null) {
            throw new IllegalArgumentException("El producto debe tener un ID para ser actualizado");
        }

        Optional<Producto> productoExistente = productoRepository.findById(producto.getId());
        if (productoExistente.isPresent()) {
            Producto existing = productoExistente.get();
            existing.setNombre(producto.getNombre());
            existing.setPrecio(producto.getPrecio());
            existing.setCantidad(producto.getCantidad());
            // Mantener la cantidad inicial original si no se especifica
            if (producto.getCantidadInicial() != null) {
                existing.setCantidadInicial(producto.getCantidadInicial());
            }
            return productoRepository.save(existing);
        }
        throw new RuntimeException("Producto no encontrado con ID: " + producto.getId());
    }

    @Override
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarProductosPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoMasCercaDeAgotarse() {
        List<Producto> productos = productoRepository.findProductosCercaDeAgotarse();
        return productos.stream()
                .min((p1, p2) -> {
                    double porcentaje1 = p1.getPorcentajeRestante();
                    double porcentaje2 = p2.getPorcentajeRestante();
                    return Double.compare(porcentaje1, porcentaje2);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosCercaDeAgotarse() {
        return productoRepository.findProductosCercaDeAgotarse();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularValorTotalInventario() {
        Optional<Double> total = productoRepository.calcularValorTotalInventario();
        return total.map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
    }

    @Override
    public boolean actualizarCantidadProducto(Long id, Integer nuevaCantidad) {
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        Optional<Producto> productoOpt = productoRepository.findById(id);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            producto.setCantidad(nuevaCantidad);
            productoRepository.save(producto);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProductoConNombre(String nombre) {
        return productoRepository.findByNombreIgnoreCase(nombre).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosDisponibles() {
        return productoRepository.findByCantidadGreaterThan(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosAgotados() {
        return productoRepository.findByCantidadEquals(0);
    }
}
