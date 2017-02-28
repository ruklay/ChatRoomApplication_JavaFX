package application;

import javafx.application.*;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientSocketConnection {

	private ClientController clc;
	private ClientLoginController cllc;
	public SocketThread socketThread = new SocketThread();
	public Socket client;
	private int port = 5000;

	HashMap<Integer, Member> members = new HashMap<Integer, Member>();

	public ClientSocketConnection() {

	}

	public ClientSocketConnection(ClientController clc, ClientLoginController cllc) {
		this.clc = clc;
		this.cllc = cllc;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void refreshThread() {
		socketThread = new SocketThread();
	}

	public void startClient() throws Exception {
		if (!isStart())
			socketThread.run();
	}

	public void stopClient() throws IOException {
		// This method is called by the controller, when the user press Exit button

		if (isStart()) {
			sendMessage(ServerConstants.LOG_OUT, String.valueOf(client.getLocalPort()));
		}
		stopClientSocket();
	}

	public void stopClientSocket() {
		// Disable all input when no connection (e.g. the server stops)

		clc.txtSend.setDisable(true);
		clc.btnSend.setDisable(true);
		clc.btnSendFile.setDisable(true);
		// socketThread.dos.close();
		// socketThread.dis.close();
		// client.close();
	}

	public boolean isStart() {
		return client != null;
	}
	
	public void getFileFromServer(String fileName) throws IOException {
		sendMessage(ServerConstants.GET_FILE_FROM_CLIENT, fileName);
	}

	public String createXMLMessage(String message) {
		// The format of broadcast message
		// Including real message, and blocked persons

		String xmlMessage = "<MESSAGE>\n";
		xmlMessage += "	<TEXT text ='" + message + "'/>\n";
		xmlMessage += "	<BLOCK>\n";
		for (Member eachMember : members.values()) {
			if (eachMember.getName().equals(clc.txtName.getText()))
				continue;
			if (eachMember.getIsBlocked())
				xmlMessage += "		<NAME name ='" + eachMember.getName() + "'/>\n";
		}
		xmlMessage += "	</BLOCK>\n";
		xmlMessage += "</MESSAGE>\n";
		return xmlMessage;
	}

	public void sendMessage(String textMessage) throws IOException {
		sendMessage(ServerConstants.BROADCAST_MESSAGE, createXMLMessage(textMessage));
	}

	public void sendMessage(int message, String textMessage) throws IOException {
		socketThread.dos.write(message);
		socketThread.dos.writeUTF(textMessage);
	}

	public void doValidateUserNamePassword(String username, String password) throws IOException {
		sendMessage(ServerConstants.LOG_IN, username + "|" + password);
	}

	public void performSendFile(String fileNameSender) {
		try {
			
			// Prepare the file to be sent
			List<String> listStr = new ArrayList<String>(Arrays.asList(fileNameSender.split("\\|")));
			String sendTo = listStr.get(0);
			String fileNameFull = listStr.get(1);
			
			File file = new File(fileNameFull);
			Path p = Paths.get(fileNameFull);
			String fileName = p.getFileName().toString();
			long fileSize = file.length();

			socketThread.dos.write(ServerConstants.SEND_FILE_TO_SERVER);
			socketThread.dos.writeUTF(sendTo+"|"+fileName);
			socketThread.dos.writeLong(fileSize);

			// Send the file to server
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
			
			byte[] b = new byte[8192];
			int len;
			int count = 0;
		    while ((len = bis.read(b)) > 0) {
		        bos.write(b, 0, len);
		        count += len;
		    }
		    bos.flush();
		    //System.out.println("FileSize = "+file.length()+" and Count = "+String.valueOf(count));
			clc.addStringConsole("Sending the " + fileName + " completed", ServerConstants.SYSTEM_MESSAGE);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class SocketThread extends Thread {
		DataOutputStream dos;
		DataInputStream dis;

		public void run() {
			try {
				client = new Socket("localhost", port);
				clc.addStringConsole("Chat Room starts on port: " + port, ServerConstants.SYSTEM_MESSAGE);

				dos = new DataOutputStream(client.getOutputStream());
				dis = new DataInputStream(client.getInputStream());

				Thread serverHandler = new Thread() {
					public void run() {
						while (true) {
							int message;
							String textMessage;
							try {
								message = dis.read();
								textMessage = dis.readUTF();

								if (message == ServerConstants.NEW_MEMBER) {
									// A new friend log in
									// Create new Member and put it in the
									// friend list

									// create new Member class and put it in
									// HashMap
									List<String> listStr = new ArrayList<String>(Arrays.asList(textMessage.split("\\|")));
									Member member = new Member(listStr.get(0), listStr.get(1), Integer.parseInt(listStr.get(2)), false);
									members.put(Integer.parseInt(listStr.get(2)), member);

									// add new member to member list
									Platform.runLater(new Runnable() {
										public void run() {
											clc.listObvMember.add(member);
											clc.listObvMemberName.add(listStr.get(0));
										}
									});

								} else if (message == ServerConstants.LOG_IN_SUCCESS) {
									// Log in success
									// Show the chat screen

									Platform.runLater(new Runnable() {
										public void run() {
											try {
												clc.txtName.setText(textMessage);
												cllc.showChatScene(textMessage);
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}
									});

								} else if (message == ServerConstants.LOG_IN_FAIL) {
									// User input wrong username or password

									Platform.runLater(new Runnable() {
										public void run() {
											try {
												cllc.txtMessage.setText(textMessage);
												cllc.clearAndFocusText();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									});

								} else if (message == ServerConstants.LOG_OUT) {
									// A friend log out
									// Remove the friend from friend list

									int logOutPort = Integer.parseInt(textMessage);
									Member logOutMember = members.get(logOutPort);
									Platform.runLater(new Runnable() {
										public void run() {
											clc.listObvMember.remove(logOutMember);
											clc.listObvMemberName.remove(logOutMember.getName());
										}
									});
									members.remove(logOutPort);
									;

								} else if (message == ServerConstants.SERVER_STOP) {
									// The server stops
									// Clear friend list
									// Disable all input and buttons

									clc.addStringConsole("The server stops working", ServerConstants.ERROR_MESSAGE);
									Platform.runLater(new Runnable() {
										public void run() {
											clc.listObvMember.clear();
											clc.listObvMemberName.clear();
										}
									});
									stopClientSocket();

								} else if (message == ServerConstants.BROADCAST_MESSAGE) {
									// Check if the sender is in blockedList or
									// not
									// If yes, do not print the message

									List<String> listStr = new ArrayList<String>(Arrays.asList(textMessage.split(">")));
									String sender = listStr.get(0);
									boolean flag = true;
									for (Member member : members.values()) {
										if (member.getName().equals(sender) && member.getIsBlocked()) {
											flag = false;
										}
									}
									if (flag)
										clc.addStringConsole(textMessage, ServerConstants.BROADCAST_MESSAGE);

								} else if (message == ServerConstants.SYSTEM_MESSAGE) {

									clc.addStringConsole(textMessage, ServerConstants.SYSTEM_MESSAGE);

								} else if (message == ServerConstants.PRIVATE_MESSAGE) {

									clc.addStringConsole(textMessage, ServerConstants.PRIVATE_MESSAGE);

								} else if (message == ServerConstants.SEND_ACK_FILE_TO_CLIENT) {

									List<String> listStr = new ArrayList<String>(Arrays.asList(textMessage.split("\\|")));
									String sender = listStr.get(0);
									String fileName = listStr.get(1);
									
									String consoleMessage = sender +"> sends a file: "+ fileName;
									clc.addStringConsole(consoleMessage, ServerConstants.SEND_ACK_FILE_TO_CLIENT);
									clc.addLinkLoadFile(fileName);

								} else if (message == ServerConstants.SEND_FILE_TO_CLIENT) {

									String fileName = textMessage;
									long fileSize = dis.readLong();
									
									clc.addStringConsole("Start downloading the "+fileName, ServerConstants.SYSTEM_MESSAGE);
										
									///////////////////
									
									File directory = new File("user_files\\"+clc.txtName.getText()+"\\");
									if(!directory.isDirectory()) {
										directory.mkdir();
									}
									System.out.println(directory+"\\"+fileName);
								
									FileOutputStream fos = new FileOutputStream(directory+"\\"+fileName);
								    BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
								    BufferedOutputStream bos = new BufferedOutputStream(fos);
								    
								    
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
								    
								    
							        clc.addStringConsole("Download "+fileName+" completed", ServerConstants.SYSTEM_MESSAGE);
							        clc.addLinkOpenFile(fileName);

								} else {

									clc.addStringConsole(textMessage + " with code " + message,ServerConstants.SYSTEM_MESSAGE);

								}

							} catch (IOException e) {
								System.err.println(e);
								break;
							}
						}
					}
				};

				serverHandler.start();
			} catch (IOException e) {
				clc.addStringConsole("Cannot connect to the server: Code 3", ServerConstants.ERROR_MESSAGE);
				clc.btnSend.setDisable(true);
				clc.txtSend.setDisable(true);
				clc.btnSendFile.setDisable(true);
			}
		}
	}
}
