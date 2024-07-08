package src.main.java;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServidorSeguidor {
    public static void main(String[] args) {
        String serverIp = "192.168.18.48"; // Cambiar por la IP del servidor líder en la LAN
        int port = 8877;

        try (Socket socket = new Socket(serverIp, port);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Conectado al servidor líder");

            while (true) {
                String mensajeRecibido = input.readLine();

                if ("PULSE".equals(mensajeRecibido)) {
                    // Responder a la pulsación del líder
                    output.println("ALIVE");
                    output.flush();
                } else if (mensajeRecibido.startsWith("STATUS:")) {
                    // Mostrar el estado de todos los seguidores
                    System.out.println(mensajeRecibido.substring(7));
                } else {
                    // Recibir tipo de búsqueda
                    String tipoBusqueda = mensajeRecibido;
                    String palabraClave = input.readLine();
                    @SuppressWarnings("unused")
                    String parametroExtra = input.readLine();
                    int n = Integer.parseInt(input.readLine());

                    System.out.println("Tipo de búsqueda recibida: " + tipoBusqueda); // Depuración
                    System.out.println("Palabra clave recibida: " + palabraClave); // Depuración
                    System.out.println("Número de líneas a procesar: " + n); // Depuración

                    List<String> lineas = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        String linea = input.readLine();
                        lineas.add(linea);
                        System.out.println("Línea recibida: " + linea); // Depuración
                    }

                    if ("1".equals(tipoBusqueda)) {
                        // Contar ocurrencias
                        int count = 0;
                        for (String linea : lineas) {
                            count += contarOcurrencias(linea, palabraClave);
                        }
                        output.println(count);
                        System.out.println("Contadas " + count + " ocurrencias de la palabra clave '" + palabraClave + "'"); // Depuración
                    } else if ("2".equals(tipoBusqueda)) {
                        // Verificar existencia
                        boolean found = false;
                        for (String linea : lineas) {
                            if (linea.contains(palabraClave)) {
                                found = true;
                                break;
                            }
                        }
                        output.println(found);
                        System.out.println("Existencia de la palabra clave '" + palabraClave + "': " + found); // Depuración
                    } else if ("3".equals(tipoBusqueda)) {
                        // Buscar repeticiones
                        int count = 0;
                        for (String linea : lineas) {
                            count += contarOcurrencias(linea, palabraClave);
                        }
                        output.println(count);
                        System.out.println("Contadas " + count + " repeticiones de la palabra clave '" + palabraClave + "'"); // Depuración
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el servidor líder: " + e.getMessage());
        }
    }

    private static int contarOcurrencias(String texto, String palabraClave) {
        Pattern pattern = Pattern.compile(Pattern.quote(palabraClave));
        Matcher matcher = pattern.matcher(texto);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
