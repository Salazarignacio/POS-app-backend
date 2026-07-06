-- Inicialización de la base de datos para la API de productos
CREATE TABLE IF NOT EXISTS proveedor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS producto (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    articulo VARCHAR(100) NOT NULL,
    categoria VARCHAR(50),
    precio DOUBLE NOT NULL,
    stock INT NOT NULL,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    proveedor_id BIGINT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES proveedor(id)
);