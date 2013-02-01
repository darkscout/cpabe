package bswabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unisa.dia.gas.jpbc.Element;

public class BswabePrvComp implements BswabeSerializeable
{
	/* these actually get serialized */
	String attr;
	Element d; /* G_2 */
	Element dp; /* G_2 */

	/* only used during dec */
	int used;
	Element z; /* G_1 */
	Element zp; /* G_1 */
	
	
	public BswabePrvComp(BswabePub pub, String attr)
	{
		this.attr = attr;
		
		this.d = pub.p.getG2().newElement();
		this.dp = pub.p.getG2().newElement();
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
	public void initFromBuffer(DataInputStream in) throws IOException
	{
		this.attr = SerializeUtils.unserializeString(in);
		SerializeUtils.unserializeElement(in, this.d);
		SerializeUtils.unserializeElement(in, this.dp);		
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
		SerializeUtils.serializeString(out, this.attr);
		SerializeUtils.serializeElement(out, this.d);
		SerializeUtils.serializeElement(out, this.dp);
	}
	
}
