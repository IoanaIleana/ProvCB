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

import java.util.HashMap;
import java.util.HashSet;

import flatProvenance.FlatFormula;


import atoms.ResolvedEquality;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;

public class PHConjunct {
	public HashSet<ResolvedRelAtom> m_atoms;
	public HashMap<ResolvedTerm, HashSet<ResolvedTerm>> m_equalities;
	
	public PHConjunct()
	{
		m_atoms = new HashSet<ResolvedRelAtom>();
		m_equalities = new HashMap<ResolvedTerm, HashSet<ResolvedTerm>>();
	}
	
	public void addAtom(ResolvedRelAtom atom)
	{
		m_atoms.add(atom);	
	}
	public void addEquality(ResolvedTerm term1, ResolvedTerm term2)
	{
		if (term1 == term2) return;
		if (term1.m_index >term2.m_index) addEquality(term2, term1);
		else
		{
			HashSet<ResolvedTerm> crt = m_equalities.get(term1);
			if (crt == null)
			{
				crt = new HashSet<ResolvedTerm>();
				m_equalities.put(term1, crt);
			}
			crt.add(term2);
		}
	}
	
	public static PHConjunct getProduct(PHConjunct conj1, PHConjunct conj2)
	{
		PHConjunct prod = new PHConjunct();
		for (ResolvedRelAtom atom: conj1.m_atoms)
			prod.m_atoms.add(atom);
		for (ResolvedRelAtom atom: conj2.m_atoms)
			prod.m_atoms.add(atom);
		
		for (ResolvedTerm t1:conj1.m_equalities.keySet())
			for (ResolvedTerm t2:conj1.m_equalities.get(t1))
				prod.addEquality(t1,t2);
		
		for (ResolvedTerm t1:conj2.m_equalities.keySet())
			for (ResolvedTerm t2:conj2.m_equalities.get(t1))
				prod.addEquality(t1,t2);
		
		return prod;
		
	}
	
	public FlatFormula getFlattened()
	{
		FlatFormula formula = new FlatFormula();
		for (ResolvedRelAtom atom: m_atoms)
			formula .addProductCanonical(atom.m_flatProvenance);
		for (ResolvedTerm t1:m_equalities.keySet())
			for (ResolvedTerm t2:m_equalities.get(t1))
				formula.addProductCanonical(t1.m_component.getClosureProvenance(t1,t2));
		return formula;
	}
	
	
	public void addAtomWatcher(ResolvedRelAtom atom)
	{
		for (ResolvedRelAtom exists: m_atoms)
			exists.addWatcher(atom);
		for (ResolvedTerm t1:m_equalities.keySet())
			t1.m_component.addWatcher(atom);
	}
	
	public void addEqualityWatcher(ResolvedEquality eq)
	{
		for (ResolvedRelAtom exists: m_atoms)
			exists.addWatcher(eq);
		for (ResolvedTerm t1:m_equalities.keySet())
			t1.m_component.addWatcher(eq);
	}
	
}
