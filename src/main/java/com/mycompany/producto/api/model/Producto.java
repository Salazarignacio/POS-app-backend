package com.mycompany.producto.api.model;

public class Producto {

    private Long id;
    private String codigo;
    private String articulo;
    private String categoria;
    private Double precio;
    private Integer stock;
    private Double porcentaje;
    private Long proveedorId;
    private String proveedorNombre;

    public Producto() {
    }

    public Producto(String articulo, String categoria, Double precio, Integer stock, String codigo) {
        this.codigo = codigo;
        this.articulo = articulo;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.porcentaje = null;
    }

    public Producto(String articulo, String categoria, double precio, Integer stock, String codigo, Double porcentaje) {
        this.codigo = codigo;
        this.articulo = articulo;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.porcentaje = porcentaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public Double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Long getId() {
        return id;
    }

    public String getArticulo() {
        return articulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public Double getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setArticulo(String articulo) {
        this.articulo = articulo;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    @Override
    public String toString() {
        return "Producto{" + "id=" + id + ", codigo=" + codigo + ", articulo=" + articulo + ", categoria=" + categoria + ", precio=" + precio + ", stock=" + stock + ", proveedorId=" + proveedorId + ", proveedorNombre=" + proveedorNombre + '}';
    }
}
