package com.mycompany.producto.api.service;

import com.mycompany.producto.api.dao.ProductoDAO;
import com.mycompany.producto.api.model.Producto;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProductoService implements GenericService<Producto> {

    @Autowired
    private final ProductoDAO prodDAO;

    public ProductoService(ProductoDAO prodDAO) {
        this.prodDAO = prodDAO;
    }

    @Override
    public void save(Producto entity) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }

        // Validaciones obligatorias
        if (entity.getCodigo() == null || entity.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (entity.getArticulo() == null || entity.getArticulo().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre (artículo) del producto es obligatorio");
        }

        // Valores por defecto para campos opcionales
        if (entity.getCategoria() == null) {
            entity.setCategoria("");
        }
        if (entity.getPrecio() == null) {
            entity.setPrecio(0.0);
        }
        if (entity.getStock() == null) {
            entity.setStock(0);
        }

        System.out.println("Intentando guardar producto: " + entity);

        if (entity.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }

        try {
            prodDAO.crear(entity);
        } catch (SQLException e) {
            System.err.println("Error SQL al crear producto: " + e.getMessage());
            throw new RuntimeException("No se pudo crear el producto", e);
        }
    }

    @Override
    public Producto findById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        try {
            return prodDAO.leer(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el producto con id " + id, e);
        }
    }

    @Override
    public List<Producto> findAll() throws Exception {
        try {
            return prodDAO.leerTodos();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener la lista de productos", e);
        }
    }

    @Override
    public void update(int id, Producto entity) throws Exception {
        System.out.println("a verr");
        if (entity == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }

        try {
            prodDAO.actualizar(id, entity);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el producto con id " + id, e);
        }
    }

    @Override
    public void delete(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        try {
            prodDAO.eliminar(id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el producto con id " + id, e);
        }
    }

    public List<Producto> findByCode(String code) throws Exception {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de producto inválido");
        }

        try {
            return prodDAO.leerCodigo(code.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el producto con código " + code, e);
        }
    }
}
