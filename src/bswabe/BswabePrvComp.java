package bswabe;

import it.unisa.dia.gas.jpbc.Element;

public class BswabePrvComp
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
	
}
