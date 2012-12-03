package bswabe;

import it.unisa.dia.gas.jpbc.Element;

/**
 * @author Junwei Wang(wakemecn@gmail.com)
 * @author "Swen Weiland (swen.weiland@gmail.com)"
 *
 * This class carries the result of a (de)crypt operation 
 * 
 */
public class BswabeElementBoolean 
{
	private Element e;
	private boolean successful;
	
	
	public BswabeElementBoolean(boolean result, Element e)
	{
		this.successful = result;
		this.e = e;
	}


	/**
	 * @return the e
	 */
	public Element getElement()
	{
		return e;
	}


	/**
	 * @return the result
	 */
	public boolean isSuccessful()
	{
		return successful;
	}
}
