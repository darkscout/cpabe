package bswabe;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 */
public class BswabePolicy implements BswabeSerializeable, Comparable<BswabePolicy>
{
	BswabePub pub;
	
	/* k=1 if leaf, otherwise threshold */
	int k;
	/* attribute string if leaf, otherwise null */
	String attr;
	Element c; /* G_1 only for leaves */
	Element cp; /* G_1 only for leaves */
	/* array of BswabePolicy and length is 0 for leaves */
	LinkedList<BswabePolicy> children;

	/* only used during encryption */
	BswabePolynomial q;

	/* only used during description */
	boolean satisfiable;
	int min_leaves;
	int attri;
	int index;
	LinkedList<BswabePolicy> sat;
	
	/**
	 * Default constructor
	 */
	public BswabePolicy(BswabePub pub)
	{
		this.pub = pub;
		this.satisfiable = false;
		this.sat = new LinkedList<BswabePolicy>();
		this.children = new LinkedList<BswabePolicy>();
	}
	
	/**
	 * Constructor from String
	 * 
	 * @param s Policy as String
	 * @throws Exception
	 */
	public BswabePolicy(String s) throws Exception
	{
		super();
		this.parsePolicyPostfix(s);
	}

	private void parsePolicyPostfix(String s) throws Exception
	{
		int child_index = 0;
		String[] toks;
		String tok;
		LinkedList<BswabePolicy> stack = new LinkedList<BswabePolicy>();
		BswabePolicy root;

		toks = s.split(" ");

		for (int i=0; i < toks.length; i++)
		{
			tok = toks[i];
			if (!tok.contains("of"))
			{
				// add a leaf node
				stack.add(baseNode(1, tok, child_index));
				child_index++;
			}
			else
			{
				/* parse k of n node */
				String[] k_n = tok.split("of");
				int k = Integer.parseInt(k_n[0]);
				int n = Integer.parseInt(k_n[1]);

				if (k < 1)
				{
					throw new Exception("error parsing " + s
							+ ": trivially satisfied operator " + tok);
				}
				else if (k > n)
				{
					throw new Exception("error parsing " + s
							+ ": unsatisfiable operator " + tok);
				}
				else if (n == 1)
				{
					throw new Exception("error parsing " + s
							+ ": indentity operator " + tok);
				}
				else if (n > stack.size())
				{
					throw new Exception("error parsing " + s
							+ ": stack underflow at " + tok);
				}

				/* pop n things and fill in children */
				BswabePolicy node = baseNode(k, null, 0);
				node.children = new LinkedList<BswabePolicy>();
				
				List<BswabePolicy> tmp = stack.subList(stack.size() - n, stack.size());
				node.children.addAll(tmp);
				// remove children from stack
				tmp.clear(); 
				
				/* push result */
				stack.add(node);
				child_index = 0;
			}
		}

		if (stack.size() > 1)
		{
			throw new Exception("error parsing " + s
					+ ": extra node left on the stack");
		}
		else if (stack.size() < 1)
		{
			throw new Exception("error parsing " + s + ": empty policy");
		}

		root = stack.getFirst();

		// make this == root
		this.k = root.k;
		this.attr = root.attr;
		this.children = root.children;
		this.q = root.q;		
	}

	/**
	 * Creates a Parent/Base node
	 * @param k
	 * @param attr
	 * @return
	 */
	private BswabePolicy baseNode(int k, String attr, int index)
	{
		BswabePolicy p = new BswabePolicy(this.pub);
		p.k = k;
		p.attr = attr;
		p.index = index;
		p.q = null;

		return p;
	}

	public boolean checkSatisfy(BswabePrv prv, boolean fullcheck)
	{
		return this.checkSatisfy(this, prv, fullcheck);
	}

	// pick_sat_min_leaves
	private boolean checkSatisfy(BswabePolicy p, BswabePrv prv, boolean fullcheck)
	{
		p.satisfiable = false;
		
		// check leaf node if satisfiable 
		if (p.children.isEmpty() == true)
		{			
			p.min_leaves = 1;
			
			for (int i = 0; i < prv.comps.size(); i++)
			{
				String prvAttr = prv.comps.get(i).attr;
				if (prvAttr.compareTo(p.attr) == 0)
				{
//					System.out.println("=satisfy= " + p.attr + " == " + prvAttr);
					p.satisfiable = true;
					p.attri = i;
					
					return true;
				}
			}
		}
		// iterate child tree recursively 
		else
		{
			p.sat = new LinkedList<BswabePolicy>();
			p.min_leaves = 0;
			Iterator<BswabePolicy> it = p.children.iterator();			
			while (it.hasNext() == true)
			{
				BswabePolicy current = it.next();

				checkSatisfy(current, prv, fullcheck);

				if (current.satisfiable == true)
				{
					p.sat.add(current);				
					p.min_leaves += current.min_leaves;
					
					if ((fullcheck == false) && (p.sat.size() >= p.k))
					{
						// shortcut - already enough attributes matching
						break;
					}
				}				
			}
						
			// final check
			if (p.sat.size() >= p.k)
			{
				p.satisfiable = true;
				
				Collections.sort(p.sat);
				
				return true;
			}
		}
		
		return false;
	}

	public void fillPolicy(BswabePub pub, Element e)
			throws NoSuchAlgorithmException
	{
		this.fillPolicy(this, pub, e);
	}

	private void fillPolicy(BswabePolicy p, BswabePub pub, Element e)
			throws NoSuchAlgorithmException
	{
		Pairing pairing = pub.p;

		p.q = randPoly(p.k - 1, e);

		if (p.children.isEmpty() == true)
		{
			Element h = pairing.getG2().newElement();
			CommonHelper.elementFromString(h, p.attr);
			
			p.c = pub.g.duplicate();
			p.c.powZn(p.q.coef[0]);
			p.cp = h.duplicate();
			p.cp.powZn(p.q.coef[0]);
		}
		else
		{
			Element r = pairing.getZr().newElement();
			Element t = pairing.getZr().newElement();
			
			Iterator<BswabePolicy> it = p.children.iterator();
			while (it.hasNext() == true)
			{
				BswabePolicy current = it.next();
				r.set(current.index + 1); 
				evalPoly(t, p.q, r);
				fillPolicy(current, pub, t);
			}
		}

	}

	private void evalPoly(Element r, BswabePolynomial q, Element x)
	{
		Element t = r.duplicate();

		r.setToZero();
		t.setToOne();

		for (int i = 0; i < q.deg + 1; i++)
		{
			/* r += q->coef[i] * t */
			Element s = q.coef[i].duplicate();
			s.mul(t);
			r.add(s);

			/* t *= x */
			t.mul(x);
		}
	}

	private BswabePolynomial randPoly(int deg, Element zeroVal)
	{
		BswabePolynomial q = new BswabePolynomial();
		q.deg = deg;
		q.coef = new Element[deg + 1];

		// first coef = zeroVal
		q.coef[0] = zeroVal.duplicate();

		// randomize the rest
		for (int i = 1; i < deg + 1; i++)
		{
			q.coef[i] = zeroVal.duplicate();
			q.coef[i].setToRandom();
		}

		return q;
	}

	@Override
	public int compareTo(BswabePolicy o)
	{
		int k = this.min_leaves;
		int l = o.min_leaves;
		
		if (k < l)
			return -1;
		else if (k == l)
			return 0;
		else
			return 1;
	}
	
	public String toString()
	{
		if (this.attr == null)
		{
			return this.children.toString();
		}
		else
		{
			return this.attr + " " + (this.index + 1);
		}
	}

	
	@Override
	public void serialize(DataOutputStream out) throws IOException
	{
		serializePolicy(out, this);
	}

	@Override
	public byte[] serialize() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);

		serializePolicy(out, this);
		
		out.close();
		baos.close();
		
		return baos.toByteArray();
	}
	
	private static void serializePolicy(DataOutputStream out, BswabePolicy p) 
			throws IOException
	{
		SerializeUtils.serializeUint32(out, p.k);
		SerializeUtils.serializeUint32(out, p.children.size());

		if (p.children.isEmpty() == true)
		{
			SerializeUtils.serializeString(out, p.attr);
			SerializeUtils.serializeElement(out, p.c);
			SerializeUtils.serializeElement(out, p.cp);
		}
		else
		{
			Iterator<BswabePolicy> it = p.children.iterator();
			while (it.hasNext() == true)
			{
				BswabePolicy current = it.next();
				serializePolicy(out, current);
			}
		}
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
	public void initFromBuffer(DataInputStream in) throws IOException
	{
		initChildFromBuffer(in, this);
	}
	
	private void initChildFromBuffer(DataInputStream in, BswabePolicy p) throws IOException
	{		
		p.k = SerializeUtils.unserializeUint32(in);
		int n = SerializeUtils.unserializeUint32(in);

		/* children */
		if (n == 0)
		{
			p.attr = SerializeUtils.unserializeString(in);

			p.c = this.pub.p.getG1().newElement();
			p.cp = this.pub.p.getG1().newElement();

			SerializeUtils.unserializeElement(in, p.c);
			SerializeUtils.unserializeElement(in, p.cp);
		}
		else
		{
			p.children = new LinkedList<BswabePolicy>();
			for (int i = 0; i < n; i++)
			{
				BswabePolicy pol = new BswabePolicy(this.pub);
				pol.index = i; // set index for lagrangeCoef 
				initChildFromBuffer(in, pol);
				p.children.add(pol);
			}
		}
	}

}
