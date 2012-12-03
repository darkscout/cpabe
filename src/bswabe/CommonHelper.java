package bswabe;

import it.unisa.dia.gas.jpbc.Element;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class CommonHelper
{
	public static void elementFromString(Element h, String s)
			throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digest = md.digest(s.getBytes());
		h.setFromHash(digest, 0, digest.length);
	}

	public static void lagrangeCoef(Element r, LinkedList<BswabePolicy> sat,
			BswabePolicy ignored)
	{
		Element t = r.duplicate();
		Iterator<BswabePolicy> it = sat.iterator();

		r.setToOne();
		while (it.hasNext() == true)
		{
			BswabePolicy current = it.next();
			
			if (current == ignored)
				continue;

			int i = ignored.index + 1;
			int j = current.index + 1;

			t.set(-j);
			r.mul(t); /* num_muls++; */

			t.set(i - j);
			t.invert();
			r.mul(t); /* num_muls++; */
		}
	}

}
