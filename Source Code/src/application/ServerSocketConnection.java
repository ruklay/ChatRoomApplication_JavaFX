package application;

import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Platform;

public class ServerSocketConnection {
	
	public ServerController svc;
	public UserController usc;
	public ServerSocketThread serverSocketThread = new ServerSocketThread();
	public ArrayList<ClientHandler> clientHandlers;
	
	public ServerSocketConnection() {

	}
	
	public ServerSocketConnection(ServerController svc, UserController usc) {
		this.svc = svc;
		this.usc = usc;
	}
	
	public void startServer() throws Exception {
		serverSocketThread.start();
	}
	
	public boolean isStart() {
		return !serverSocketThread.server.isClosed();
	}
	
	public void stopServer() {
		try {
			for (ClientHandler client : clientHandlers) {
				client.dos.write(ServerConstants.SERVER_STOP);
				client.dos.writeUTF("");
				//client.dos.close();
				//client.dis.close();
				svc.txtConsole.setDisable(true);
			}	
			Platform.runLater(new Runnable() {
				public void run() {
					svc.listObvMember.clear();
				}
			});
			serverSocketThread.server.close();
		} catch (Exception e) {
			svc.addStringConsole("Some problem while closing the server", ServerConstants.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private class ServerSocketThread extends Thread {
		private ServerSocket server;
		
		public void run() {
			try {
	            server = new ServerSocket(Integer.parseInt(svc.txtPort.getText()));
	            svc.addStringConsole("Server setup", ServerConstants.SYSTEM_MESSAGE);
	            svc.addStringConsole("Waiting for client connections on port: "+svc.txtPort.getText(), ServerConstants.SYSTEM_MESSAGE);
	            
	            clientHandlers = new ArrayList<ClientHandler>();
	            
	            while(true)
	            {
	                Socket serverSocket = server.accept();
	                
	                ClientHandler newClientHandler = new ClientHandler(serverSocket,clientHandlers,svc,usc);
	                newClientHandler.start();
	                
	                clientHandlers.add(newClientHandler);
	            }
	        } catch (IOException e){
	            System.out.println("Cannot serve a client");
	        }
		}
	}
}
