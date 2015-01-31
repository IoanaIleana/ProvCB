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

import instance.ConnectedComponent;

//a variable of the chased instance or a constant (not an atom position)
public class ResolvedTerm 
{
	//name of the variable/constant
	public String m_name;
	
	//index of the variable (id)
	public int m_index;
	
	//connected component this variable belongs to
	public ConnectedComponent m_component;
	
	//index of this variable in the connected components (to access closure equalities)
	public int m_indexInComponent;
	
	
	public ResolvedTerm(String name, int index)
	{
		m_name = name;
		m_index = index;
		
		m_component = new ConnectedComponent(this);
		m_indexInComponent = 0;
	}
	
	public String toString()
	{
		return m_name;
	}
}
