package webserver;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Armando Alvear
 */
final class HttpRequest implements Runnable{
	
    final static String CRLF = "\r\n";
    Socket socket;
    //Constructor
    public HttpRequest(Socket socket) throws Exception{
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run(){
        
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e );
            e.printStackTrace();
        }
        
    }

    private void processRequest() throws Exception{
        
        //Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream(); 
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        //Set up Buffer Reader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        //Get the request line of the HTTP request message.
        String requestLine = br.readLine(); 
        //Display request line.
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines
        String headerline = null;
        while ((headerline = br.readLine()).length() != 0){
                System.out.println(headerline);
        }

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();  // skip over the method, which should be "GET"
        String fileName = tokens.nextToken();
        //Prepend a "." so that file request is within the current directory.
        fileName = fileName;

        // Open the requested file.
        FileInputStream fis1 = null;
        boolean fileExists = true;

        try {
            fis1 = new FileInputStream(fileName);
        }  catch (FileNotFoundException e) {
            fileExists = false;
        }
        
        //Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;

        if(fileExists) {
            statusLine = "HTTP/1.0 200 OK";
            contentTypeLine = "Content-type:" +
            contentType( fileName ) + CRLF;
        } else {
            statusLine = "HTTP/1.0 404: NOT FOUND";
            contentTypeLine = "text" + CRLF;
            entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
        }

        // Send the status line.
         os.writeBytes(statusLine);
        // Send the content type line.
        os.writeBytes(contentTypeLine);
        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        // Send the entity body.
        if (fileExists) {
            sendBytes(fis1, os);
            fis1.close();
        } else {
            os.writeBytes(entityBody);
        }

        os.close();
        br.close();
        socket.close(); 
        
    }
    
    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception{

        // Construct a 1K buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copy requested file into the socket's output stream.
        while((bytes=fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }

    }

    private static String contentType(String fileName){
    
	if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
		return "text/html";
	}
	if(fileName.endsWith(".gif") || fileName.endsWith(".GIF"))
	{
		return "image/gif";
	}
	if(fileName.endsWith(".jpeg"))
	{
		return "image/jpeg";
	}
	return "application/octet-stream";
        
    }
                
}
