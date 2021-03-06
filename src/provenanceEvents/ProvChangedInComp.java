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

package provenanceEvents;

import atoms.ResolvedEquality;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;

public class ProvChangedInComp extends ProvenanceEvent
{
	public ResolvedTerm m_term;
	
	public ProvChangedInComp(ResolvedTerm term)
	{
		m_term = term;
	}
	
	public void propagate()
	{
		for (ResolvedRelAtom atom: m_term.m_component.m_atomWatchers)
			atom.provenanceChanged();
		for (ResolvedEquality eq: m_term.m_component.m_equalityWatchers)
			eq.provenanceChanged();
	}
}
