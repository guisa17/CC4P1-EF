package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

func main() {
	serverIP := "192.168.18.48" // Cambiar por la IP del servidor líder en la LAN
	port := "8877"

	conn, err := net.Dial("tcp", serverIP+":"+port)
	if err != nil {
		fmt.Println("Error al conectar con el servidor:", err)
		return
	}
	defer conn.Close()

	fmt.Println("Conectado al servidor líder. Escribe el nombre del archivo:")

	// Nombre del archivo predeterminado (modificar según la ubicación del archivo)
	fileName := "C:\\Users\\guisa\\Desktop\\Concurrente-EF\\src\\main\\res\\input.txt"

	// Verificar que el archivo existe
	if _, err := os.Stat(fileName); os.IsNotExist(err) {
		fmt.Println("El archivo no existe o no es un archivo válido.")
		return
	}

	reader := bufio.NewReader(os.Stdin)

	fmt.Println("Selecciona el tipo de búsqueda:")
	fmt.Println("1: Contar ocurrencias de una palabra")
	fmt.Println("2: Verificar si una palabra existe")
	fmt.Println("3: Buscar palabras clave repetidas n veces")
	tipoBusqueda, _ := reader.ReadString('\n')
	tipoBusqueda = tipoBusqueda[:len(tipoBusqueda)-1] // Remover el salto de línea

	fmt.Println("Escribe la palabra clave para buscar:")
	palabraClave, _ := reader.ReadString('\n')
	palabraClave = palabraClave[:len(palabraClave)-1] // Remover el salto de línea

	parametroExtra := ""
	if tipoBusqueda == "3" {
		fmt.Println("Escribe el número de repeticiones deseadas:")
		parametroExtra, _ = reader.ReadString('\n')
		parametroExtra = parametroExtra[:len(parametroExtra)-1] // Remover el salto de línea
	}

	// Enviar la configuración de búsqueda al servidor líder
	fmt.Fprintln(conn, tipoBusqueda)
	fmt.Fprintln(conn, palabraClave)
	fmt.Fprintln(conn, parametroExtra)

	// Leer y enviar el contenido del archivo
	file, err := os.Open(fileName)
	if err != nil {
		fmt.Println("Error al abrir el archivo:", err)
		return
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		fmt.Fprintln(conn, scanner.Text())
	}

	fmt.Fprintln(conn, "") // Enviar una línea vacía para indicar el final del archivo

	// Leer respuesta del servidor líder
	fmt.Println("Respuesta del servidor líder:")
	serverReader := bufio.NewReader(conn)
	for {
		response, err := serverReader.ReadString('\n')
		if err != nil {
			break
		}
		fmt.Print(response)
	}
}
