const net = require('net');
const fs = require('fs');
const readline = require('readline');

// Dirección y puerto del servidor líder
const SERVER_IP = '192.168.18.48'; // Cambiar por la IP del servidor líder en la LAN
const SERVER_PORT = 8877;

// Conectar al servidor
const client = new net.Socket();
client.connect(SERVER_PORT, SERVER_IP, () => {
    console.log('Conectado al servidor líder. Escribe el nombre del archivo:');

    // Nombre del archivo predeterminado (modificar según la ubicación del archivo)
    const fileName = 'C:\\Users\\guisa\\Desktop\\Concurrente-EF\\src\\main\\res\\input.txt';

    // Verificar que el archivo existe
    if (!fs.existsSync(fileName)) {
        console.error('El archivo no existe o no es un archivo válido.');
        client.end();
        return;
    }

    // Crear interfaz para leer desde la consola
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    rl.question('Selecciona el tipo de búsqueda:\n1: Contar ocurrencias de una palabra\n2: Verificar si una palabra existe\n3: Buscar palabras clave repetidas n veces\n', (tipoBusqueda) => {
        rl.question('Escribe la palabra clave para buscar: ', (palabraClave) => {
            let parametroExtra = '';
            if (tipoBusqueda === '3') {
                rl.question('Escribe el número de repeticiones deseadas: ', (param) => {
                    parametroExtra = param;
                    sendSearchRequest(tipoBusqueda, palabraClave, parametroExtra);
                    rl.close();
                });
            } else {
                sendSearchRequest(tipoBusqueda, palabraClave, parametroExtra);
                rl.close();
            }
        });
    });
});

function sendSearchRequest(tipoBusqueda, palabraClave, parametroExtra) {
    // Enviar la configuración de búsqueda al servidor líder
    client.write(tipoBusqueda + '\n');
    client.write(palabraClave + '\n');
    client.write(parametroExtra + '\n');

    // Leer y enviar el contenido del archivo
    const fileStream = fs.createReadStream('C:\\Users\\guisa\\Desktop\\Concurrente-EF\\src\\main\\res\\input.txt');
    fileStream.on('data', (chunk) => {
        client.write(chunk);
    });

    fileStream.on('end', () => {
        client.write('\n'); // Enviar una línea vacía para indicar el final del archivo

        // Leer respuesta del servidor líder
        console.log('Respuesta del servidor líder:');
        client.on('data', (data) => {
            console.log(data.toString());
        });

        client.on('close', () => {
            console.log('Conexión cerrada');
        });
    });
}

client.on('error', (err) => {
    console.error('Error en la comunicación con el servidor líder:', err.message);
});
