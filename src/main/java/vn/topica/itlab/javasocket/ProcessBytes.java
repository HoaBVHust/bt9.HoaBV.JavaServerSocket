package vn.topica.itlab.javasocket;

public class ProcessBytes {
	//convert a "int" number to a array 4 bytes that present this "int" number
	public static byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	//convert a "short" number to a array 2 bytes that present this "short" number
	public static byte[] shortToByteArray(short value) {
		return new byte[] { (byte) (value >>> 8), (byte) value };
	}
}
