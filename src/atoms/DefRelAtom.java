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

import instance.Relation;

//a relational atom in the definition of a constraint
public class DefRelAtom 
{
	public Relation m_relation;
	public AtomPositionTerm[] m_terms;
	
	public int m_id;
	public static int countDefRelAtoms = 0;
	
	public int hashCode()
	{
		return m_id;
	}
	
	public DefRelAtom(Relation relation, int size)
	{
		m_relation = relation;
		m_terms = new AtomPositionTerm[size];
		m_id = countDefRelAtoms;
		countDefRelAtoms++;
	}
	
}
