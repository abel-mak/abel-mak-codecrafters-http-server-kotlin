import java.net.*;
import java.io.*;
import kotlin.concurrent.thread;
import java.nio.file.Paths;

fun okResponse(outputStream: OutputStream, body: String = "") {
    val response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ${body.count()}\r\n\r\n$body"
    outputStream.write(response.toByteArray());
}

fun notFoundResponse(outputStream: OutputStream) {
   outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".toByteArray());
}

fun badRequestResponse(outputStream: OutputStream) {
   outputStream.write("HTTP/1.1 400 Bad Request\r\n\r\n".toByteArray());
}

fun sendFileResponse(outputStream: OutputStream, file: File) {
    if (file.exists()) {
        val body = file.readText();
        val response = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ${body.count()}\r\n\r\n$body"
            outputStream.write(response.toByteArray());
    }
    else {
        notFoundResponse(outputStream);
    }
}

fun createdResponse(outputStream: OutputStream) {
    outputStream.write("HTTP/1.1 201 Created\r\n\r\n".toByteArray());
}

fun handleGetRequest(outputStream: OutputStream, path: String,
        requestHeaderMap: MutableMap<String, String>, directory: String?) {
    if (path == "/") {
        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".toByteArray());
    } 
    else if (path.startsWith("/echo/") == true) {
        val message = path.replace("/echo/", "");

        okResponse(outputStream, message);
    }
    else if (path.startsWith("/user-agent") == true) {
        okResponse(outputStream, requestHeaderMap["User-Agent"] ?: "");
    }
    else if (path.startsWith("/files/") == true) {
        if (directory == null)
            notFoundResponse(outputStream);
        else {

            val filePath: String = 
                Paths.get(directory, path.replace("/files/", "")).toString();
            sendFileResponse(outputStream, File(filePath));
        }

    }
    else {
        notFoundResponse(outputStream); 
    }
} 

fun handlePostRequest(outputStream: OutputStream, path: String, 
        directory: String?, requestBody: String) {
    if (path.startsWith("/files/") == true) {
        val filename = path.replace("/files/", "");
        if (filename != "" && directory != null) {
            File(Paths.get(directory, filename).toString()).writeText(requestBody);
            createdResponse(outputStream);
        }
    }
}

fun handleConnection(directory: String?, socket: Socket) {
    val bufferedReader: BufferedReader =
        BufferedReader(InputStreamReader(socket.getInputStream()));
    val requestHeaderArr: MutableList<String> = mutableListOf();
    while (true) {
        val input = bufferedReader.readLine();
        if (input.count() > 0) {
            requestHeaderArr.add(input)
        }
        else
            break;
    }
    var requestBody = "";
    while (true) {
        if (bufferedReader.ready() == false)
            break;
        val buff = CharArray(255);
        bufferedReader.read(buff, 0, 255);
        requestBody += String(buff);
    }
    print(requestBody);

    val requestHeaderMap: MutableMap<String, String> = mutableMapOf();

    for (item in requestHeaderArr) {
        if (!item.startsWith("GET") && !item.startsWith("POST")) {
            val (headerName, headerValue) = item.split(": ");
            requestHeaderMap[headerName] = headerValue; 
        }
    }
    val reqFieldArr = requestHeaderArr.first().split(' ');
    if (reqFieldArr.count() == 3) {
        val path = reqFieldArr[1];
        val outputStream = socket.getOutputStream();
        when(reqFieldArr.first()) {
            "GET" -> handleGetRequest(outputStream, path, requestHeaderMap, directory);
            "POST" -> handlePostRequest(outputStream, path, directory, requestBody);
            else -> {
                badRequestResponse(outputStream);
            }
        }
        outputStream.close()
            //socket.close();
    }
}

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    println("Logs from your program will appear here!")
        var directory: String? = null;

    for (i in 0 until args.count()) {
        if (args[i] == "--directory" && i + 1 < args.count()) {
            directory = args[i+1];
            break;
        }
    }
    // Uncomment this block to pass the first stage
    var serverSocket = ServerSocket(4221)
        //
        //// Since the tester restarts your program quite often, setting SO_REUSEADDR
        // // ensures that we don't run into 'Address already in use' errors
        serverSocket.reuseAddress = true
        //

        while (true) {
            try {
                val socket =  serverSocket.accept();
                println("accepted new connection")

                    thread {
                        handleConnection(directory, socket);
                    }                

            }
            catch (e: Exception) {
                println("\nexecption occured ${e.message}"); 
            }
        }

}
