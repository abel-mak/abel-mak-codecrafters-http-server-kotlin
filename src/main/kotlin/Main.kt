import java.net.*;
import java.io.*;
import kotlin.concurrent.thread;

fun okResponse(outputStream: OutputStream, body: String = "") {
    val response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ${body.count()}\r\n\r\n$body"
    outputStream.write(response.toByteArray());
}

fun notFoundResponse(outputStream: OutputStream) {
   outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".toByteArray());
}

fun handleConnection(socket: Socket) {
    val inputStream = socket.getInputStream();
    var totalBytes = ByteArray(0);
    while (true) {
        val buff = ByteArray(64);
        if (inputStream.available() != 0) {
            inputStream.read(buff, 0, 64);
            totalBytes += buff
        }
        else
            break;
    }
    val requestHeaderArr = totalBytes.toString(Charsets.UTF_8).split("\r\n");
    val requestHeaderMap: MutableMap<String, String> = mutableMapOf();

    for (item in requestHeaderArr) {
        if (item.startsWith("User-Agent") == true) {
            requestHeaderMap["User-Agent"] = item.replace("User-Agent: ", "");
        }
    }
    if (requestHeaderArr.first().startsWith("GET")) {
        val reqFieldArr = requestHeaderArr.first().split(' ');
        if (reqFieldArr.count() == 3) {
            val path = reqFieldArr[1];
            val outputStream = socket.getOutputStream();
            if (path == "/") {
                outputStream.write("HTTP/1.1 200 OK\r\n\r\n".toByteArray());
            } 
            else if (path.startsWith("/echo") == true) {
                val message = path.replace("/echo/", "");

                okResponse(outputStream, message);
            }
            else if (path.startsWith("/user-agent")) {
                okResponse(outputStream, requestHeaderMap["User-Agent"] ?: "");
            }
            else {
                notFoundResponse(outputStream); 
            }
            outputStream.close()
        }
    }
}

fun main() {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    println("Logs from your program will appear here!")

        // Uncomment this block to pass the first stage
        var serverSocket = ServerSocket(4221)
        //
        // // Since the tester restarts your program quite often, setting SO_REUSEADDR
        // // ensures that we don't run into 'Address already in use' errors
        serverSocket.reuseAddress = true
        //

        while (true) {
            try {
                val socket =  serverSocket.accept();
                println("accepted new connection")
            
                thread {
                        handleConnection(socket);
                }                

            }
            catch (e: Exception) {
                println("\nexecption occured ${e.message}"); 
            }
        }

}
