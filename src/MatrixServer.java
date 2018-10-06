
import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

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


import java.time.*;

	


public class MatrixServer {
	protected Socket socket;
	private static DataInputStream dis = null;

	private static int size;
	private static MatrixResult res;
	private static String op;
	//public static ArrayList<CalculationThread> threadList = new ArrayList<CalculationThread>();
	

	public static void main(String[] args) throws ClassNotFoundException {

	

		// TODO Auto-generated method stub
		int port = 1024;
		int socketPort = 1000;
		int count = 0;
		int workerCount = 1;
		int []socketList = new int[10000];
		int socketIndex = 0;
		String id;

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
				System.out.println("create finished ");

				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				SendWork rec = (SendWork)in.readObject();
				System.out.println("received");
				if(rec.op ==1 || rec.op ==2 || rec.op ==3) {
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					
					String key = LocalDate.now().toString() +LocalTime.now().toString();
					
					dos.writeUTF(key);
					
				}else  {
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				id = dis.readUTF();
				if (rec.op == 5) {
					//use the id to find the result 
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				out.writeObject(res);
				}else {
					//use the id to get the info
				}
				}

				


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
<<<<<<< HEAD

=======
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
>>>>>>> 20240eb748fd11ee07d022063ca9d421218a9dd7
*/
	}

