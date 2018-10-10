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



			
	public enum BinaryOperation{
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
		try {
			// create a socket
			socket = new Socket(hostname, port);
			//out = new OutputStreamWriter(socket.getOutputStream());
			//in = new ObjectInputStream(socket.getInputStream());			

		}
		catch (Exception e) {
			e.printStackTrace();
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
		sc.close();
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
		System.out.println("input the opreation you want ");
		System.out.println("1 for add \n 2for multiply \n 3 for minus ");
		System.out.println("4 for check status \n 5 for get result");
		int op = sc.nextInt();
		if(op == 1 || op ==2 || op == 3) {
			System.out.println("do you want to input the matrix by hand?");
			boolean temp = sc.nextBoolean();
			System.out.println("inter the size of matrix you want");
			matrixSize = sc.nextInt();
			if(temp) {
				TypeMatrix(matrixSize,a);
				TypeMatrix(matrixSize,b);
			}else {
				 
				CreateMatrix(matrixSize);
			}
		}else if (op ==4 || op == 5 || op == 8) {
			System.out.println("enter the id");
			id = Integer.toString(sc.nextInt());
		}
		SendWork send = new SendWork(op,a,b,id);
		System.out.println("send created");
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		System.out.println("a");
		
		
		
		out.writeObject(send);
		System.out.println("send finished");
		if(op == 4 || op == 5) {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			MatrixResult res = (MatrixResult) in.readObject();
			
			if(res.stat == Status.not_finished) {
				System.out.println("Job not finished yet - please check later");
				
			}
			else if(res.stat == Status.successful_calculation) {
				print_2D(res.answer);
			}
			
			in.close();
			
		}else {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			output = dis.readUTF();
			System.out.println("this is your work id, plz keep it"+output);
			dis.close();
		}
		out.close();
		socket.close();
		sc.close();
		
	}

	public static void main(String[] args) {
		
		String hostname = "localhost";
		int port = 1024;
		

		if (args.length != 3) {
			System.out.println("Use the default setting...");
		} 
		else {
			//use cmd line args
			hostname = args[0];
			port = Integer.parseInt(args[1]);
//			String s = args[3];
//			System.out.println("Process " + s);
			
		}

		String rowCol = "row_column";
		MatrixClient client = new MatrixClient(hostname, port);
		try {
			sendObject();
		}catch( IOException e ) {
			e.printStackTrace();
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
			
		
	}

}
