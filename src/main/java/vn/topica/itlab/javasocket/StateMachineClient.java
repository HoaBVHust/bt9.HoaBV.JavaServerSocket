package vn.topica.itlab.javasocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;


public class StateMachineClient implements Runnable {
	public State state = State.INIT ;
	private String fileString;
	int index;
	public StateMachineClient(String fileString,int index) {
		this.fileString = fileString;
		this.index= index;
	}
	@Override
	public void run() {
		while(true) {
			try(Socket socket = new Socket("localhost", 9669)) {
				System.out.println("Connected: " + socket);
				FileInputStream fileInputStream = new FileInputStream(fileString);
				Scanner scanner = new Scanner(fileInputStream);
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				while (true) {
					while (scanner.hasNextLine()) {
						String string = scanner.nextLine();
						Packet packet = new Packet(string);
						out.writeBytes(packet.toBytes());
						System.out.print("Client "+index+" send:\n"+
								packet.toString()+"\n"
								//packet.toPacket()+"\n"+
								//packet.toBytes()+"\n"
								);
						while(true) {
							try {
								Packet packetReply = new Packet(in);
								System.out.print("Client "+index+" receive:\n"+
										//packetReply.toPacket()+"\n"+
										packetReply.toString()+"\n");
								handleServerReply(packetReply);
								break;
							} catch (Exception e) {
								e.printStackTrace();
								break;
							}
						}
						System.out.println("State of Client "+index+" is:" + state);
					}
					scanner.close();
					fileInputStream.close();
					break;
				}
				socket.close();
				System.out.println("Closed socket:"+index+" "+socket);
				break;
			
			} catch (ConnectException e) {
				System.out.println("Server no respond"); 
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception stateMachineClient");
			}
		}
		
	}
	public void handleServerReply(Packet packet) {
		switch (packet.cmdCode) {
		case 0:
			handleAuthen(packet);
			break;
		case 1:
			handleInsert(packet);
			break;
		case 2:
			handleCommit(packet);
			break;
		case 3:
			handleSelect(packet);
			break;
		case 4:
			handleError(packet);
			break;			
		default:
			break;
		}
	}
	private void handleError(Packet packet) {
		System.out.println("Message request error!");
		
	}
	private void handleSelect(Packet packet) {
		if(packet.getTLVbyTag(3).value.equals("OK")) {
			System.out.println("Select successfull. User was selected: PhoneNumber "+packet.getTLVbyTag(1).value+" Name "+ packet.getTLVbyTag(2).value);
		}
		else {
			System.out.println("Select error");
		}
	}
	private void handleCommit(Packet packet) {
		if(packet.getTLVbyTag(3).value.equals("OK")) {
			state = State.SELECT;
			System.out.println("Commit successful");
		}
		else {
			System.out.println("Commit error");
		}
		
	}
	private void handleInsert(Packet packet) {
		if(packet.getTLVbyTag(3).value.equals("OK")) {
			System.out.println("Insert successful. User was added to list user");
		}
		else {
			System.out.println("Insert error");
		}
		
		
	}
	public void handleAuthen(Packet packet) {
		if(packet.getTLVbyTag(3).value.equals("OK")) {
			state = State.READY;
			System.out.println("Authen successful");
		}
		else {
			System.out.println("Authen error");
		}
		
	}
}

	