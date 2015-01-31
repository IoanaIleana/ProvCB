/**
  * Copyright 2013, 2014 Ioana Ileana @ Telecom ParisTech 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.

  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/

package flatProvenance;


import instance.ChasedInstance;
import instance.ProvenanceSymbol;

//a conjunct in a provenance formula
public class FlatConjunct 
{
	//real provenance content, as a bitmask
	private long[] m_symbolMask;
	
	//cost
	private int m_cost;
	
	//number of needed longs 
	public static int m_countLongsProv = 1;
	
	public int cost()
	{
		return m_cost;
	}

	public FlatConjunct()
	{
		m_symbolMask=new long[m_countLongsProv];
		m_cost = 0;
	}
	
	public void addSymbol(ProvenanceSymbol symbol)
	{
		int longplace = symbol.m_index/63;
		int longind = symbol.m_index%63;
		m_symbolMask[longplace]|=((long)1)<<longind;
		
		//suppose we don't deal with duplicates here
		m_cost++;
	}
	
	public static FlatConjunct getProduct(FlatConjunct conj1, FlatConjunct conj2)
	{
		FlatConjunct prod = new FlatConjunct();
		for (int i = 0; i< m_countLongsProv;++i)
		{
			prod.m_symbolMask[i] = conj1.m_symbolMask[i] | conj2.m_symbolMask[i];
			
			for (int j = 0; j<62; ++j)
				if ((prod.m_symbolMask[i] & (((long)1)<<j)) !=0 )
					prod.m_cost++;
			
		}
		return prod;	
	}
	
	public boolean isSubsumedBy(FlatConjunct other)
	{
		for (int k = 0; k< m_countLongsProv;++k)
			if ((other.m_symbolMask[k]&m_symbolMask[k])!=other.m_symbolMask[k]) 
				return false;
		return true;
	}
	
	public String toString()
	{
		String str="";
		for (int k = 0; k< m_countLongsProv;++k)
			for (int i = 0; i<62; ++i)
				if ((m_symbolMask[k] & (((long)1)<<i)) !=0 )
					str+=ChasedInstance.zeInstance.m_provenanceSymbols.get(k*63+i).m_name+",";
		return str;
	}
}
