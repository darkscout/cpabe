package bswabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unisa.dia.gas.jpbc.Element;

public class BswabeMsk implements BswabeSerializeable
{
	/*
	 * A master secret key
	 */
	public Element beta; /* Z_r */
	public Element g_alpha; /* G_2 */
	
	@Override
	public void initFromBuffer(BswabePub pub, byte[] buffer) throws IOException
	{
		ByteArrayInputStream bain = new ByteArrayInputStream(buffer);
		DataInputStream in = new DataInputStream(bain);
		
		this.initFromBuffer(pub, in);
		
		in.close();
	}
	
	@Override
	public void initFromBuffer(BswabePub pub, DataInputStream in)
			throws IOException
	{
		this.beta = pub.p.getZr().newElement();
		this.g_alpha = pub.p.getG2().newElement();

		SerializeUtils.unserializeElement(in, this.beta);
		SerializeUtils.unserializeElement(in, this.g_alpha);
	}
	
	@Override
	public byte[] serialize() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);

		this.serialize(out);
		
		out.close();
		baos.close();
		
		return baos.toByteArray();
	}
	
	@Override
	public void serialize(DataOutputStream out) throws IOException
	{
		SerializeUtils.serializeElement(out, this.beta);
		SerializeUtils.serializeElement(out, this.g_alpha);
	}
}
