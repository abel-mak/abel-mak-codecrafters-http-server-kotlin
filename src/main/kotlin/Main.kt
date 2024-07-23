import java.net.ServerSocket;

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
        }
        catch (e: Exception) {
            println(e.message); 
        }
    }
		
    // serverSocket.accept() // Wait for connection from client.
}
