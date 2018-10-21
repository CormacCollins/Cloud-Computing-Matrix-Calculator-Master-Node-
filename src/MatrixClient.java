import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import java.util.Scanner;




public class MatrixClient {
	private static Socket socket = null;
	//writing simple intiger for matrix size
	



	//private ObjectOutputStream out = null;
	//private String IPaddress = "xxxxxxx";
	//private String username = "xxxxxx";
	//private String password = "xxxxxxxxx";

	private static double [][] a =null;
	private static double [][] b =null;
	static MatrixResult res;
	private static ObjectOutputStream out;




	private static DataInputStream dis;




	private static ObjectInputStream in;
	
	private int id;

	private boolean localTesting = false;
	private boolean noServerResponse = false;

			
	public enum BinaryOperation{
		CANCEL, //0
		ADD,  //1
		MUTIPLY,  //2
		SUBSTRACT, //3
		STATUS, //4
		RESULT, //5
		PartialSum, //6 - Use by the calcWOrkers for when the Node's send work
		Testing, //7
		GetTestResults //8
	}
	BinaryOperation operation;
	//setup client with requested port
			
	
	//setup client with requested port
	//TODO: respond to each exception
	public MatrixClient(String hostname, int port) {		
		String s ="";
		port = 1024;
		try {
			// create a socket
			
			if(!localTesting) {
				 s = "137.116.128.225";
				socket = new Socket(s, port);
				
			}
			else {
				 s = "localhost";
				socket = new Socket(s, 1024);
			}
			System.out.println("Using host/IP: " + s + " port: " + port);
			//out = new OutputStreamWriter(socket.getOutputStream());
			//in = new ObjectInputStream(socket.getInputStream());			

		}
		catch (Exception e) {
			System.out.println("Connection not possible - nothing hosted at " + s + ", port  " + port );
			//e.printStackTrace();
			noServerResponse = true;
		}

	}


	//at this stage don't need to use 'command'
	
	
	
	

	/*
	private void setMatrixSize(int n, String command) throws IOException {
		System.out.println("Requesting size " + n);
		BufferedWriter writer = new BufferedWriter(out);
		writer.write(command + ":" + Integer.toString(n));
		writer.newLine();
		writer.flush();
	}
	*/
	//at this stage don't need to use 'command'
	
	
	
	public static  void CreateMatrix(int size) {
		a = new double[size][size];
		b = new double[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				a[i][j] = (int) (Math.random() * 10);
				b[i][j] = (int) (Math.random() * 10);
				// number in matrix is around 1-10, to get better result
			}
		}
		// print matrix to check the result
		//print_2D(a);
		System.out.println("Created matrix \n");
		//print_2D(b);

	}
	
	public static void print_2D(double[][] c2) {
		for (double[] row : c2)

			// converting each row as string
			// and then printing in a separate line
			System.out.println(Arrays.toString(row));

	}
	

	// input matrix by hand 
	public static void TypeMatrix(int size,double[][] a) {
		Scanner sc = new Scanner(System.in);
		a = new double[size][size];	
		System.out.println("enter the matrix you want, ','for divide each element,and';'for switch row ");
		String input = sc.nextLine();
		String[] row = input.split(";");
		for(int i = 0;i<size;i++) {
			String[] cell = row[i].split(",");
			for(int j = 0;j<size;j++) {
				a[i][j]= Double.parseDouble(cell[j]);
			}
		}
		
	}


	// main function for send and recive 
	public static void sendObject() throws IOException, ClassNotFoundException {
		String id = null;
		int matrixSize = 100;
		String output;

//		id = "1";
//		CreateMatrix(5);
//		SendWork send = new SendWork(3,a,b,id);		
//		out = new ObjectOutputStream(socket.getOutputStream());
//		dis = new DataInputStream(socket.getInputStream());
//		in = new ObjectInputStream(socket.getInputStream());
//		out.writeObject(send);
//		System.out.println("Wrote object");
//		
//		res = (MatrixResult) in.readObject();
//		print_2D(res.answer);
//		
//			output = dis.readUTF();
//			System.out.println("this is your work id, plz keep it"+output);
		
		Scanner sc = new Scanner (System.in);
		System.out.println("input the operation you want ");
		System.out.println("1 for add \n2 for multiply \n3 for minus ");
		System.out.println("4 for check status \n5 for get result \n0 for stop work and print bill");
		int op = sc.nextInt();
		
//		String hostname = "137.116.128.225";
//		int port = 1024;
		

		MatrixClient client = new MatrixClient("", 0);
		
		if(op == 1 || op ==2 || op == 3) {
			System.out.println("do you want to input the matrix by hand (y or n)?");
			boolean temp = sc.next().charAt(0) == 'y' ? true : false;
			System.out.println("inter the size of matrix you want");
			matrixSize = sc.nextInt();
			if(temp) {
				TypeMatrix(matrixSize,a);
				TypeMatrix(matrixSize,b);
			}else {				 
				CreateMatrix(matrixSize);
				System.out.println("Matrix A: ");
				print_2D(a);
				System.out.println("Matrix B: ");
				print_2D(b);
			}
		}else if (op ==4 || op == 5 || op == 8 || op == 0) {
			System.out.println("enter the id");
			id = Integer.toString(sc.nextInt());
		}
		SendWork send = new SendWork(op,a,b,id);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(send);
		//System.out.println("send finished");
		if(op == 4) {
			try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			MatrixResult res = (MatrixResult) in.readObject();
			
			if(res.stat == Status.not_finished) {
				System.out.println("Job not finished yet - please check later");
				
			}
			else if(res.stat == Status.invalid_paramaters) {
				System.out.println("No such ID exisits");
				
			}
			else {
				System.out.println("Result " + "for id " + id + " ready for retrieval");				
			}
			in.close();
			}catch(Exception ex) {
				System.out.println("Server cancelled the connection");
				sc.close();
				return;
			}
			
		}else if(op == 5){
			ObjectInputStream in;
			MatrixResult res;
			try {
				 in = new ObjectInputStream(socket.getInputStream());
				 res = (MatrixResult) in.readObject();
				}catch(Exception ex) {
					System.out.println("Server cancelled the connection");
					return;
				}
				
			
			
			if(res.stat == Status.not_finished) {
				System.out.println("Job not finished yet - please check later");
				
			}
			else if(res.stat == Status.invalid_paramaters) {
				System.out.println("No such ID exisits");
				
			}
			else if(res.stat == Status.successful_calculation) {
				print_2D(res.answer);
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				System.out.println("here is your bill: " + dis.readDouble() + " dollars");
			}
		}
		else if(op != 0){
			DataInputStream dis;
			try {
			 dis = new DataInputStream(socket.getInputStream());
			}catch(Exception e) {
				System.out.println("Server cancelled the connection");
				return;
			}
			 output = dis.readUTF();
			System.out.println("this is your work id:  "+output);
			dis.close();
		}else {
			DataInputStream dis;
			try {
			 dis = new DataInputStream(socket.getInputStream());
			}catch(Exception e) {
				System.out.println("Server cancelled the connection");
				return;
			}
			Double d = dis.readDouble();
			//System.out.println("here is your bill: " + d + " dollars");
			if(d <= 0) {
				System.out.println("No such ID exisits");
			}
			else {
				System.out.println("You have cancelled your calculation \nHere is your bill: " + d + " dollars");
			}
		}
		out.close();
		socket.close();
		sc.close();
		
	}

	public static void main(String[] args) {
		
		
		try {
			sendObject();
		}catch( IOException e ) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
			
		
	}

}