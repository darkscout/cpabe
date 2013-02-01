package bswabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class BswabePrv implements BswabeSerializeable
{
	BswabePub pub;
	/*
	 * A private key
	 */
	Element d; /* G_2 */
	ArrayList<BswabePrvComp> comps; /* BswabePrvComp */
	
	public BswabePrv(BswabePub pub)
	{
		this.pub = pub;
		this.comps = new ArrayList<BswabePrvComp>();
	}
	
	@Override
	public void initFromBuffer(byte[] buffer) throws IOException
	{
		ByteArrayInputStream bain = new ByteArrayInputStream(buffer);
		DataInputStream in = new DataInputStream(bain);
		
		this.initFromBuffer(in);
		
		in.close();
	}
	
	@Override
	public void initFromBuffer(DataInputStream in)
			throws IOException
	{
			int len;

			this.d = this.pub.p.getG2().newElement();
			SerializeUtils.unserializeElement(in, this.d);

			this.comps = new ArrayList<BswabePrvComp>();
			len = SerializeUtils.unserializeUint32(in);

			for (int i = 0; i < len; i++)
			{
				BswabePrvComp c = new BswabePrvComp(this.pub, null);
				c.initFromBuffer(in);

				this.comps.add(c);
			}
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
		SerializeUtils.serializeElement(out, this.d);
		SerializeUtils.serializeUint32(out, this.comps.size());

		Iterator<BswabePrvComp> it = this.comps.iterator();
		while (it.hasNext() == true)
		{
			BswabePrvComp current = it.next();			
			current.serialize(out);
		}
	}
}