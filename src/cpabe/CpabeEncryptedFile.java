package cpabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import bswabe.BswabeCph;
import bswabe.BswabePub;
import bswabe.BswabeSerializeable;
import bswabe.SerializeUtils;

/**
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class CpabeEncryptedFile implements BswabeSerializeable
{
	private BswabeCph cph;
	private byte[] aesBuf;
	
	public CpabeEncryptedFile(BswabeCph cph, byte[] aesBuf)
	{
		this.cph = cph;
		this.aesBuf = aesBuf;
	}
	
	public CpabeEncryptedFile(BswabePub pub, String encfile) throws Exception
	{
		InputStream is = new FileInputStream(encfile);
		DataInputStream in = new DataInputStream(is);

		this.cph = new BswabeCph();
		
		this.initFromBuffer(pub, in);

		in.close();
		is.close();		
	}

	/**
	 * @return the cph
	 */
	public BswabeCph getCph()
	{
		return cph;
	}

	/**
	 * @return the aesBuf
	 */
	public byte[] getAesBuf()
	{
		return aesBuf;
	}

	/**
	 * @param cph the cph to set
	 */
	public void setCph(BswabeCph cph)
	{
		this.cph = cph;
	}

	/**
	 * @param aesBuf the aesBuf to set
	 */
	public void setAesBuf(byte[] aesBuf)
	{
		this.aesBuf = aesBuf;
	}

	
	
	@Override
	public void initFromBuffer(BswabePub pub, byte[] buffer) throws IOException
	{
		ByteArrayInputStream bain = new ByteArrayInputStream(buffer);
		DataInputStream in = new DataInputStream(bain);

		this.initFromBuffer(pub, in);
		
		in.close();
		bain.close();
	}

	@Override
	public void initFromBuffer(BswabePub pub, DataInputStream in)
			throws IOException
	{
		int len;
		byte[] cphBuf;

		/* read aes buf */
		len = SerializeUtils.unserializeUint32(in);
		this.aesBuf = new byte[len];
		in.read(this.aesBuf);
		
		/* read cph buf */
		len = SerializeUtils.unserializeUint32(in);
		cphBuf = new byte[len];
		in.read(cphBuf);
		
		this.cph.initFromBuffer(pub, cphBuf);
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
		/* write aes_buf */
		SerializeUtils.serializeUint32(out, aesBuf.length);
		out.write(aesBuf);

		/* write cph_buf */
		byte[] cphBuf = this.cph.serialize();
		SerializeUtils.serializeUint32(out, cphBuf.length);
		out.write(cphBuf);
	}

}
