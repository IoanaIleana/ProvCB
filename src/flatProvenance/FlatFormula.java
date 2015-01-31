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
import instance.Switches;

import java.util.ArrayList;

//a provenance formula as a sum of conjuncts
public class FlatFormula 
{
	//list of provenance conjuncts
	public ArrayList<FlatConjunct> m_conjuncts;
	
	//aux list for product
	public static ArrayList<FlatConjunct> prodList= new ArrayList<FlatConjunct>(50);
	
	//aux lists for sum
	public static ArrayList<FlatConjunct> sumList = new ArrayList<FlatConjunct>(50);
	public static ArrayList<FlatConjunct> deltaList = new ArrayList<FlatConjunct>(50);
	public static ArrayList<Boolean> statusList = new ArrayList<Boolean>(50);
	
	public FlatFormula()
	{
		m_conjuncts = new ArrayList<FlatConjunct>();
	}
	
	public void clear()
	{
		m_conjuncts.clear();
	}
	
	public FlatFormula(FlatConjunct conjunct)
	{
		m_conjuncts = new ArrayList<FlatConjunct>();
		m_conjuncts.add(conjunct);
	}
	
	public void initWith(FlatFormula other)
	{
		for (int i = 0; i<other.m_conjuncts.size(); ++i)
			m_conjuncts.add(other.m_conjuncts.get(i));
	}
		
	
	public void addProductCanonical(FlatFormula form)
	{
		int othersize = form.m_conjuncts.size();
		if (othersize == 0) clear();
		int thissize = m_conjuncts.size(); 
		if (thissize == 0) 
			return;
		
		prodList.clear();
		statusList.clear();
		for (int i = 0; i<thissize*othersize; ++i)
			statusList.add(true);
		
		for (int i = 0; i<thissize; ++i)
		{
			FlatConjunct crtConjunct = m_conjuncts.get(i);
			for (int j = 0; j<othersize; ++j)
			{
				FlatConjunct prod = FlatConjunct.getProduct(crtConjunct,form.m_conjuncts.get(j));
				
				boolean isValid = true;
				if (Switches.COST_ON)
					if (prod.cost()>=ChasedInstance.zeInstance.m_query.m_minCost)
						isValid = false;
				//if (thissize!=1 && othersize!=1)
				{
					if (isValid)
					{
						for (int k = 0; k<thissize; k++) if (k!=i)
							if (statusList.get(k*othersize+j)) //the product is not subsumed by some other
								if (prod.isSubsumedBy(m_conjuncts.get(k)))
								{
									isValid = false;
									break;
								}
					}
					if (isValid)
					{
						for (int k = 0; k<othersize; k++) if (k!=j)
							if (statusList.get(i*othersize+k)) //the product is not subsumed by some other
								if (prod.isSubsumedBy(form.m_conjuncts.get(k)))
								{
									isValid = false;
									break;
								}
					}
				}
					
				if (!isValid) 
					statusList.set(i*othersize+j, false);
				else	
					prodList.add(prod);
			}
		}
		
		//System.out.println("former term1:");
		//System.out.println(this);
		//System.out.println("former term2:");
		//System.out.println(form);
		
		ArrayList<FlatConjunct> aux = m_conjuncts; m_conjuncts = prodList; prodList = aux;
		/*
		if (!isCanonical())
		{
			System.out.println("product:");
			System.out.println(this);
			
			System.out.println("huge problem");
		}
		*/
	}
	
	
	public boolean addSumCanonical(FlatFormula toAdd, boolean retainOnlyDelta)
	{
		deltaList.clear();
		statusList.clear();
		
		int thissize = m_conjuncts.size();
		for (int j = 0; j<thissize; ++j)
			statusList.add(false);
		
		boolean origSubsumed = false;
		for (int i =0; i<toAdd.m_conjuncts.size(); ++i)
		{
			//check if this conjunct is subsumed by the original formula
			FlatConjunct crtConjunct = toAdd.m_conjuncts.get(i);
			boolean subsumed = false;
			for (int j = 0; j<thissize; ++j)
			{
				if (crtConjunct.isSubsumedBy(this.m_conjuncts.get(j)))
				{
					subsumed = true;
					break;
				}
			}
			
			if (!subsumed) //if not subsumed, check if it subsumes anything
			{
				deltaList.add(crtConjunct);
				for (int j = 0; j<thissize; ++j)
					if (this.m_conjuncts.get(j).isSubsumedBy(crtConjunct))
					{
						statusList.set(j, true);
						origSubsumed = true;
					}
			}
		}
	
		
		if (origSubsumed) //retain only non subsumed conjuncts
		{
			sumList.clear();
			for (int j = 0; j<thissize; ++j)
				if (!statusList.get(j)) //not subsumed
					sumList.add(this.m_conjuncts.get(j));
			ArrayList<FlatConjunct>  aux = this.m_conjuncts; this.m_conjuncts = sumList; sumList = aux;
		}
		
		for (int i =0; i<deltaList.size(); ++i)
			m_conjuncts.add(deltaList.get(i));
		

		boolean result = deltaList.size()==0;
		
		if (retainOnlyDelta){//change toAdd to reflect non-subsumed conjuncts
			ArrayList<FlatConjunct> aux = toAdd.m_conjuncts; toAdd.m_conjuncts = deltaList; deltaList = aux;
		}
		
	/*
		if (!isCanonical())
			System.out.println("huge problem");
		if (!toAdd.isCanonical())
			System.out.println("huge problem");
	*/
		return result;
	}
	
	
	public boolean isSubsumedBy(FlatFormula other)
	{
		int thissize = m_conjuncts.size();
		int othersize = other.m_conjuncts.size();
		for (int i = 0; i<thissize; ++i)
		{
			boolean subsumed  = false;
			FlatConjunct crtConjunct = m_conjuncts.get(i);
			for (int j = 0; j<othersize; ++j)
				if (crtConjunct.isSubsumedBy(other.m_conjuncts.get(j)))
				{
					subsumed = true;
					break;
				}
			if (!subsumed) return false;
		}
		return true;
	}
	
	

	public int shrinkToMinCost()
	{
		FlatConjunct minConj = m_conjuncts.get(0);
		for (int i = 1; i<m_conjuncts.size(); ++i)
			if (m_conjuncts.get(i).cost()<minConj.cost())
				minConj = m_conjuncts.get(i);
		m_conjuncts.clear();
		m_conjuncts.add(minConj);
		return minConj.cost();
	}
	
	public boolean isCanonical()
	{
		for (int i = 0; i < m_conjuncts.size(); i++)
			for (int j = i+1; j<m_conjuncts.size(); ++j)
			{
				if (m_conjuncts.get(i).isSubsumedBy(m_conjuncts.get(j))) return false;
				if (m_conjuncts.get(j).isSubsumedBy(m_conjuncts.get(i))) return false;
			}
		return true;
	}
	
	public String toString()
	{
		String provStr = "";
		for (int i = 0; i<m_conjuncts.size(); ++i)
			provStr+=m_conjuncts.get(i)+"+";
		return provStr;
	}
	
	public void displayRows()
	{
		for (int i = 0; i<m_conjuncts.size(); ++i)
			System.out.println(m_conjuncts.get(i));
		System.out.println();
	}
}
