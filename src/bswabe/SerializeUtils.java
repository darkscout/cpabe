package bswabe;

import it.unisa.dia.gas.jpbc.Element;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class SerializeUtils
{

	/* potential problem: the number to be serialize is less than 2^31 */
	public static void serializeUint32(DataOutputStream out, int k)
			throws IOException
	{
		for (int i = 3; i >= 0; i--)
		{
			byte b = (byte) ((k & (0x000000ff << (i * 8))) >> (i * 8));
			out.writeByte(b);
		}
	}

	public static int unserializeUint32(DataInputStream in) throws IOException
	{
		int r = 0;

		for (int i = 3; i >= 0; i--)
		{
			r |= in.readUnsignedByte() << (i * 8);
		}
		return r;
	}

	public static void serializeElement(DataOutputStream out, Element e)
			throws IOException
	{
		byte[] arr_e = e.toBytes();
		serializeUint32(out, arr_e.length);
		out.write(arr_e);
	}

	public static void unserializeElement(DataInputStream in, Element e)
			throws IOException
	{
		int len;
		byte[] e_byte;

		len = unserializeUint32(in);

		if (len > in.available())
		{
			throw new IOException("Not enough bytes left (" + len + ">"
					+ in.available() + ")");
		}

		e_byte = new byte[len];
		in.read(e_byte);
		e.setFromBytes(e_byte);
	}

	public static void serializeString(DataOutputStream out, String s)
			throws IOException
	{
		// out.writeUTF(s);

		// libbswabe
		byte[] b = s.getBytes();
		for (int i = 0; i < b.length; i++)
		{
			out.writeByte(b[i]);
		}
		out.writeByte(0); // termination
	}

	public static String unserializeString(DataInputStream in)
			throws IOException
	{

		// String str = in.readUTF();
		// return str;

		// libbswabe version
		StringBuilder sb = new StringBuilder();
		char c = (char) in.readUnsignedByte();
		while ((c != 0) && (in.available() > 0))
		{
			sb.append(c);
			c = (char) in.readUnsignedByte();
		}
		return sb.toString();
	}
}
