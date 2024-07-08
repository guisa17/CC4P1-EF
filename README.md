# Examen Final - Programación Concurrente y Distribuida

- Pisfil Puicon Angello Jamir
- Rivas Galindo Hugo Rodrigo
- Salcedo Alvarez Guillermo Ronie

## Ejecución local

Para la compilación de todos los archivos Java haremos

```
javac -d build/classes src/main/java/*.java
```

Para la ejecución del servidor líder
```
java -cp build/classes src.main.java.ServidorLider
```

Para la ejecución del servidor seguidor (en otro terminal)
```
java -cp build/classes src.main.java.ServidorSeguidor
```

Para la ejecución del cliente (en otro terminal)
```
java -cp build/classes src.main.java.ServidorCliente
```


Para la ejecución de los clientes en otros lenguajes de programación (Go, Python y JavaScript)
```
cd src/clients
go run cliente.go
python cliente.py
node cliente.js
```

## Ejecución Android
Para la ejecución de la aplicación Android basta con clonar el proyecto y ejecutarlo desde AndroidStudio en emulador o un dispositivo Android conectado.
https://github.com/guisa17/CC4P1-EF-Android

### Nota
Asegurarse que los servidores y clientes se encuentren en la misma red y que las direcciones IP sean correctas.
