package bswabe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class BswabePub implements BswabeSerializeable
{
	/*
	 * A public key
	 */
	public String pairingDesc;
	public Pairing p;
	public Element g; /* G_1 */
	public Element h; /* G_1 */
	public Element f; /* G_1 */
	public Element gp; /* G_2 */
	public Element g_hat_alpha; /* G_T */
	
	
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
		this.pairingDesc = SerializeUtils.unserializeString(in);

		CurveParameters params = new DefaultCurveParameters()
				.load(new ByteArrayInputStream(this.pairingDesc.getBytes()));
		this.p = PairingFactory.getPairing(params);
		Pairing pairing = this.p;

		this.g = pairing.getG1().newElement();
		this.h = pairing.getG1().newElement();
		this.gp = pairing.getG2().newElement();
		this.g_hat_alpha = pairing.getGT().newElement();

		SerializeUtils.unserializeElement(in, this.g);
		SerializeUtils.unserializeElement(in, this.h);
		SerializeUtils.unserializeElement(in, this.gp);
		SerializeUtils.unserializeElement(in, this.g_hat_alpha);
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
		SerializeUtils.serializeString(out, this.pairingDesc);
		SerializeUtils.serializeElement(out, this.g);
		SerializeUtils.serializeElement(out, this.h);
		SerializeUtils.serializeElement(out, this.gp);
		SerializeUtils.serializeElement(out, this.g_hat_alpha);
	}
}
