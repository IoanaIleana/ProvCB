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

package atoms;

import flatProvenance.FlatFormula;
import instance.ChasedInstance;
import instance.Relation;

import java.util.HashSet;

import placeHolderProvenance.PHFormula;
import provenanceEvents.ProvChangedOnAtom;

//a relational atom in the instance
public class ResolvedRelAtom 
{
	public Relation m_relation;
	public ResolvedTerm[] m_terms;	
	
	public HashSet<ResolvedRelAtom> m_atomWatchers;
	public HashSet<ResolvedEquality> m_equalityWatchers;
	
	public FlatFormula m_flatProvenance;
	public PHFormula m_placeHolderProvenance;
	
	public String m_key;
	
	public ResolvedRelAtom(Relation relation, int size)
	{
		m_relation = relation;
		m_terms = new ResolvedTerm[size];
		
		m_atomWatchers = new HashSet<ResolvedRelAtom>();
		m_equalityWatchers = new HashSet<ResolvedEquality>();
		
		m_flatProvenance=null;
		m_placeHolderProvenance = null;
	}
	
	public void setProvenance(FlatFormula flatProvenance, PHFormula phProvenance)
	{
		if (null == m_flatProvenance)
			m_flatProvenance = new FlatFormula();
		else
			m_flatProvenance.clear();
		m_flatProvenance.initWith(flatProvenance);
		
		m_placeHolderProvenance = phProvenance;
	}
	
	public void computeKey()
	{
		m_key = "";
		for (int i = 0; i<m_terms.length; ++i)
			m_key+=m_terms[i].m_name+" ";
		//do this by string buffer
	}
	
	
	public void addWatcher(ResolvedRelAtom atom)
	{
		m_atomWatchers.add(atom);
	}
	
	public void addWatcher(ResolvedEquality eq)
	{
		m_equalityWatchers.add(eq);
	}
	
	public boolean isSameAs(ResolvedRelAtom atom1)
	{
		for (int i = 0; i<m_terms.length; ++i)
			if (m_terms[i]!=atom1.m_terms[i]) return false;
		return true;
	}
	
	
	public void registerAsWatcher(PHFormula form)
	{
		form.addAtomWatcher(this);
	}
	
	public void provenanceChanged()
	{
		FlatFormula newFormula = m_placeHolderProvenance.getFlattened();
		if (!newFormula.isSubsumedBy(m_flatProvenance))
		{
			m_flatProvenance = newFormula;
			ChasedInstance.zeInstance.addEvent(new ProvChangedOnAtom(this));
		}
	}
	
	public String toString()
	{
		String str = m_relation.m_name+"(";
		for (int i = 0; i< m_terms.length; ++i)
		{
			str+=m_terms[i];
			if (i<m_terms.length-1) str+=",";
			else str+=")";
		}
		str+=" prov: "+m_flatProvenance;
		return str;
	}
}
