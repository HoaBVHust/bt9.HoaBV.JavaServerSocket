package vn.topica.itlab.javasocket;

import java.io.DataInputStream;
enum Tag {Key, PhoneNumber, Name, ResultCode}
//This is a block include tag, length and value
public class TLV {
	short tag;
	short length=0;
	String value;

	public TLV() {
	}
	//constructor of TLV with text syntax
	public TLV(String tag, String value) {
		Tag tagEnum=Tag.valueOf(tag);
		switch (tagEnum) {
		case Key:
			this.tag = 0;
			break;
		case PhoneNumber:
			this.tag = 1;
			break;
		case Name:
			this.tag = 2;
			break;
		case ResultCode:
			this.tag = 3;
			break;
		default:
			break;
		}
		length = (short)value.getBytes().length;
		this.value = value;
	}
	//constructor of TLV with data from input stream
	public TLV(DataInputStream packetStream) {
		try {
			tag = packetStream.readShort();
			length = packetStream.readShort();
			byte[] bs = new byte[length];
			packetStream.readFully(bs, 0, length);
			value = new String(bs);
		} catch (Exception e) {
			System.out.println("error tlv");
			e.printStackTrace();
		}
	}
	//convert TLV to text syntax
	@Override
	public String toString() {
		switch (tag) {
		case 0:
			return "Key "+value+" ";
		case 1:
			return "PhoneNumber "+value+" ";
		case 2:
			 return "Name " + value + " ";
		case 3:
			 return "ResultCode " +  value + " ";
		default:
			return "";
		}
	}
	//convert TLV to a string that present fields of Packet
	public String toPacket() {
		return "" + tag + " " + length + " " + value + " ";
	}
	//convert TLV to a a string, that if every a its char is converted to a byte, this array byte will be send
	public String toBytes() {
		String tag = new String(ProcessBytes.shortToByteArray(this.tag));
		String length = new String(ProcessBytes.shortToByteArray(this.length));
		return tag + length + value;
	}
}
