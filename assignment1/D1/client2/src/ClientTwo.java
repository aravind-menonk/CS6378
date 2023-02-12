import java.io.*;
import java.net.*;

public class ClientTwo {
	private Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream out = null;

	public ClientTwo(String address, int port){
		try {
			socket = new Socket(address, port);
			System.out.println("Connected");
			input = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}catch (UnknownHostException uh) {
			uh.printStackTrace();
			return;
		}catch (IOException io) {
			io.printStackTrace();
			return;
		}

		//Base file path for Client 2
		String baseFilePath = "/home/012/a/ax/axk210275/D1/client2/";
		/*
		 * Send 100 bytes at a time to the server. 
		 * Send the length first to the server. We send 0 to signify end of message 
		 */
		try{
			byte[] message = new byte[100];
			InputStream is = new FileInputStream(baseFilePath + "F2.txt");
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
		
		/*
		 * Create the new file F3 and update it with data sent from the Server
		 * Read the length of the message sent. If it is equal to 0, it means the message is completely received
		 */
		try{
			File f3 = new File(baseFilePath + "F3.txt");
			f3.createNewFile();
			FileOutputStream os = new FileOutputStream(f3, false);
			int length = input.readInt();
			while(length > 0){
				byte[] message = new byte[200];
				input.readFully(message, 0, message.length);
				os.write(message);
				length = input.readInt();
			}
			os.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		//Close the connections
		try { 
			input.close();
			out.close();
			socket.close();
		}catch( Exception e){
			e.printStackTrace();
		}
		
	}

	public static void main(String args[]){
		ClientTwo client = new ClientTwo("10.176.69.32", 5056);
	}
}
