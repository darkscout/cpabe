package bswabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class BswabeCph implements BswabeSerializeable
{
	/*
	 * A ciphertext. Note that this library only handles encrypting a single
	 * group element, so if you want to encrypt something bigger, you will have
	 * to use that group element as a symmetric key for hybrid encryption (which
	 * you do yourself).
	 */
	public BswabePub pub;
	public Element cs; /* G_T */
	public Element c; /* G_1 */
	public BswabePolicy p;
	
	public BswabeCph(BswabePub pub)
	{
		this.pub = pub;
	}
	
	@Override
	public void initFromBuffer(byte[] buffer) throws IOException
	{
		ByteArrayInputStream bain = new ByteArrayInputStream(buffer);
		DataInputStream in = new DataInputStream(bain);

		this.initFromBuffer(in);
		
		in.close();
		bain.close();
	}
	
	@Override
	public void initFromBuffer(DataInputStream in)
			throws IOException
	{
		this.cs = this.pub.p.getGT().newElement();
		this.c = this.pub.p.getG1().newElement();

		SerializeUtils.unserializeElement(in, this.cs);
		SerializeUtils.unserializeElement(in, this.c);

		this.p = new BswabePolicy(this.pub);
		this.p.initFromBuffer(in);
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
		SerializeUtils.serializeElement(out, this.cs);
		SerializeUtils.serializeElement(out, this.c);
		this.p.serialize(out);
	}
}
