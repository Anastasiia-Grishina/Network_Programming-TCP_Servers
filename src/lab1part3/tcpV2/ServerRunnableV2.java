package lab1part3.tcpV2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import lab1part3.tcp.ConsumingTask;

public class ServerRunnableV2 implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    private String OUTPUT 		  = null;

    public ServerRunnableV2(Socket clientSocket, String serverText) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
    }

    public void run() {
        try {
        	// input is a get request - url
            InputStream input = clientSocket.getInputStream();
            System.out.println("Server runnable");
            
            // Read the bytes from the input stream of the socket line by line
            DataInputStream  inBytes 	= new DataInputStream(input);
//            		(new InputStreamReader(input)); 
            String cmdRecv = inBytes.readLine();
//            String cmdText = inBytes.readLine();
            String cmdType = cmdRecv.split("#")[0];
            String cmdText = cmdRecv.split("#")[1];
            
            System.out.println("Cmd type: " + cmdType);
            System.out.println("Cmd text: " + cmdText);
            
            if (cmdType.equals("loop")) {
            	try {
            		int iterNumber 	= Integer.parseInt(cmdText);
            		OUTPUT 			= new ConsumingTask(iterNumber).
            				FibonacciNumbers().toString();
            		System.out.println("Output is - " + OUTPUT);
            	} catch (NumberFormatException ex) {
            		OUTPUT 			= "Please, enter a valid number of Fibonacci numbers";
            	}
            } else if (cmdType.equals("cmd")) {
            	OUTPUT = cmdExecute(cmdText);
            }
            
            OutputStream output = clientSocket.getOutputStream();
            
            output.write(OUTPUT.getBytes());
            
            // this is the server response sent
            output.flush();
            
            output.close();
            input.close();
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
    
    // Command initiation will include on of the following
 	// depending on the OS type
 	public String shellPath() {
 		String sh = null;
 		String OS = System.getProperty("os.name").toLowerCase();
 		if (OS.indexOf("win") >= 0) {
 			sh = "bash";
 		} else {
 			sh = "/bin/sh";
 		}	
 		System.out.println("OS bash path is " + sh);
 		return sh;
 	}
    
    public String cmdExecute(String cmdText) {
    	// Create a command as an array of String elems
		String[] fullCmd	= {shellPath(), "-c", cmdText};
		String output		= "Unknown command";
		try {
			// Execute a command
			Process process = Runtime.getRuntime().exec(fullCmd);
            // Get input streams
            BufferedReader stdInput = new BufferedReader(
            		new InputStreamReader(
            				process.getInputStream()) //, "UTF-8"
            		);
            BufferedReader stdError = new BufferedReader(
            		new InputStreamReader(
            				process.getErrorStream()) // , "UTF-8"
            		);
            
            // Read command standard output and save
            String s = null;
            output = "Standard output for " + cmdText + "\n\n";

            System.out.println("Standard output for " + cmdText);
            try { 
	            while ((s = stdInput.readLine()) != null) {
	                System.out.println(s);
	                output = output + "\n" + s;
	            }
            } catch (Exception ex) {
        		System.out.println("Readline exception\n");
        	}

            
            // Read command standard errors and save
            if ((s = stdError.readLine()) != null) { 
            	output = "Standard error for " + cmdText + "\n\n";
            	output = output + s;
            	
            	System.out.println	("Standard error for " + cmdText);
            	System.out.println(s);
        		
            	try { 
		        	while ((s = stdError.readLine()) != null) {
		        		System.out.println(s);
		        		output = output + "\n" + s;
		        	}
            	} catch (Exception ex) {
            		System.out.println("Readline exception\n");
            	}
            }
            
        } catch (Exception e) {
        	System.out.println("Exception occurred\n");
        	// Print all the error stack, since a general exception class is captured
        	e.printStackTrace(System.err);
            
            // Java VM is terminated with non-zero status: abnormal termination
            System.exit(1);
		}
		return output;
    }
    
}
