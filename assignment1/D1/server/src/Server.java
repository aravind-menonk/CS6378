import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server{
	static int numClients;
	static Semaphore sem;
	
	public static void main(String[] args) throws IOException{
		try{
			ServerSocket serverSocket = new ServerSocket(5056);
			//variable to make sure only 2 clients are able to connect
			numClients = 0;
			//Semaphore to make the file access to append in sequence, ie. only one thread is able to append the file at a time
			sem = new Semaphore(1);	
			while(numClients < 2){
				acceptClientReq(serverSocket);
			}
			serverSocket.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void acceptClientReq(ServerSocket serverSocket) {
		Socket s = null;
		try{
			s = serverSocket.accept();
			
			System.out.println("Recieved connection : " + s);
			DataInputStream input = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());

			System.out.println("Assigned thread for the client.");
			//One thread corresponding to each client
			Thread t = new ClientHandler(s, input, out, sem);
			numClients++;	
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
