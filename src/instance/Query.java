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

import placeHolderProvenance.PHFormula;
import flatProvenance.FlatFormula;

public class Query extends Tgd
{
	public FlatFormula m_provenance;
	public int m_minCost;
	public Query()
	{
		super();
		m_provenance = null;
		m_minCost = 100000;
	}
	public void addNewLocalMapping(LocalMapping mapping, Object adder)
	{
		//remember that if you interlace with chase you need to rather do a sum!
		m_provenance = mapping.m_flatProvenance;
		
		if (Switches.COST_ON)
			m_minCost = m_provenance.shrinkToMinCost();
	}
	
	public void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula flatform, Object adder)
	{
		if (Switches.COST_ON)
			m_minCost = m_provenance.shrinkToMinCost();	
	}
}
