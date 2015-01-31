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

package instance;

import atoms.ResolvedTerm;
import placeHolderProvenance.PHFormula;
import flatProvenance.FlatFormula;

public class LocalMapping 
{
	public LocalMapping m_leftMapping;
	public LocalMapping m_rightMapping;
	
	public ResolvedTerm[] m_termsForEqsInParent;
	public ResolvedTerm[] m_termsForAncestors;
	
	public int m_hashCodeEq;
	public int m_hashCodeId;
	
	public FlatFormula m_flatProvenance;
	public PHFormula m_placeHolderProvenance;
	
	public LocalMapping()
	{
		m_leftMapping=null;
		m_rightMapping=null;
	}
	
	public int hashCode()
	{
		return m_hashCodeId;
	}

	public void fillFrom(LocalMapping leftMapping, LocalMapping rightMapping, 
						PHFormula leftph, PHFormula rightph,
						FlatFormula leftflat, FlatFormula rightflat,
						boolean[] comesFromLeft, int[] indices)
	{
		m_leftMapping = leftMapping;
		m_rightMapping = rightMapping;
		
		for (int i = 0; i<comesFromLeft.length;++i)
		{
			if (comesFromLeft[i])
				m_termsForAncestors[i] = leftMapping.m_termsForAncestors[indices[i]];
			else
				m_termsForAncestors[i] = rightMapping.m_termsForAncestors[indices[i]];
		}
		computeHashCodeId();
		
		
		if (ChasedInstance.zeInstance.m_usePlaceHolders)
		{
			m_placeHolderProvenance = new PHFormula(leftph, rightph);
			for (int i = 0; i<leftMapping.m_termsForEqsInParent.length;++i)
			{
				if (leftMapping.m_termsForEqsInParent[i] == rightMapping.m_termsForEqsInParent[i]) continue;
				m_placeHolderProvenance.addEquality(leftMapping.m_termsForEqsInParent[i], rightMapping.m_termsForEqsInParent[i]);
			}	
		}
		
		//System.out.println("interms");
		m_flatProvenance.clear();
		m_flatProvenance.initWith(leftflat);
		//m_flatProvenance.display();
		m_flatProvenance.addProductCanonical(rightflat);
		//m_flatProvenance.display();
	
		for (int i = 0; i<leftMapping.m_termsForEqsInParent.length;++i)
		{
			if (leftMapping.m_termsForEqsInParent[i] == rightMapping.m_termsForEqsInParent[i]) continue;
			m_flatProvenance.addProductCanonical(leftMapping.m_termsForEqsInParent[i].m_component.getClosureProvenance(leftMapping.m_termsForEqsInParent[i], rightMapping.m_termsForEqsInParent[i]));
		}
		//m_flatProvenance.display();
		//m_flatProvenance.canonicize();
	}
	
	public boolean isSameAs(LocalMapping mapping)
	{
		for (int i = 0; i<m_termsForAncestors.length; ++i)
			if (m_termsForAncestors[i]!=mapping.m_termsForAncestors[i]) return false;
		return true;
	}
	
	public boolean equals(Object object)
	{
		LocalMapping mapping=(LocalMapping)object;
		return isSameAs(mapping);
	}
	
	public void computeHashCodeEq()
	{
		m_hashCodeEq = 0;
		
		for (int i = 0; i<m_termsForEqsInParent.length; ++i)
			m_hashCodeEq+=m_hashCodeEq * 0x9e370001 + m_termsForEqsInParent[i].m_component.m_index;
	}
		
	
	public void computeHashCodeId()
	{
		m_hashCodeId = 0;
		for (int i = 0; i<m_termsForAncestors.length; ++i)
			m_hashCodeId += m_hashCodeId * 0x9e370001 + m_termsForAncestors[i].m_index;
	}

	public void makeResolvedTermsForEqs (int[] equalities)
	{
		m_termsForEqsInParent = new ResolvedTerm [equalities.length];
		for (int i = 0; i<equalities.length; ++i)
			m_termsForEqsInParent[i] = m_termsForAncestors[equalities[i]];
	}
	
	
	public String toString()
	{
		String str = "terms useful for upper levels\n";
		for (int i = 0; i< m_termsForAncestors.length; ++i)
			str+=m_termsForAncestors[i];	
		return str;
	}

}
