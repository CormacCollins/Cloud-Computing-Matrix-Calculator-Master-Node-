import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;


public class MatrixServer {
	protected Socket socket;
	private static DataInputStream dis = null;
	private static int size;
	private static MatrixResult res;
	private static String op;
	//public static ArrayList<CalculationThread> threadList = new ArrayList<CalculationThread>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = 1024;
		int socketPort = 1000;
		int count = 0;
		int workerCount = 1;
		int []socketList = new int[10000];
		int socketIndex = 0;
		if (args.length == 2) {
			try {
				socketPort = Integer.parseInt(args[0]);
				workerCount = Integer.parseInt(args[1]);
			}
			catch(Exception e){
			}
		}
		else {
			//System.out.println("Default port: " + port + " and deafult workerCount " + workerCount);
		}
		
		
		System.out.println("Matrix server is running on port " + port + "...");
		//System.out.println("Requesting " + workerCount + " workers");
		// create a server socket and wait for client's connection
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				
				Socket socket = serverSocket.accept();
				//System.out.println("Socket number " + count + " open.");
				
						
				//create new server to communicate permanently with client				
				MatrixServer matrixServer = new MatrixServer();
				matrixServer.setSocket(socket);	
				dis = new DataInputStream(socket.getInputStream());
				size = dis.readInt();
				op = dis.readUTF();
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(res);

				//ThreadManager calculationThread = new ThreadManager(socket, count, workerCount);
				//calculationThread.start();
				matrixServer.setSocket(socket);				

				//ThreadManager calculationThread = new ThreadManager(socket, count, workerCount);
				//calculationThread.start();		
				count++;

			}
		} catch (IOException e) {
			// Server failed
			e.printStackTrace();
			
		}
		
	}
	
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	/*
=======
	
>>>>>>> 413d0570f947d842fc40aa8b728882710285d587
	public void execute() {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			// read the message from client and parse the execution
			String line = reader.readLine();


			// write the result back to the client
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			
			// ---- WRITE BACK OUTPUT ---- //
			
			writer.write("Recieved: " + line + "Giving back the result....");
			writer.newLine();
			writer.write("Here is the result");
//			writer.write(""+result);
			writer.newLine();
			writer.flush();
//			
			
			
			
			// close the stream
			reader.close();
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
<<<<<<< HEAD
*/
}

