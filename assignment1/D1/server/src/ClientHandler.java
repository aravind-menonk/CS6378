import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

public class ClientHandler extends Thread{
	final DataInputStream input;
	final DataOutputStream out;
	final Socket socket;
	private Semaphore sem;

	public ClientHandler(Socket socket, DataInputStream input, DataOutputStream out, Semaphore sem){
		this.socket = socket;
		this.input = input;
		this.sem = sem;
		this.out = out;
	}

	@Override
	public void run(){
		String filePath = "/home/012/a/ax/axk210275/D1/server/F3.txt";
		try{ 
			//inorder to write the F1 first to F3, the first process will acquire the lock.
			sem.acquire();
			System.out.println("Semaphore acquired");

			//Read the length send by the clients. If it is 0, it means it has finished sending the data
			int length = input.readInt();  
			while(length > 0){
				byte[] message = new byte[100];
				input.readFully(message, 0, message.length);
				try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
					fos.write(message);
				}
				length = input.readInt();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		//Once the data is written to F3, release the lock so that the next thread of the other client can write to the file.
		sem.release();
		System.out.println("Semaphore released");
		
		File file = new File(filePath);
		while(file.length() < 600){
			//infinite loop till file becomes 600 bytes ie, both clients have finished writing to the file.
		}

		//Send F3 to the clients, 200 bytes at a time.
		byte[] message = new byte[200];
		try{	
			//Send the length first to the clients. We send 0 to signify end of message 
			 InputStream is = new FileInputStream(filePath);
			 System.out.println("Sending F3 to Clients");
			 
			 while ((is.read(message)) != -1) {
				out.writeInt(message.length);
				out.write(message);
			 }
			 out.writeInt(0);
			 out.flush();
			 is.close();

		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			System.out.println("Connection closing...");
			this.socket.close();
			System.out.println("Closed");
			this.out.close();
			this.input.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
