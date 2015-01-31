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

package nodes;
import flatProvenance.FlatFormula;
import instance.LocalMapping;

import java.util.ArrayList;
import java.util.HashSet;

import placeHolderProvenance.PHFormula;
import atoms.AtomPositionTerm;
import atoms.DefEquality;
import atoms.DefRelAtom;
import atoms.ResolvedTerm;

public abstract class Node 
{
	public Node m_parent;	
	public ArrayList<DefEquality> m_defEqualities;
	
	public boolean m_hasNew;
	
	//projected attributes 
	public boolean[] m_comesFromLeft;
	public int[] m_indices;

    //indices in children or atom participating in equalities
	public int[] m_eqsLeft;
	public int[] m_eqsRight;
	
	public LocalMapping m_dummyMapping;
	
	public int m_id;
	public static int countNodes = 0;
	
	public int m_depth;
	
	public Node()
	{
		m_parent = null;
		m_defEqualities = new ArrayList<DefEquality>();
		m_hasNew = false;
		
		m_id = countNodes;
		countNodes++;
	}
	
	public int hashCode()
	{
		return m_id;
	}
	
	public void addDefEquality(DefEquality atom)
	{
		m_defEqualities.add(atom);
	}
	
	public boolean checkMatchingEqualities(ResolvedTerm[] resTermsLeft, ResolvedTerm[] resTermsRight)
	{
		for (int j = 0; j<resTermsLeft.length; ++j)
			if (resTermsLeft[j].m_component != resTermsRight[j].m_component)
				return false;
		return true;
	}
	
	
	public abstract boolean hasAtom(DefRelAtom atom);
	
	public abstract void computeNeededTerms(ArrayList<AtomPositionTerm> termsNeededUpper);
	
	public abstract void addNewLocalMapping(LocalMapping mapping, Object adder);
	
	public abstract void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula provform, Object adder);
	
	public abstract void registerAsWatcherOnRelations();
	
	public abstract void flush(); 
	
	public abstract void refreshDummyMapping();
	
	public abstract void pushNew();
	
	public void notifyHasNew()
	{
		m_hasNew = true;
		if (null!= m_parent) m_parent.notifyHasNew();
	}


	public void displaySubtree(int spacing)
	{
		for (int i = 0; i<spacing; ++i) System.out.print(" ");
		
		
		if (this instanceof LeafNode)
			System.out.println(((LeafNode)this).m_atom.m_relation.m_name+" ");
		else
		{
			for (DefRelAtom atom:((InnerNode)this).m_atoms)
				System.out.print(atom.m_relation.m_name+" ");
			System.out.print("   "+m_defEqualities.size()+"   ");
			for (int i = 0; i<m_defEqualities.size();++i)
				System.out.print(m_defEqualities.get(i)+" ");
			System.out.println();
			
			((InnerNode)this).m_leftChild.displaySubtree(spacing+4);
			((InnerNode)this).m_rightChild.displaySubtree(spacing+4);
		}
	}
	
	public void addAtomsToParent(HashSet<DefRelAtom> atoms)
	{
		if (this instanceof LeafNode)
			atoms.add(((LeafNode)this).m_atom);
		else
			atoms.addAll(((InnerNode)this).m_atoms);
	}
	
	public String nodeAtoms()
	{
		if (this instanceof LeafNode)
			return ((LeafNode)this).m_atom.m_relation.m_name;
		else
		{
			String output = "";
			for (DefRelAtom atom:((InnerNode)this).m_atoms)
				output+=atom.m_relation.m_name+" ";
			return output;
		}
	}
}
