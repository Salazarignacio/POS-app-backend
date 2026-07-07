package com.mycompany.producto.api.dao;

import com.mycompany.producto.api.model.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ProveedorDAO {

    @Autowired
    private DataSource dataSource;

    public void crear(Proveedor entity) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("El proveedor no puede ser null");
        }
        String sql = "INSERT INTO proveedor (nombre) VALUES (?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNombre());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas < 1) {
                throw new SQLException("No se pudo crear el proveedor");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long idGenerado = rs.getLong(1);
                    entity.setId(idGenerado);
                } else {
                    throw new SQLException("El proveedor fue creado pero no se pudo obtener el ID generado");
                }
            } catch (SQLException e) {
                throw new SQLException("Error al crear el proveedor en la base de datos", e);
            }
        }
    }

    public Proveedor leer(Integer id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para leer proveedor");
        }
        String sql = "SELECT * FROM proveedor WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Proveedor prov = new Proveedor(rs.getString("nombre"));

                    prov.setId(rs.getLong("id"));
                    return prov;
                } else {
                    throw new SQLException("No se encontró proveedor con id " + id);
                }
            }
        }

    }

    public ArrayList<Proveedor> leerTodos() throws Exception {
        ArrayList<Proveedor> proveedores = new ArrayList();
        String sql = "SELECT * FROM proveedor";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Proveedor prov = new Proveedor(rs.getString("nombre"));
                prov.setId(rs.getLong("id"));
                proveedores.add(prov);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al leer la lista de proveedores: " + e.getMessage(), e);
        }
        return proveedores;
    }

    public void eliminar(Integer id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para eliminar proveedor");
        }
        String updateProductsSql = "UPDATE producto SET proveedor_id = NULL WHERE proveedor_id = ?";
        String sql = "DELETE FROM proveedor WHERE ID = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateProductsSql)) {
                    updateStmt.setInt(1, id);
                    updateStmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    int filasAfectadas = stmt.executeUpdate();
                    if (filasAfectadas == 0) {
                        throw new SQLException("ID no encontrado " + id);
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el proveedor con id " + id, e);
        }
    }


    public void actualizar(Integer id, Proveedor entity) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido para actualizar producto");
        }
        if (entity == null) {
            throw new IllegalArgumentException("El proveedor a actualizar no puede ser null");
        }
        String sql = "UPDATE proveedor " + "SET nombre = ? " + "WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entity.getNombre());
            stmt.setLong(2, id);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("ID no encontrado" + id);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el proveedor con id " + id, e);
        }
    }
}
