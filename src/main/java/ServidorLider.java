package src.main.java;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ServidorLider {

    private static List<SeguidorInfo> seguidores = new CopyOnWriteArrayList<>();
    private static final int PUERTO_SERVIDOR = 8877;
    private static final int INTERVALO_PULSACION = 2000; // 2 segundos

    private static class SeguidorInfo {
        Socket socket;
        int numero;
        @SuppressWarnings("unused")
        boolean activo;

        SeguidorInfo(Socket socket, int numero) {
            this.socket = socket;
            this.numero = numero;
            this.activo = true;
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO_SERVIDOR, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Servidor líder iniciado y esperando conexión de seguidores...");

            // Iniciar el hilo de pulsaciones
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(ServidorLider::enviarPulsaciones, 0, INTERVALO_PULSACION, TimeUnit.MILLISECONDS);

            // Conectando seguidores (asumimos 2 seguidores para el ejemplo)
            for (int i = 0; i < 2; i++) {
                Socket seguidorSocket = serverSocket.accept();
                seguidores.add(new SeguidorInfo(seguidorSocket, i + 1));
                System.out.println("Seguidor " + (i + 1) + " conectado: " + seguidorSocket.getInetAddress());
            }

            System.out.println("Esperando conexión de cliente...");

            while (true) {
                try (Socket clienteSocket = serverSocket.accept();
                     BufferedReader input = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
                     PrintWriter output = new PrintWriter(clienteSocket.getOutputStream(), true)) {

                    System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                    // Recibir configuración de búsqueda del cliente
                    String tipoBusqueda = input.readLine();
                    String palabraClave = input.readLine();
                    String parametroExtra = input.readLine();

                    // Recibir archivo del cliente
                    String linea;
                    List<String> lineasArchivo = new ArrayList<>();
                    while ((linea = input.readLine()) != null && !linea.isEmpty()) {
                        lineasArchivo.add(linea);
                    }

                    // Dividir archivo entre seguidores
                    int numSeguidores = seguidores.size();
                    int numLineas = lineasArchivo.size();
                    int lineasPorSeguidor = (numLineas + numSeguidores - 1) / numSeguidores; // Para redondear hacia arriba

                    for (int i = 0; i < numSeguidores; i++) {
                        SeguidorInfo seguidorInfo = seguidores.get(i);
                        Socket seguidorSocket = seguidorInfo.socket;
                        PrintWriter seguidorOutput = new PrintWriter(seguidorSocket.getOutputStream(), true);

                        int inicio = i * lineasPorSeguidor;
                        int fin = Math.min(inicio + lineasPorSeguidor, numLineas); // Para evitar exceder el límite

                        seguidorOutput.println(tipoBusqueda); // Enviar el tipo de búsqueda
                        seguidorOutput.println(palabraClave); // Enviar la palabra clave
                        seguidorOutput.println(parametroExtra); // Enviar el parámetro extra (si es necesario)
                        seguidorOutput.println(fin - inicio); // Número de líneas enviadas a este seguidor
                        for (int j = inicio; j < fin; j++) {
                            seguidorOutput.println(lineasArchivo.get(j));
                        }
                    }

                    // Recibir resultados de los seguidores y enviarlos al cliente
                    if ("1".equals(tipoBusqueda)) {
                        // Contar ocurrencias
                        int totalCount = 0;
                        for (SeguidorInfo seguidorInfo : seguidores) {
                            Socket seguidorSocket = seguidorInfo.socket;
                            BufferedReader seguidorInput = new BufferedReader(new InputStreamReader(seguidorSocket.getInputStream()));

                            totalCount += Integer.parseInt(seguidorInput.readLine());
                        }
                        output.println("La palabra clave '" + palabraClave + "' apareció " + totalCount + " veces en el archivo.");
                    } else if ("2".equals(tipoBusqueda)) {
                        // Verificar existencia
                        boolean found = false;
                        for (SeguidorInfo seguidorInfo : seguidores) {
                            Socket seguidorSocket = seguidorInfo.socket;
                            BufferedReader seguidorInput = new BufferedReader(new InputStreamReader(seguidorSocket.getInputStream()));

                            if (Boolean.parseBoolean(seguidorInput.readLine())) {
                                found = true;
                            }
                        }
                        if (found) {
                            output.println("La palabra clave '" + palabraClave + "' existe en el archivo.");
                        } else {
                            output.println("La palabra clave '" + palabraClave + "' no existe en el archivo.");
                        }
                    } else if ("3".equals(tipoBusqueda)) {
                        // Buscar repeticiones
                        int totalCount = 0;
                        int n = Integer.parseInt(parametroExtra);
                        for (SeguidorInfo seguidorInfo : seguidores) {
                            Socket seguidorSocket = seguidorInfo.socket;
                            BufferedReader seguidorInput = new BufferedReader(new InputStreamReader(seguidorSocket.getInputStream()));

                            totalCount += Integer.parseInt(seguidorInput.readLine());
                        }
                        if (totalCount >= n) {
                            output.println("La palabra clave '" + palabraClave + "' aparece al menos " + n + " veces en el archivo.");
                        } else {
                            output.println("La palabra clave '" + palabraClave + "' no aparece al menos " + n + " veces en el archivo.");
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error iniciando el servidor: " + e.getMessage());
        }
    }

    private static void enviarPulsaciones() {
        StringBuilder estadoSeguidores = new StringBuilder();
        for (SeguidorInfo seguidor : seguidores) {
            try {
                PrintWriter out = new PrintWriter(seguidor.socket.getOutputStream(), true);
                out.println("PULSE");
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(seguidor.socket.getInputStream()));
                String respuesta = in.readLine();

                if ("ALIVE".equals(respuesta)) {
                    seguidor.activo = true;
                    estadoSeguidores.append("Seguidor ").append(seguidor.numero).append(" activo;");
                } else {
                    seguidor.activo = false;
                    estadoSeguidores.append("Seguidor ").append(seguidor.numero).append(" inactivo(carga asignada a otro nodo);");
                }
            } catch (IOException e) {
                seguidor.activo = false;
                estadoSeguidores.append("Seguidor ").append(seguidor.numero).append(" inactivo(carga asignada a otro nodo);");
            }
        }

        // Enviar el estado a todos los seguidores
        for (SeguidorInfo seguidor : seguidores) {
            try {
                PrintWriter out = new PrintWriter(seguidor.socket.getOutputStream(), true);
                out.println("STATUS:" + estadoSeguidores.toString());
                out.flush();
            } catch (IOException e) {
                System.err.println("Error al enviar estado al seguidor " + seguidor.numero);
            }
        }
        System.out.println(estadoSeguidores.toString());
    }
}
