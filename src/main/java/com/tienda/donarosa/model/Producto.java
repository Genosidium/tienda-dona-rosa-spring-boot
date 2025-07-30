package com.tienda.donarosa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, unique = true)
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de precio inválido")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "cantidad_inicial")
    private Integer cantidadInicial;

    // Constructor vacío
    public Producto() {}

    // Constructor con parámetros
    public Producto(String nombre, BigDecimal precio, Integer cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.cantidadInicial = cantidad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(Integer cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    // Métodos de negocio
    public BigDecimal getValorTotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public boolean estaCercaDeAgotarse() {
        if (cantidadInicial == null || cantidadInicial == 0) {
            return cantidad <= 1;
        }
        return cantidad <= (cantidadInicial * 0.1);
    }

    public double getPorcentajeRestante() {
        if (cantidadInicial == null || cantidadInicial == 0) {
            return 0.0;
        }
        return ((double) cantidad / cantidadInicial) * 100;
    }

    @PrePersist
    public void prePersist() {
        if (cantidadInicial == null) {
            cantidadInicial = cantidad;
        }
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", cantidad=" + cantidad +
                ", cantidadInicial=" + cantidadInicial +
                '}';
    }
}
