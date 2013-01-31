package bswabe;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 *
 */
import it.unisa.dia.gas.jpbc.CurveParameters;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.DefaultCurveParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

public class Bswabe
{
	private static final String curveParams = "type a\n"
			+ "q 87807107996633125224377819847540498158068831994142082"
			+ "1102865339926647563088022295707862517942266222142315585"
			+ "8769582317459277713367317481324925129998224791\n"
			+ "h 12016012264891146079388821366740534204802954401251311"
			+ "822919615131047207289359704531102844802183906537786776\n"
			+ "r 730750818665451621361119245571504901405976559617\n"
			+ "exp2 159\n" + "exp1 107\n" + "sign1 1\n" + "sign0 1\n";

	/*
	 * Generate a public key and corresponding master secret key.
	 */
	public static void setup(BswabePub pub, BswabeMsk msk)
	{
		Element alpha, beta_inv;

		CurveParameters params = new DefaultCurveParameters()
				.load(new ByteArrayInputStream(curveParams.getBytes()));

		pub.pairingDesc = curveParams;
		pub.p = PairingFactory.getPairing(params);
		Pairing pairing = pub.p;

		pub.g = pairing.getG1().newElement();
		pub.gp = pairing.getG2().newElement();
		alpha = pairing.getZr().newElement();
		msk.beta = pairing.getZr().newElement();

		alpha.setToRandom();
		msk.beta.setToRandom();
		pub.g.setToRandom();
		pub.gp.setToRandom();

		msk.g_alpha = pub.gp.duplicate();
		msk.g_alpha.powZn(alpha);

		beta_inv = msk.beta.duplicate();
		beta_inv.invert();
		pub.f = pub.g.duplicate();
		pub.f.powZn(beta_inv);

		pub.h = pub.g.duplicate();
		pub.h.powZn(msk.beta);

		pub.g_hat_alpha = pairing.pairing(pub.g, msk.g_alpha);
	}

	/*
	 * Generate a private key with the given set of attributes.
	 */
	public static BswabePrv keygen(BswabePub pub, BswabeMsk msk, String[] attrs)
			throws NoSuchAlgorithmException
	{
		BswabePrv prv = new BswabePrv(pub);
		Element g_r, r, beta_inv;
		Pairing pairing;

		/* initialize */
		pairing = pub.p;
		r = pairing.getZr().newElement();

		/* compute */
		r.setToRandom();
		g_r = pub.gp.duplicate();
		g_r.powZn(r);

		prv.d = msk.g_alpha.duplicate();
		prv.d.mul(g_r);
		beta_inv = msk.beta.duplicate();
		beta_inv.invert();
		prv.d.powZn(beta_inv);

		prv.comps = new ArrayList<BswabePrvComp>();
		
		for (int i = 0; i < attrs.length; i++)
		{
			BswabePrvComp comp = new BswabePrvComp(pub, attrs[i]);
			Element h_rp;
			Element rp;

			h_rp = pairing.getG2().newElement();
			rp = pairing.getZr().newElement();

			CommonHelper.elementFromString(h_rp, comp.attr);
			rp.setToRandom();

			h_rp.powZn(rp);

			comp.d = g_r.duplicate();
			comp.d.mul(h_rp);
			comp.dp = pub.g.duplicate();
			comp.dp.powZn(rp);

			prv.comps.add(comp);
		}

		return prv;
	}

	/*
	 * Delegate a subset of attribute of an existing private key.
	 */
	public static BswabePrv delegate(BswabePub pub, BswabePrv prv_src,
			String[] attrs_subset) throws NoSuchAlgorithmException,
			IllegalArgumentException
	{

		BswabePrv prv = new BswabePrv(pub);
		Element g_rt, rt, f_at_rt;
		Pairing pairing;

		/* initialize */
		pairing = pub.p;
		prv.d = pairing.getG2().newElement();

		g_rt = pairing.getG2().newElement();
		rt = pairing.getZr().newElement();
		f_at_rt = pairing.getZr().newElement();

		/* compute */
		rt.setToRandom();
		
		f_at_rt = pub.f.duplicate();
		f_at_rt.powZn(rt);
		prv.d = prv_src.d.duplicate();
		prv.d.mul(f_at_rt);

		g_rt = pub.g.duplicate();
		g_rt.powZn(rt);

		prv.comps = new ArrayList<BswabePrvComp>();

		for (int i = 0; i < attrs_subset.length; i++)
		{
			BswabePrvComp comp = new BswabePrvComp(pub, attrs_subset[i]);
			Element h_rtp;
			Element rtp;

			BswabePrvComp comp_src = null;
			Iterator<BswabePrvComp> it = prv_src.comps.iterator();			
			while (it.hasNext() == true)
			{
				BswabePrvComp current = it.next();
				
				if (current.attr == comp.attr)
				{
					comp_src = current;
					break;
				}
			}

			if (comp_src == null)
			{
				throw new IllegalArgumentException("comp_src_init == false");
			}

			h_rtp = pairing.getG2().newElement();
			rtp = pairing.getZr().newElement();

			CommonHelper.elementFromString(h_rtp, comp.attr);
			rtp.setToRandom();

			h_rtp.powZn(rtp);

			comp.d = g_rt.duplicate();
			comp.d.mul(h_rtp);
			comp.d.mul(comp_src.d);

			comp.dp = pub.g.duplicate();
			comp.dp.powZn(rtp);
			comp.dp.mul(comp_src.dp);

			prv.comps.add(comp);
		}

		return prv;
	}

	/*
	 * Pick a random group element and encrypt it under the specified access
	 * policy. The resulting ciphertext is returned and the Element given as an
	 * argument (which need not be initialized) is set to the random group
	 * element.
	 * 
	 * After using this function, it is normal to extract the random data in m
	 * using the pbc functions element_length_in_bytes and element_to_bytes and
	 * use it as a key for hybrid encryption.
	 * 
	 * The policy is specified as a simple string which encodes a postorder
	 * traversal of threshold tree defining the access policy. As an example,
	 * 
	 * "foo bar fim 2of3 baf 1of2"
	 * 
	 * specifies a policy with two threshold gates and four leaves. It is not
	 * possible to specify an attribute with whitespace in it (although "_" is
	 * allowed).
	 * 
	 * Numerical attributes and any other fancy stuff are not supported.
	 * 
	 * Returns null if an error occurred, in which case a description can be
	 * retrieved by calling bswabe_error().
	 */
	public static BswabeCphKey enc(BswabePub pub, String policy)
			throws Exception
	{
		BswabeCphKey keyCph = new BswabeCphKey();
		BswabeCph cph = new BswabeCph(pub);
		Element s, m;

		/* initialize */
		Pairing pairing = pub.p;
		s = pairing.getZr().newElement();
		m = pairing.getGT().newElement();
		cph.p = new BswabePolicy(policy);

		/* compute */
		m.setToRandom();
		s.setToRandom();
		
		cph.cs = pub.g_hat_alpha.duplicate();
		cph.cs.powZn(s);
		cph.cs.mul(m);

		cph.c = pub.h.duplicate();
		cph.c.powZn(s);

		cph.p.fillPolicy(pub, s);

		keyCph.cph = cph;
		keyCph.key = m;

		return keyCph;
	}

	/*
	 * Decrypt the specified ciphertext using the given private key, filling in
	 * the provided element m (which need not be initialized) with the result.
	 * 
	 * Returns true if decryption succeeded, false if this key does not satisfy
	 * the policy of the ciphertext (in which case m is unaltered).
	 */
	public static BswabeElementBoolean dec(BswabePub pub, BswabePrv prv,
			BswabeCph cph)
	{
		Element t;
		Element m;

		cph.p.checkSatisfy(prv, false);
		if (cph.p.satisfiable == false)
		{
//			System.out.println("cannot decrypt, attributes in key do not satisfy policy");
			return new BswabeElementBoolean(false, null);
		}

		t = decFlatten(cph.p, prv, pub);

		m = cph.cs.duplicate();
		m.mul(t); /* num_muls++; */

		t = pub.p.pairing(cph.c, prv.d);
		t.invert();
		m.mul(t); /* num_muls++; */

		return new BswabeElementBoolean(true, m);
	}

	private static Element decFlatten(BswabePolicy p, BswabePrv prv, BswabePub pub)
	{
		Element r = pub.p.getGT().newElement();
		Element one = pub.p.getZr().newElement();
		one.setToOne();
		r.setToOne();

		decNodeFlatten(r, one, p, prv, pub);
		
		return r;
	}

	private static void decNodeFlatten(Element r, Element exp, BswabePolicy p,
			BswabePrv prv, BswabePub pub)
	{
		if (p.children.isEmpty() == true)
			decLeafFlatten(r, exp, p, prv, pub);
		else
			decInternalFlatten(r, exp, p, prv, pub);
	}

	private static void decLeafFlatten(Element r, Element exp, BswabePolicy p,
			BswabePrv prv, BswabePub pub)
	{
		BswabePrvComp c;
		Element s, t;

		c = prv.comps.get(p.attri);

		s = pub.p.pairing(p.c, c.d); /* num_pairings++; */
		t = pub.p.pairing(p.cp, c.dp); /* num_pairings++; */
		
		t.invert();
		s.mul(t); /* num_muls++; */
		s.powZn(exp); /* num_exps++; */

		r.mul(s); /* num_muls++; */
	}

	private static void decInternalFlatten(Element r, Element exp,
			BswabePolicy p, BswabePrv prv, BswabePub pub)
	{
		Element t, expnew;

		t = pub.p.getZr().newElement();

		Iterator<BswabePolicy> it = p.sat.iterator();			
		while (it.hasNext() == true)
		{
			BswabePolicy current = it.next();
			CommonHelper.lagrangeCoef(t, p.sat, current);
			expnew = exp.duplicate();
			expnew.mul(t); /* num_muls++; */
			decNodeFlatten(r, expnew, current, prv, pub); 
		}
	}
}
