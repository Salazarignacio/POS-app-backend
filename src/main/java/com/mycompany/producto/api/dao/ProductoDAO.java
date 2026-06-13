package com.mycompany.producto.api.dao;

import com.mycompany.producto.api.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProductoDAO implements GenericDAO<Producto> {

    @Autowired
    private DataSource dataSource;

    @Override
    public void crear(Producto entity) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        String sql = "INSERT INTO producto (articulo, categoria, precio, stock, codigo) VALUES (?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getArticulo());
            stmt.setString(2, entity.getCategoria());
            
            if (entity.getPrecio() != null) {
                stmt.setDouble(3, entity.getPrecio());
            } else {
                stmt.setDouble(3, 0.0);
            }
            
            if (entity.getStock() != null) {
                stmt.setInt(4, entity.getStock());
            } else {
                stmt.setInt(4, 0);
            }
            
            stmt.setString(5, entity.getCodigo());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas < 1) {
                throw new SQLException("No se pudo crear el producto");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long idGenerado = rs.getLong(1);
                    entity.setId(idGenerado);
                } else {
                    throw new SQLException("El producto fue creado pero no se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el producto en la base de datos", e);
        }
    }

    @Override
    public Producto leer(Integer id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para leer producto");
        }
        String sql = "SELECT * FROM producto WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Producto prod = new Producto(
                            rs.getString("articulo"),
                            rs.getString("categoria"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("codigo")
                    );

                    prod.setId(rs.getLong("id"));
                    return prod;
                } else {
                    throw new SQLException("No se encontró producto con id " + id);
                }
            }
        }
    }

    @Override
    public ArrayList<Producto> leerTodos() throws Exception {
        ArrayList<Producto> productos = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id DESC LIMIT 20";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto prod = new Producto(
                        rs.getString("articulo"),
                        rs.getString("categoria"),
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        rs.getString("codigo")
                );
                prod.setId(rs.getLong("id"));
                productos.add(prod);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al leer la lista de productos: " + e.getMessage(), e);
        }
        return productos;
    }

    @Override
    public void actualizar(Integer id, Producto entity) throws Exception {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para actualizar producto");
        }

        if (entity == null) {
            throw new IllegalArgumentException("El producto a actualizar no puede ser null");
        }

        // Si hay un porcentaje, calculamos el nuevo precio y lo redondeamos en Java
        // para asegurar que el valor sea limpio (múltiplo de 50) y sin decimales.
        if (entity.getPorcentaje() != null) {
            Producto productoActual = leer(id);
            if (productoActual.getPrecio() != null) {
                double nuevoPrecio = productoActual.getPrecio() * (1 + entity.getPorcentaje() / 100.0);
                // Redondeo al múltiplo de 50 más cercano y eliminamos decimales
                double precioRedondeado = Math.round(nuevoPrecio / 50.0) * 50.0;
                entity.setPrecio((double) (int) precioRedondeado);
                entity.setPorcentaje(null); // Limpiamos para usar el SQL estándar de precio fijo
            }
        }

        String sql = "UPDATE producto SET articulo = ?, categoria = ?, "
                + "precio = ?, stock = ?, codigo = ? "
                + "WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getArticulo());
            stmt.setString(2, entity.getCategoria());

            if (entity.getPrecio() != null) {
                stmt.setDouble(3, entity.getPrecio());
            } else {
                stmt.setNull(3, java.sql.Types.DOUBLE);
            }

            if (entity.getStock() != null) {
                stmt.setInt(4, entity.getStock());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            stmt.setString(5, entity.getCodigo());
            stmt.setLong(6, id);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("ID no encontrado " + id);
            }

        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el producto con id " + id, e);
        }
    }

    @Override
    public void eliminar(Integer id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para eliminar producto");
        }
        String sql = "DELETE FROM producto WHERE ID = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("ID no encontrado " + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el producto con id " + id, e);
        }
    }

    public List<Producto> leerCodigo(String codigo) throws Exception {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Codigo inválido para leer producto");
        }
        
        String busqueda = "%" + codigo.trim() + "%";
        String sql = """
            SELECT * FROM producto 
            WHERE codigo LIKE ? 
               OR articulo LIKE ? 
               OR categoria LIKE ?
            ORDER BY LENGTH(codigo) ASC, articulo ASC
            LIMIT 100
        """;

        List<Producto> productos = new ArrayList<>();

        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, busqueda);
            stmt.setString(2, busqueda);
            stmt.setString(3, busqueda);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto prod = new Producto(
                            rs.getString("articulo"),
                            rs.getString("categoria"),
                            rs.getDouble("precio"),
                            rs.getInt("stock"),
                            rs.getString("codigo")
                    );

                    prod.setId(rs.getLong("id"));
                    productos.add(prod);
                }
            }
            return productos;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar productos por código: " + codigo, e);
        }
    }

}
