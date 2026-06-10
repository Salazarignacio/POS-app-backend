package com.mycompany.producto.api.controller;

import com.mycompany.producto.api.model.Producto;
import com.mycompany.producto.api.service.ProductoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService pService;

    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto) throws Exception {
        try {
            pService.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(producto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el producto: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Producto>> leerProductos() throws Exception {
        return ResponseEntity.ok(pService.findAll());
    }

    @GetMapping("/test")
    public String testeando() throws Exception {
        return "Testeando";
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> leerProducto(@PathVariable int id) throws Exception {
        try {
            return ResponseEntity.ok(pService.findById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> ActualizarProducto(@PathVariable int id, @RequestBody Producto producto) throws Exception {
        try {
            pService.update(id, producto);
            return ResponseEntity.ok("Producto actualizado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se pudo actualizar el producto");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> EliminarProducto(@PathVariable int id) throws Exception {
        try {
            pService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/codigo/{code}")
    public ResponseEntity<?> leerCodigo(@PathVariable String code) throws Exception {
        try {
            return ResponseEntity.ok(pService.findByCode(code));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Producto no encontrado");
        }
    }
}
