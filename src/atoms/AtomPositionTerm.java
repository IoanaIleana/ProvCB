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

//a position in an atom (non-resolved atom attribute)
//these are used for the initial definitions of the constraints:
//for equalities in the premise, and for conclusion definitions
//the class is also used to represent constants and Skolems
public class AtomPositionTerm
{
	//the atom in the premise, null if this is a Skolem or constant
	public DefRelAtom m_atom;
	
	//the position in the atom, -1 if this is a Skolem of constant
	public int m_attributeIndex;
	
	//the constant, null if this is not a constant
	public ResolvedTerm m_constant;
	
	//the Skolem index at the constraint level, -1 if this is not a Skolem
	public int m_skolemIndex;
	
	public AtomPositionTerm(DefRelAtom atom, int position)
	{
		m_atom = atom;
		m_attributeIndex = position;
		m_skolemIndex = -1;
		m_constant = null;
	}
	
	public String toString()
	{
		return m_atom.m_relation.m_name+"."+m_attributeIndex;
	}
	
	public boolean isSameAs(AtomPositionTerm other)
	{
		return (m_atom==other.m_atom && m_attributeIndex==other.m_attributeIndex);
	}
}
