package src.main.java;
import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) {
        String serverIp = "192.168.18.48"; // Cambiar por la IP del servidor líder en la LAN
        int port = 8877;

        try (Socket socket = new Socket(serverIp, port);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor líder. Escribe el nombre del archivo:");

            // Leer el nombre del archivo desde la consola (modificar según la ubicación del archivo)
            String fileName = "C:\\Users\\guisa\\Desktop\\Concurrente-EF\\src\\main\\res\\input.txt";

            // Obtener la ruta absoluta del archivo
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                System.err.println("El archivo no existe o no es un archivo válido.");
                return;
            }

            System.out.println("Selecciona el tipo de búsqueda:");
            System.out.println("1: Contar ocurrencias de una palabra");
            System.out.println("2: Verificar si una palabra existe");
            System.out.println("3: Buscar palabras clave repetidas n veces");
            String tipoBusqueda = consoleInput.readLine();

            System.out.println("Escribe la palabra clave para buscar:");
            String palabraClave = consoleInput.readLine();

            String parametroExtra = "";
            if ("3".equals(tipoBusqueda)) {
                System.out.println("Escribe el número de repeticiones deseadas:");
                parametroExtra = consoleInput.readLine();
            }

            // Enviar la configuración de búsqueda al servidor líder
            output.println(tipoBusqueda);
            output.println(palabraClave);
            output.println(parametroExtra);

            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine()) != null) {
                output.println(line);
            }
            fileReader.close();

            output.println(""); // Enviar una línea vacía para indicar el final del archivo

            // Leer respuesta del servidor líder
            System.out.println("Respuesta del servidor líder:");
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el servidor líder: " + e.getMessage());
        }
    }
}
