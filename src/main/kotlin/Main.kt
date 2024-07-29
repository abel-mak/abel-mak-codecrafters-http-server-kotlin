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

fun readFile(file: File): String? {
    var result = "";
    if (file.exists()) {
       // val lines = file.readLines();
       // for (line in lines) {
       //    result += line; 
       // }
        return file.readText();
    }
    return null;
    
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
                    val fileContent = readFile(File(filePath))
                        if (fileContent != null) {
                            okResponse(outputStream, fileContent);
                        }
                        else {
                            notFoundResponse(outputStream); 
                        }
                }

            }
            else {
                notFoundResponse(outputStream); 
            }
            outputStream.close()
                //socket.close();
        }
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
