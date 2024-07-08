import socket

def main():
    server_ip = "192.168.18.48"  # Cambiar por la IP del servidor líder en la LAN
    port = 8877

    try:
        with socket.create_connection((server_ip, port)) as sock:
            input_stream = sock.makefile('r')
            output_stream = sock.makefile('w')

            print("Conectado al servidor líder. Escribe el nombre del archivo:")

            # Leer el nombre del archivo desde la consola (modificar según la ubicación del archivo)
            file_name = "C:\\Users\\guisa\\Desktop\\Concurrente-EF\\src\\main\\res\\input.txt"

            # Verificar que el archivo existe
            try:
                with open(file_name, 'r') as file:
                    pass
            except FileNotFoundError:
                print("El archivo no existe o no es un archivo válido.")
                return

            print("Selecciona el tipo de búsqueda:")
            print("1: Contar ocurrencias de una palabra")
            print("2: Verificar si una palabra existe")
            print("3: Buscar palabras clave repetidas n veces")
            tipo_busqueda = input()

            print("Escribe la palabra clave para buscar:")
            palabra_clave = input()

            parametro_extra = ""
            if tipo_busqueda == "3":
                print("Escribe el número de repeticiones deseadas:")
                parametro_extra = input()

            # Enviar la configuración de búsqueda al servidor líder
            output_stream.write(tipo_busqueda + '\n')
            output_stream.write(palabra_clave + '\n')
            output_stream.write(parametro_extra + '\n')
            output_stream.flush()

            # Leer y enviar el contenido del archivo
            with open(file_name, 'r') as file:
                for line in file:
                    output_stream.write(line)
                output_stream.write('\n')  # Enviar una línea vacía para indicar el final del archivo
                output_stream.flush()

            # Leer respuesta del servidor líder
            print("Respuesta del servidor líder:")
            while True:
                response = input_stream.readline()
                if not response:
                    break
                print(response.strip())

    except Exception as e:
        print("Error en la comunicación con el servidor líder:", e)

if __name__ == "__main__":
    main()
