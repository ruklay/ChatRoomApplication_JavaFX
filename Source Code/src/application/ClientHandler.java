package application;

import javafx.application.*;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class ClientHandler extends Thread {
	public Socket serverSocket;
	ArrayList<ClientHandler> clients;

	DataInputStream dis = null;
	DataOutputStream dos = null;
	private ServerController svc;
	private UserController usc;

	private String clientName = null;
	private String IPAddress = null;
	private int port = 0;
	
	ArrayList<String> blockedList;
	private HashMap<Integer, Member> members = new HashMap<Integer, Member>();

	public ClientHandler(Socket serverSocket, ArrayList<ClientHandler> clients, ServerController svc, UserController usc) {

		this.serverSocket = serverSocket;
		this.clients = clients;
		this.svc = svc;
		this.usc = usc;
		this.IPAddress = serverSocket.getInetAddress().getHostAddress();
		this.port = serverSocket.getPort();

		svc.addStringConsole("New client connected from IP: " + IPAddress +" and port: " + port, ServerConstants.SYSTEM_MESSAGE);

		try {
			dis = new DataInputStream(serverSocket.getInputStream());
			dos = new DataOutputStream(serverSocket.getOutputStream());
		} catch (IOException e) {
			svc.addStringConsole("Cannot get DataStream ..", ServerConstants.ERROR_MESSAGE);
		}
	}

	private boolean isMemberLoggedOn(String username) {
		// Check if this member is already in the chat room or not

		boolean flag = false;
		for (Member member : svc.listObvMember) {
			if (member.getName().equals(username)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	private String getBroadcastMessage(String xmlMessage) {
		// Parse the XML Broadcast message from client
		// Extract only real message to be displayed in console

		String broadcastMessage = "";
		try {
			DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmldoc = docReader.parse(new InputSource(new StringReader(xmlMessage)));

			Element root = xmldoc.getDocumentElement();
			NodeList certainNodes = root.getElementsByTagName("TEXT");
			for(int i = 0; i < certainNodes.getLength(); i++) {
				broadcastMessage = ((Element)(certainNodes.item(i))).getAttribute("text");
			}
		} catch (Exception e) {

		}
		return broadcastMessage;
	}

	private ArrayList<String> getBlockedList(String xmlMessage) {
		// Parse the XML Broadcast message from client
		// Extract a list of blocked user name

		ArrayList<String> blockedList = new ArrayList<String>();
		String name = "";
		try {
			DocumentBuilder docReader = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document xmldoc = docReader.parse(new InputSource(new StringReader(xmlMessage)));

			Element root = xmldoc.getDocumentElement();
			NodeList certainNodes = root.getElementsByTagName("NAME");
			for(int i = 0; i < certainNodes.getLength(); i++) {
				name = ((Element)(certainNodes.item(i))).getAttribute("name");
				blockedList.add(name);
			}
		} catch (Exception e) {

		}
		return blockedList;
	}

	public void run() {
		// New client connects
		//svc.addStringConsole("Client connected");

		while (true) {
			int message;
			String strFromClient;

			try {
				message = dis.read();
				strFromClient = dis.readUTF();

				// Perform the message based on message id sent from a client
				if (message == ServerConstants.CHAT_MESSAGE) {
					// Not used

					//svc.addStringConsole("Got message from " + clientName + "> " + strFromClient);
					//dos.write(ServerConstants.CHAT_MESSAGE);
					//dos.writeUTF("-->Got the: " + strFromClient);

				} else if (message == ServerConstants.BROADCAST_MESSAGE) {
					// Parse the XML broadcast message
					// Send to all members except the blocked ones

					String broadcastMessage = getBroadcastMessage(strFromClient);
					blockedList = getBlockedList(strFromClient);

					String broadMessageWithUser = clientName + "> " + broadcastMessage;
					svc.addStringConsole(broadMessageWithUser, ServerConstants.BROADCAST_MESSAGE);
					for (ClientHandler client : clients) {
						if(!blockedList.contains(client.clientName)) {
							client.dos.write(ServerConstants.BROADCAST_MESSAGE);
							client.dos.writeUTF(broadMessageWithUser);
						}
					}

				} else if (message == ServerConstants.LOG_IN) {
					// A new user is logging in

					clientName = strFromClient.split("\\|")[0];
					String clientPassword = strFromClient.split("\\|")[1];

					if (usc.isLoginSuccessful(clientName, clientPassword)) {
						// Log in success

						// Check if this user is currently in the chat room (double logged in user)
						if(!isMemberLoggedOn(clientName)) {
							// This new member is not in the chat room
							// Log in as usual

							// Act this new member that the log in process is successful
							dos.write(ServerConstants.LOG_IN_SUCCESS);
							dos.writeUTF(clientName);

							// Log the console
							String welcomeNewMember = clientName +" has join the chat";
							svc.addStringConsole(welcomeNewMember, ServerConstants.SYSTEM_MESSAGE);

							// Create and add this new member to the member list
							Member member = new Member(clientName, IPAddress, port, false);
							members.put(port, member);

							Platform.runLater(new Runnable() {
								public void run() {
									svc.listObvMember.add(member);
								}
							});

							// Ack all existing members that we have recieved a new member
							for(ClientHandler client: clients){
								client.dos.write(ServerConstants.SYSTEM_MESSAGE);
								client.dos.writeUTF(welcomeNewMember);

								if(client.clientName.equals(clientName)){
									// Send all current members info to the new member
									// So the new member can have full member info in his friend list

									for(ClientHandler clientEach: clients){
										client.dos.write(ServerConstants.NEW_MEMBER);
										client.dos.writeUTF(clientEach.clientName+"|"+clientEach.IPAddress+"|"+clientEach.port);
									}
								} else {
									// Send only the new member info to the those existing members
									// Those existing members already have friend list, so need only the new member info

									client.dos.write(ServerConstants.NEW_MEMBER);
									client.dos.writeUTF(clientName+"|"+IPAddress+"|"+port);
								}
					        }
						} else {
							// This new user can't log in because this user is already in the chat room

							svc.addStringConsole("Username "+clientName+" logged in falied (already logged on)", ServerConstants.ERROR_MESSAGE);
							dos.write(ServerConstants.LOG_IN_FAIL);
							dos.writeUTF("Username has already logged on");
						}
					} else {
						// This new user sends wrong username or password

						svc.addStringConsole("Username "+clientName+" logged in falied", ServerConstants.ERROR_MESSAGE);
						dos.write(ServerConstants.LOG_IN_FAIL);
						dos.writeUTF("Invalid Username or Password");
						//break;
					}


				} else if (message == ServerConstants.LOG_OUT) {
					// This user log out or left the chat room

					String logOutMessage = clientName+" has left the chat";
					svc.addStringConsole(logOutMessage, ServerConstants.SYSTEM_MESSAGE);

					// Remove this member from the memberlist
					int logOutPort = Integer.parseInt(strFromClient);
					Member logOutMember = members.get(logOutPort);
					Platform.runLater(new Runnable() {
						public void run() {
							svc.listObvMember.remove(logOutMember);
						}
					});
					members.remove(logOutPort);

					// Send message to all existing friends
					for(ClientHandler client: clients){
						if (!client.clientName.equals(clientName)) {
							client.dos.write(ServerConstants.SYSTEM_MESSAGE);
							client.dos.writeUTF(logOutMessage);

							//client.dos.write(ServerConstants.SYSTEM_MESSAGE);
							//client.dos.writeUTF("Member in chatroom: "+memberList.toString());

							client.dos.write(ServerConstants.LOG_OUT);
							client.dos.writeUTF(strFromClient);
						}
			        }

					// This thread does not need to run anymore
					// Exit the while(true) loop
					break;

				} else if (message == ServerConstants.PRIVATE_MESSAGE) {
					// Send private message

					String targetUser = strFromClient.split("\\|")[0];
					String privateMessage = strFromClient.split("\\|")[1];
					privateMessage = "From "+clientName + " to "+targetUser+"> " + privateMessage;
					svc.addStringConsole(privateMessage, ServerConstants.PRIVATE_MESSAGE);
					for (ClientHandler client : clients) {
						if(client.clientName.equals(targetUser)) {
							client.dos.write(ServerConstants.PRIVATE_MESSAGE);
							client.dos.writeUTF(privateMessage);
						}
					}
				} else if (message == ServerConstants.SEND_FILE_TO_SERVER) {
					// Client sends a file to server
					
					// Prepare the target file
					List<String> listStr = new ArrayList<String>(Arrays.asList(strFromClient.split("\\|")));
					String sendTo = listStr.get(0);
					String fileName = listStr.get(1);
					
					//Path p = Paths.get(strFromClient);
					//String fileName = p.getFileName().toString();
					long fileSize = dis.readLong();
					
					svc.addStringConsole(clientName+" sends a file: "+fileName, ServerConstants.SYSTEM_MESSAGE);
					//svc.addStringConsole("File size : "+String.valueOf(fileSize), ServerConstants.SYSTEM_MESSAGE);
					
					// Receiving the file and save it to the server path
					FileOutputStream fos = new FileOutputStream("server_files\\"+fileName);
				    BufferedInputStream bis = new BufferedInputStream(serverSocket.getInputStream());
				    //BufferedOutputStream bos = new BufferedOutputStream(fos);
				    
				    byte[] b = new byte[8192];
					int len;
					int count = 0;
					int gap = 0;
				    while ((len = bis.read(b)) > 0) {
				    	count += len;
				    	if (count > fileSize) gap = count-(int)fileSize;
				        fos.write(b, 0, len-gap);
				        //System.out.println("Count = "+String.valueOf(count-gap));
				        if(count>=fileSize) break;
				    }
				    
			        svc.addStringConsole("Receiving "+fileName+" completed", ServerConstants.SYSTEM_MESSAGE);
			        fos.close();
			        
			        // Ack receiver(s) that there is a new file
			        String sendingFileMessage = clientName + "|" + fileName;
					//svc.addStringConsole(sendingFileMessage, ServerConstants.BROADCAST_MESSAGE);
			        
					for (ClientHandler client : clients) {
						if(sendTo.equals("All members") || sendTo.equals(client.clientName)) {
							client.dos.write(ServerConstants.SEND_ACK_FILE_TO_CLIENT);
							client.dos.writeUTF(sendingFileMessage);
						}
					}
					
				} else if (message == ServerConstants.GET_FILE_FROM_CLIENT) {
					// Server sends a file to the client
					
					// Prepare the file
					svc.addStringConsole("Begin sending "+strFromClient+" to the client", ServerConstants.SYSTEM_MESSAGE);
					
					String fileNameFull = "server_files\\"+strFromClient;
					File file = new File(fileNameFull);
					long fileSize = file.length();

					dos.write(ServerConstants.SEND_FILE_TO_CLIENT);
					dos.writeUTF(strFromClient);
					dos.writeLong(fileSize);

					// Send the file to the client
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					BufferedOutputStream bos = new BufferedOutputStream(serverSocket.getOutputStream());
					
					byte[] b = new byte[8192];
					int len;
					int count = 0;
				    while ((len = bis.read(b)) > 0) {
				        bos.write(b, 0, len);
				        count += len;
				    }
				    bos.flush();
				    //System.out.println("FileSize = "+file.length()+" and Count = "+String.valueOf(count));
				    svc.addStringConsole("Sending the " + strFromClient + " completed", ServerConstants.SYSTEM_MESSAGE);
					
				} else {
					// Unknown message code
					System.out.println("Unknown message code.."+strFromClient);
				}
			} catch (IOException e) {
				svc.addStringConsole("Error in communication with the server.", ServerConstants.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

		try {
			clients.remove(this);
		} catch (Exception e) {
			svc.addStringConsole("Cannot remove client", ServerConstants.ERROR_MESSAGE);
		}
	}
}
