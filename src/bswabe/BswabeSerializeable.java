package bswabe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public interface BswabeSerializeable
{
	//public abstract BswabeSerializeable(BswabePub pub);
	
	/**
	 * Initialize object values from a byte buffer created by the serialize method
	 * 
	 * @param buffer A buffer created by the serialize method
	 * @throws IOException 
	 */
	public abstract void initFromBuffer(BswabePub pub, byte[] buffer) throws IOException;

	public abstract void initFromBuffer(BswabePub pub, DataInputStream in) throws IOException;

	/**
	 * Serialized the current object to a byte buffer 
	 * @return
	 * @throws IOException
	 */
	public byte[] serialize() throws IOException;
	
	
	public abstract void serialize(DataOutputStream out) throws IOException;
}
