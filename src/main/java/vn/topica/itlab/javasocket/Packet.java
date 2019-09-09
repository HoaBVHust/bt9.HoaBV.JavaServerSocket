package vn.topica.itlab.javasocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.util.ArrayList;

enum CmdCode {AUTHEN, INSERT, COMMIT, SELECT, ERROR};
public class Packet {
	int lengthOfMessage=0;
	short cmdCode;
	short version = 0;
	ArrayList<TLV> arrayList = new ArrayList<TLV>();

	public Packet() {
	}
	//constructor of Packet with text syntax
	public Packet(String string) {
		String[] strings = string.split(" ");
		CmdCode cmdCodeEnum = CmdCode.valueOf(strings[0]);
		switch (cmdCodeEnum) {
		case AUTHEN:
			cmdCode = 0;
			break;
		case INSERT:
			cmdCode = 1;
			break;
		case COMMIT:
			cmdCode = 2;
			break;
		case SELECT:
			cmdCode = 3;
			break;
		case ERROR:
			cmdCode = 4;
			break;

		default:
			break;
		}
		int totalLength=8;
		for (int i = 1; i < strings.length; i=i+2) {
			TLV tlv = new TLV(strings[i], strings[i+1]);
			totalLength= totalLength + 4 + tlv.length;
			arrayList.add(tlv);
		}
		lengthOfMessage = totalLength;
	}
	//constructor of Packet with data from input stream
	public Packet(DataInputStream in) throws EOFException,Exception{
		try {
			lengthOfMessage=in.readInt();
			byte[] packetByte = new byte[lengthOfMessage-4];
			in.readFully(packetByte, 0, lengthOfMessage-4);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packetByte);
			DataInputStream packetStream = new DataInputStream(byteArrayInputStream);
			cmdCode = packetStream.readShort();
			version = packetStream.readShort();
			int i = 8;
			while (i < lengthOfMessage) {
				TLV tlv = new TLV(packetStream);
				arrayList.add(tlv);
				i = i + 4 + tlv.length;
			}
		} finally {
			
		}
	}
	//get a TLV that have tag = "tag"
	public TLV getTLVbyTag(int tag) {
		for (TLV tlv : arrayList) {
			if(tlv.tag == (short)tag)
				return tlv;
		}
		return (new TLV());
	}
	//convert Packet to text syntax
	@Override
	public String toString() {
		String string="";
		switch (cmdCode) {
		case 0:
			string = "AUTHEN ";
			break;
		case 1:
			string = "INSERT ";
			break;
		case 2:
			string = "COMMIT ";
			break;
		case 3:
			string = "SELECT ";
			break;
		case 4:
			string = "ERROR ";
			break;
		default:
			break;
		}
		for (TLV tlv : arrayList) {
			string = string + tlv.toString();
		}
		return string;
	}
	//convert Packet to a string that present fields of Packet
	public String toPacket() {
		String packet = "" + lengthOfMessage + " " + cmdCode + " " + version + " ";
		for (TLV tlv : arrayList) {
			packet = packet + tlv.toPacket();
		}
		return packet;
	}
	//convert Packet to a a string, that if every a its char convert to a byte, this array byte will be send
	public String toBytes() {
		String lengthOfMessage = new String(ProcessBytes.intToByteArray(this.lengthOfMessage));
		String cmdCode = new String(ProcessBytes.shortToByteArray(this.cmdCode));
		String version = new String(ProcessBytes.shortToByteArray(this.version));
		String packet = lengthOfMessage + cmdCode + version;
		for (TLV tlv : arrayList) {
			packet = packet + tlv.toBytes();
		}
		return packet;
	}
}
