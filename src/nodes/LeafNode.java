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

import java.util.ArrayList;

import atoms.AtomPositionTerm;
import atoms.DefRelAtom;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;

import instance.ChasedInstance;
import instance.LocalMapping;

import flatProvenance.FlatFormula;

import placeHolderProvenance.PHConjunct;
import placeHolderProvenance.PHFormula;



public class LeafNode extends Node
{	
	public DefRelAtom m_atom;
	
	public ResolvedTerm[] m_termsForEqsLeft;
	public ResolvedTerm[] m_termsForEqsRight;
	
	public AdaptiveMappingsIndex m_index;
	public PHConjunct dummyConjunct;
	
	public int m_cursorInRelation;

	
	public LeafNode(DefRelAtom atom)
	{
		super();
		m_atom = atom;
		m_index = new AdaptiveMappingsIndex(10);
		
		m_cursorInRelation = 0;
		
		m_depth = 1;
	}
	
	public boolean hasAtom(DefRelAtom atom)
	{
		return atom==m_atom;
	}
	
	public void refreshDummyMapping()
	{
		m_dummyMapping = new LocalMapping();
		m_dummyMapping.m_termsForAncestors = new ResolvedTerm[m_indices.length];
		m_dummyMapping.m_flatProvenance = new FlatFormula();
	}
	
	public void computeNeededTerms(ArrayList<AtomPositionTerm> termsNeededUpper)
	{
		m_comesFromLeft = new boolean[termsNeededUpper.size()];
		m_indices = new int[termsNeededUpper.size()];
		
		//projected attributes
		for (int i = 0; i<termsNeededUpper.size(); ++i)
			m_indices[i] = termsNeededUpper.get(i).m_attributeIndex;
		
		//join attributes
		//to do: store constants differently!
		m_eqsLeft = new int[m_defEqualities.size()];
		m_eqsRight = new int[m_defEqualities.size()];
		
		
		for (int i = 0; i<m_defEqualities.size();++i)
		{
			AtomPositionTerm term1 = m_defEqualities.get(i).m_term1;
			AtomPositionTerm term2 = m_defEqualities.get(i).m_term2;

			m_eqsLeft[i] = term1.m_attributeIndex;
			m_eqsRight[i] = term2.m_attributeIndex;
		}	
		
		m_termsForEqsLeft = new ResolvedTerm[m_defEqualities.size()];
		m_termsForEqsRight = new ResolvedTerm[m_defEqualities.size()];

		refreshDummyMapping();
	}
	
	
	public void flush()
	{	
		m_index.flush();
		m_cursorInRelation = 0;
	}
	
	private void addNewAtom(ResolvedRelAtom atom)
	{
		//check if inner atom equalities are respected
		for (int i = 0; i<m_termsForEqsLeft.length; ++i)
		{
			m_termsForEqsLeft[i] = atom.m_terms[m_eqsLeft[i]];
			m_termsForEqsRight[i] = atom.m_terms[m_eqsRight[i]];
		}
		if (!checkMatchingEqualities(m_termsForEqsLeft, m_termsForEqsRight)) 
			return;
		
		//fill dummy mapping
		for (int i = 0; i<m_indices.length; ++i)
			m_dummyMapping.m_termsForAncestors[i] = atom.m_terms[m_indices[i]];
		m_dummyMapping.computeHashCodeId();
		
		if (ChasedInstance.zeInstance.m_usePlaceHolders)
		{
			//to do: replace this by "clear"
			dummyConjunct = new PHConjunct();
			dummyConjunct.addAtom(atom);
			
			m_dummyMapping.m_placeHolderProvenance =new PHFormula(dummyConjunct);
			
			for (int i=0; i<m_termsForEqsLeft.length; ++i)
				m_dummyMapping.m_placeHolderProvenance.addEquality(m_termsForEqsLeft[i], m_termsForEqsRight[i]);
		}
		
		m_dummyMapping.m_flatProvenance.clear();
		m_dummyMapping.m_flatProvenance.initWith(atom.m_flatProvenance);
		//to do: check if eqs on same term; don't forget constants
		for (int i=0; i<m_termsForEqsLeft.length; ++i)
			m_dummyMapping.m_flatProvenance.addProductCanonical(m_termsForEqsLeft[i].m_component.getClosureProvenance(m_termsForEqsLeft[i], m_termsForEqsRight[i]));
		
		addNewLocalMapping(m_dummyMapping, this);
	}
	
	public void addNewLocalMapping(LocalMapping mapping, Object adder)
	{		
		//find if an identical-project atom already exists
		LocalMapping exists = m_index.getAndInsert(mapping, mapping.m_hashCodeId);
		if (exists!=null)
		{
			//do this better with subsumption
			boolean subsumed = exists.m_flatProvenance.addSumCanonical(mapping.m_flatProvenance, true);
			
			if (ChasedInstance.zeInstance.m_usePlaceHolders)
				exists.m_placeHolderProvenance.addSum(mapping.m_placeHolderProvenance);
			else if (subsumed)
				return;
			
			
			m_parent.addExistingMappingAdditionalProvenance(exists, mapping.m_placeHolderProvenance, mapping.m_flatProvenance, this);
		}
		else
		{
			m_parent.addNewLocalMapping(mapping, this);	
			refreshDummyMapping();	
		}
	}
	
	public void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula provform,Object adder)
	{	
	}
	
	public void registerAsWatcherOnRelations()
	{
		m_atom.m_relation.addWatcher(this);
	}
	
	public void pushNew()
	{
		ResolvedRelAtom newAtom = m_atom.m_relation.m_atoms.get(m_cursorInRelation);
		addNewAtom(newAtom);
		m_cursorInRelation++;
		if (m_cursorInRelation ==m_atom.m_relation.m_atoms.size())
			m_hasNew=false;
	}
}
