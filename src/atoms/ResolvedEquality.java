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

import instance.ChasedInstance;
import placeHolderProvenance.PHFormula;
import provenanceEvents.ProvChangedOnEquality;
import flatProvenance.FlatFormula;

//an equality in the instance
public class ResolvedEquality 
{
	public ResolvedTerm m_term1;
	public ResolvedTerm m_term2;
	
	public FlatFormula m_flatProvenance;
	public PHFormula m_placeHolderProvenance;
	
	public ResolvedEquality()
	{
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
	
	public boolean isSameAs(ResolvedEquality eq)
	{
		if (m_term1 == eq.m_term1 && m_term2 == eq.m_term2) return true;
		else if (m_term1 ==eq.m_term2 && m_term2 == eq.m_term1) return true;
		else return false;
	}
	
	public void registerAsWatcher(PHFormula form)
	{
		form.addEqualityWatcher(this);
	}
	
	public void provenanceChanged()
	{
		FlatFormula newFormula = m_placeHolderProvenance.getFlattened();
		if (!newFormula.isSubsumedBy(m_flatProvenance))
		{
			m_flatProvenance = newFormula;
			ChasedInstance.zeInstance.addEvent(new ProvChangedOnEquality(this));
		}
	}
	
	public String toString()
	{
		return m_term1+"="+m_term2+" prov: "+m_flatProvenance;
	}
	
}
