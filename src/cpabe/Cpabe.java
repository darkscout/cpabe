package cpabe;

import it.unisa.dia.gas.jpbc.Element;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import bswabe.Bswabe;
import bswabe.BswabeCph;
import bswabe.BswabeCphKey;
import bswabe.BswabeElementBoolean;
import bswabe.BswabeMsk;
import bswabe.BswabePrv;
import bswabe.BswabePub;
import cpabe.policy.LangPolicy;

public class Cpabe
{

	/**
	 * @param args
	 * @author Junwei Wang(wakemecn@gmail.com)
	 * @author "Swen Weiland (swen.weiland@gmail.com)" 
	 */

	public void setup(String pubfile, String mskfile) throws IOException,
			ClassNotFoundException
	{
		byte[] pub_byte, msk_byte;
		BswabePub pub = new BswabePub();
		BswabeMsk msk = new BswabeMsk(pub);
		Bswabe.setup(pub, msk);

		/* store BswabePub into mskfile */
		pub_byte = pub.serialize();
		Common.spitFile(pubfile, pub_byte);

		/* store BswabeMsk into mskfile */
		msk_byte = msk.serialize();
		Common.spitFile(mskfile, msk_byte);
	}

	public void keygen(String pubfile, String prvfile, String mskfile,
			String attr_str) throws NoSuchAlgorithmException, IOException
	{
		BswabePub pub;
		BswabeMsk msk;
		byte[] pub_byte, msk_byte, prv_byte;

		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = new BswabePub();
		pub.initFromBuffer(pub_byte);

		/* get BswabeMsk from mskfile */
		msk_byte = Common.suckFile(mskfile);
		msk = new BswabeMsk(pub);
		msk.initFromBuffer(msk_byte);

		String[] attr_arr = LangPolicy.parseAttribute(attr_str);
		BswabePrv prv = Bswabe.keygen(pub, msk, attr_arr);

		/* store BswabePrv into prvfile */
		prv_byte = prv.serialize();
		Common.spitFile(prvfile, prv_byte);
	}

	public boolean enc(String pubfile, String policy, String inputfile,
			String encfile) throws Exception
	{
		CpabeEncryptedFile encFile;
		BswabePub pub;
		BswabeCph cph;
		BswabeCphKey keyCph;
		byte[] plt, aesBuf, pub_byte;
		Element m;

		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = new BswabePub();
		pub.initFromBuffer(pub_byte);

		/* encrypt */
		keyCph = Bswabe.enc(pub, policy);
		cph = keyCph.cph;
		m = keyCph.key;

		if (cph == null)
		{
			return false;
		}

		/* write file to encrypted */
		plt = Common.suckFile(inputfile);
		aesBuf = AESCoder.encrypt(m.toBytes(), plt);	
		
		encFile = new CpabeEncryptedFile(cph, aesBuf);
		Common.spitFile(encfile, encFile.serialize());
		
		return true;
	}

	public boolean dec(String pubfile, String prvfile, String encfile,
			String decfile) throws Exception
	{
		byte[] plt, prv_byte, pub_byte;
		CpabeEncryptedFile ciphertext;
		BswabeCph cph;
		BswabePrv prv;
		BswabePub pub;
		

		/* get BswabePub from pubfile */
		pub_byte = Common.suckFile(pubfile);
		pub = new BswabePub();
		pub.initFromBuffer(pub_byte);

		/* read ciphertext */
		ciphertext = new CpabeEncryptedFile(pub, encfile);
		cph = ciphertext.getCph();

		/* get BswabePrv form prvfile */
		prv_byte = Common.suckFile(prvfile);
		prv = new BswabePrv(pub);
		prv.initFromBuffer(prv_byte);

		BswabeElementBoolean beb = Bswabe.dec(pub, prv, cph);
		if (beb.isSuccessful() == false)
		{
			return false;
		}

		plt = AESCoder.decrypt(beb.getElement().toBytes(), ciphertext.getAesBuf());
		Common.spitFile(decfile, plt);
		
		return true;
	}

}
