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
        String sql = "INSERT INTO producto (articulo, categoria, precio, stock, codigo, proveedor_id) VALUES (?,?,?,?,?,?)";

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

            if (entity.getProveedorId() != null) {
                stmt.setLong(6, entity.getProveedorId());
            } else {
                stmt.setNull(6, java.sql.Types.BIGINT);
            }

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
        String sql = "SELECT p.*, prov.nombre AS proveedor_nombre FROM producto p LEFT JOIN proveedor prov ON p.proveedor_id = prov.id WHERE p.id = ?";

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
                    
                    long provId = rs.getLong("proveedor_id");
                    if (!rs.wasNull()) {
                        prod.setProveedorId(provId);
                        prod.setProveedorNombre(rs.getString("proveedor_nombre"));
                    }
                    
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
        String sql = "SELECT p.*, prov.nombre AS proveedor_nombre FROM producto p LEFT JOIN proveedor prov ON p.proveedor_id = prov.id ORDER BY p.id DESC LIMIT 20";
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
                
                long provId = rs.getLong("proveedor_id");
                if (!rs.wasNull()) {
                    prod.setProveedorId(provId);
                    prod.setProveedorNombre(rs.getString("proveedor_nombre"));
                }
                
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
                + "precio = ?, stock = ?, codigo = ?, proveedor_id = ? "
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
            
            if (entity.getProveedorId() != null) {
                stmt.setLong(6, entity.getProveedorId());
            } else {
                stmt.setNull(6, java.sql.Types.BIGINT);
            }
            
            stmt.setLong(7, id);

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
            SELECT p.*, prov.nombre AS proveedor_nombre 
            FROM producto p 
            LEFT JOIN proveedor prov ON p.proveedor_id = prov.id
            WHERE p.codigo LIKE ? 
               OR p.articulo LIKE ? 
               OR p.categoria LIKE ?
            ORDER BY LENGTH(p.codigo) ASC, p.articulo ASC
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
                    
                    long provId = rs.getLong("proveedor_id");
                    if (!rs.wasNull()) {
                        prod.setProveedorId(provId);
                        prod.setProveedorNombre(rs.getString("proveedor_nombre"));
                    }
                    
                    productos.add(prod);
                }
            }
            return productos;
        } catch (SQLException e) {
            throw new SQLException("Error al buscar productos por código: " + codigo, e);
        }
    }

    public List<Producto> filtrar(String articulo, String categoria, String codigo, Long proveedorId) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT p.*, prov.nombre AS proveedor_nombre FROM producto p LEFT JOIN proveedor prov ON p.proveedor_id = prov.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (articulo != null && !articulo.trim().isEmpty()) {
            sql.append(" AND p.articulo LIKE ?");
            params.add("%" + articulo.trim() + "%");
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            sql.append(" AND p.categoria LIKE ?");
            params.add("%" + categoria.trim() + "%");
        }
        if (codigo != null && !codigo.trim().isEmpty()) {
            sql.append(" AND p.codigo LIKE ?");
            params.add("%" + codigo.trim() + "%");
        }
        if (proveedorId != null) {
            sql.append(" AND p.proveedor_id = ?");
            params.add(proveedorId);
        }
        
        sql.append(" ORDER BY p.id DESC LIMIT 100");
        
        List<Producto> productos = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof String) {
                    stmt.setString(i + 1, (String) val);
                } else if (val instanceof Long) {
                    stmt.setLong(i + 1, (Long) val);
                } else {
                    stmt.setObject(i + 1, val);
                }
            }
            
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
                    
                    long provId = rs.getLong("proveedor_id");
                    if (!rs.wasNull()) {
                        prod.setProveedorId(provId);
                        prod.setProveedorNombre(rs.getString("proveedor_nombre"));
                    }
                    
                    productos.add(prod);
                }
            }
            return productos;
        } catch (SQLException e) {
            throw new SQLException("Error al filtrar productos: " + e.getMessage(), e);
        }
    }

}

