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

package placeHolderProvenance;
import java.util.ArrayList;


import atoms.ResolvedEquality;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;

import flatProvenance.FlatFormula;

public class PHFormula {
	public ArrayList<PHConjunct> m_conjuncts;
	
	public PHFormula()
	{
		m_conjuncts = new ArrayList<PHConjunct>();
	}
	
	public PHFormula(PHConjunct conjunct)
	{
		m_conjuncts = new ArrayList<PHConjunct>();
		m_conjuncts.add(conjunct);
	}
	
	public PHFormula(PHFormula form1, PHFormula form2)
	{
		m_conjuncts = new ArrayList<PHConjunct>();
		if (form1.m_conjuncts.size() == 0)
		{
			for (int i = 0; i<form2.m_conjuncts.size(); ++i)
				m_conjuncts.add(form2.m_conjuncts.get(i));
			return;
		}
		if (form2.m_conjuncts.size() == 0)
		{
			for (int i = 0; i<form1.m_conjuncts.size(); ++i)
				m_conjuncts.add(form1.m_conjuncts.get(i));
			return;
		}
		
		for (int i = 0; i<form1.m_conjuncts.size(); ++i)
			for (int j = 0; j<form2.m_conjuncts.size(); ++j)
				m_conjuncts.add(PHConjunct.getProduct(form1.m_conjuncts.get(i),form2.m_conjuncts.get(j)));
	}
	
	public PHFormula(PHFormula other)
	{
		m_conjuncts = new ArrayList<PHConjunct>();
		for (int i = 0; i<other.m_conjuncts.size(); ++i)
			m_conjuncts.add(other.m_conjuncts.get(i));
	}
	
	public void addEquality(ResolvedTerm term1, ResolvedTerm term2)
	{
		if (m_conjuncts.size() == 0)
			m_conjuncts.add(new PHConjunct());
		for (int i = 0; i<m_conjuncts.size(); ++i)
			m_conjuncts.get(i).addEquality(term1,term2);
	}
	
	
	public void addSum(PHFormula formula)
	{
		for (int i =0; i<formula.m_conjuncts.size(); ++i)
			m_conjuncts.add(formula.m_conjuncts.get(i));
	}
	
	public FlatFormula getFlattened()
	{
		FlatFormula formula = new FlatFormula();
		for (int i = 0; i<m_conjuncts.size(); ++i)
			formula.addSumCanonical(m_conjuncts.get(i).getFlattened(), false);
		return formula;
	}
	
	public void addAtomWatcher(ResolvedRelAtom atom)
	{
		for (int i = 0; i<m_conjuncts.size(); ++i)
			m_conjuncts.get(i).addAtomWatcher(atom);
	}
	
	public void addEqualityWatcher(ResolvedEquality eq)
	{
		for (int i = 0; i<m_conjuncts.size(); ++i)
			m_conjuncts.get(i).addEqualityWatcher(eq);
	}
	
}
