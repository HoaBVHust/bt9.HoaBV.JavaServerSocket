package vn.topica.itlab.javasocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

enum State{INIT, READY, SELECT};
public class StateMachine implements Runnable {
	private Socket socket;
	public State state = State.INIT;
	public ArrayList<UserInformation> arrayListUser; 
	public StateMachine(Socket socket,ArrayList<UserInformation> arrayListUser) {
		this.socket = socket;
		this.arrayListUser = arrayListUser;
	}

	@Override
	public void run() {
		System.out.println("Connected: " + socket);
		try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			while (true) {
				try {
					//Server receive packet
					Packet packet= new Packet(in);
					System.out.print("Server receive:\n"+
							//packet.toPacket()+"\n"+
							packet.toString()+"\n");
					//Server process packet
					handlePacketIn(packet, out);
					System.out.println("listUser: "+arrayListUser.toString());
				} catch (EOFException e) {
					System.out.println("Client closed socket");
					break;
				} catch (SocketException e) {
					System.out.println("Client closed socket");
					break;
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + socket);
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
			System.out.println("Closed: " + socket);
		}
	}
	//2.1 Server check packet that satisfy the conditions
	public boolean checkPhoneNotErr(Packet packet) {
	
		byte[] bytes = packet.getTLVbyTag(1).value.getBytes();
		if (bytes.length != 10)
			return false;
		if (bytes[0] != '0' | bytes[1] != '9' | bytes[2] != '8' | bytes[3] == '0' | bytes[3] == '1')
			return false;
		for (int i = 4; i < bytes.length; i++) {
			if (bytes[i] > '9' | bytes[i] < '0')
				return false;
		}
		return true;
	}
	// when satisfy the conditions, reply error packet
	public void handleError(DataOutputStream out) {
		Packet packetError = new Packet();
		packetError.lengthOfMessage = 14;
		packetError.cmdCode = 4;
		TLV tlv = new TLV();
		tlv.tag = 3;
		tlv.length = 2;
		tlv.value = "NA";
		packetError.arrayList.add(tlv);
		try {
			out.writeBytes(packetError.toBytes());
			System.out.print("Server reply:\n"+
					packetError.toString()+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//when packet satisfy the conditions
	public void handleNotErr(Packet packet, DataOutputStream out) {
		switch (packet.cmdCode) {
		case 0:
			handleAuthen(packet, out);
			break;
		case 1:
			handleInsert(packet, out);
			break;
		case 2:
			handleCommit(packet, out);
			break;
		case 3:
			handleSelect(packet, out);
			break;
		default:
			break;
		}
		try {
			out.writeBytes(packet.toBytes());
			System.out.print("Server reply:\n"+
					packet.toString()+"\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void resultCodeOK(Packet packet,TLV tlvResultCode) {
		tlvResultCode.tag = 3;//Set tag is ResultCode
		tlvResultCode.value = "OK";
		packet.lengthOfMessage = packet.lengthOfMessage - tlvResultCode.length +2;
		tlvResultCode.length = 2;
	}
	public void resultCodeNOK(Packet packet,TLV tlvResultCode) {
		tlvResultCode.tag = 3;//Set tag is ResultCode
		tlvResultCode.value = "NOK";
		packet.lengthOfMessage = packet.lengthOfMessage - tlvResultCode.length +3;
		tlvResultCode.length = 3;
	}
	//2.2 Process packet when it is Authen packet
	public void handleAuthen(Packet packet, DataOutputStream out) {
		TLV tlvResultCode = packet.getTLVbyTag(0);		
		if(state==State.INIT & tlvResultCode.value.equals("topica")) {
			resultCodeOK(packet,tlvResultCode);
			state = State.READY;
		}
		else {
			resultCodeNOK(packet, tlvResultCode);
		}
	}
	//2.3 Process packet when it is Insert packet
	public void handleInsert(Packet packet, DataOutputStream out) {
		TLV tlvResultCode = packet.getTLVbyTag(2);
		if(state==State.READY) {		
			UserInformation user = new UserInformation();
			user.phoneNumber=packet.getTLVbyTag(1).value;
			user.name=packet.getTLVbyTag(2).value;		
			arrayListUser.add(user);
			resultCodeOK(packet, tlvResultCode);			
		}
		else {
			resultCodeNOK(packet, tlvResultCode);
		}
	}
	//2.3 Process packet when it is Commit packet
	public void handleCommit(Packet packet, DataOutputStream out) {
		TLV tlvResultCode = new TLV();
		packet.arrayList.add(tlvResultCode);
		packet.lengthOfMessage += 4;
		if(state==State.READY) {
			state = State.SELECT;
			resultCodeOK(packet, tlvResultCode);			
		}
		else {
			resultCodeNOK(packet, tlvResultCode);
		}
	}
	//2.4 Process packet when it is Select packet
	public void handleSelect(Packet packet, DataOutputStream out) {
		TLV tlvResultCode = new TLV();
		packet.arrayList.add(tlvResultCode);
		packet.lengthOfMessage += 4;
		if(state==State.SELECT) {
			resultCodeOK(packet, tlvResultCode);
			TLV tlvName = new TLV();
			tlvName.tag = 2;
			for (UserInformation user : arrayListUser) {
				if(user.phoneNumber.equals(packet.getTLVbyTag(1).value)) {
					tlvName.value = user.name;
					tlvName.length = (short)tlvName.value.getBytes().length;
				}
			}
			packet.arrayList.add(tlvName);
			packet.lengthOfMessage += 4+tlvName.length;
		}
		else {
			resultCodeNOK(packet, tlvResultCode);
		}
	}
	// when have a packet come to server
	public void handlePacketIn(Packet packet, DataOutputStream out) {
		if (checkPhoneNotErr(packet))
			handleNotErr(packet, out);
		else {
			handleError(out);
		}
	}
}
