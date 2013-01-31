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
				String str = SerializeUtils.unserializeString(in);
				BswabePrvComp c = new BswabePrvComp(this.pub, str);

				SerializeUtils.unserializeElement(in, c.d);
				SerializeUtils.unserializeElement(in, c.dp);

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
		int prvCompsLen = this.comps.size();

		SerializeUtils.serializeElement(out, this.d);
		SerializeUtils.serializeUint32(out, prvCompsLen);

		Iterator<BswabePrvComp> it = this.comps.iterator();
		while (it.hasNext() == true)
		{
			BswabePrvComp current = it.next();
			SerializeUtils.serializeString(out, current.attr);
			SerializeUtils.serializeElement(out, current.d);
			SerializeUtils.serializeElement(out, current.dp);
		}
	}
}