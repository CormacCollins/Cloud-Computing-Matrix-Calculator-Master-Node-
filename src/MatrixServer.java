import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Executable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Timer;

enum partition_type {row_column, row_full, data_split };

class JobInfo {
	public int workerCount;
	public int matrixSize;
	public partition_type partitionType;
}



public class MatrixServer {
	protected Socket socket;
	public static ArrayList<CalculationThread> threadList = new ArrayList<CalculationThread>();
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = 1024;
		int count = 0;
		if (args.length == 1) {
			try {
				port = Integer.parseInt(args[0]);
			}
			catch(Exception e){
			}
		}
		System.out.println("Matrix server is running on port " + port + "...");
		// create a server socket and wait for client's connection
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
	        System.out.println("Server Started");
			while(true) {

				Socket socket = serverSocket.accept();
				System.out.println("Socket number " + count + " open.");
				
						
				//create new server to communicate permanently with client				
				MatrixServer matrixServer = new MatrixServer();
				matrixServer.setSocket(socket);				
			
				CalculationThread calculationThread = new CalculationThread(socket, count);
				calculationThread.start();
				
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
	//TODO implement
	public JobInfo workAmount(String[] args) {
		return new JobInfo();
	}
	
	
	

}
