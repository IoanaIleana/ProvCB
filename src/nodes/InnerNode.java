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
import instance.ChasedInstance;
import instance.LocalMapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import atoms.AtomPositionTerm;
import atoms.ResolvedTerm;
import atoms.DefRelAtom;

import placeHolderProvenance.PHFormula;


public class InnerNode extends Node
{
	public Node m_leftChild;
	public Node m_rightChild;
	
	public HashSet<DefRelAtom> m_atoms;
	
	public AdaptiveMappingsIndex m_localMappingsLeft;
	public AdaptiveMappingsIndex m_localMappingsRight;
	
	public AdaptiveMappingsIndex m_joinMappingsId;
	
	public InnerNode( Node left, Node right)
	{
		super();
		
		m_leftChild = left; left.m_parent = this;
		m_rightChild = right; right.m_parent = this;

		m_localMappingsLeft = new AdaptiveMappingsIndex(4);
		m_localMappingsRight = new AdaptiveMappingsIndex(4);	
		
		m_atoms = new HashSet<DefRelAtom>();
		m_leftChild.addAtomsToParent(m_atoms);
		m_rightChild.addAtomsToParent(m_atoms);
		
		m_depth = m_leftChild.m_depth;
		if (m_rightChild.m_depth > m_depth)
			m_depth = m_rightChild.m_depth;
		m_depth++;
	}
	
	
	public boolean hasAtom(DefRelAtom atom)
	{
		return m_atoms.contains(atom);
	}
	
	public void refreshDummyMapping()
	{
		m_dummyMapping = new LocalMapping();
		m_dummyMapping.m_termsForAncestors = new ResolvedTerm[m_comesFromLeft.length];
		m_dummyMapping.m_flatProvenance = new FlatFormula();
	}
	

	//compute projected attributes for this node, as indices in children's projected attributes
	public void computeNeededTerms(ArrayList<AtomPositionTerm> termsNeededUpper)
	{	
		m_comesFromLeft = new boolean[termsNeededUpper.size()];
		m_indices = new int[termsNeededUpper.size()];
		m_eqsLeft = new int[m_defEqualities.size()];
		m_eqsRight = new int[m_defEqualities.size()];
		
		ArrayList<AtomPositionTerm> termsneededLeft = new ArrayList<AtomPositionTerm>();
		ArrayList<AtomPositionTerm> termsneededRight = new ArrayList<AtomPositionTerm>();
		
		//projected attributes
		for (int i = 0; i<termsNeededUpper.size(); ++i)
		{
			AtomPositionTerm term = termsNeededUpper.get(i);
		
			if (m_leftChild.hasAtom(term.m_atom))
			{
				m_comesFromLeft[i] = true;
				m_indices[i] = termsneededLeft.size();
				termsneededLeft.add(term);
			}	
			else
			{
				m_comesFromLeft[i]= false;
				m_indices[i]=termsneededRight.size();
				termsneededRight.add(term);
			}
		}
		
		//equalities at this node's level
		for (int i = 0; i<m_defEqualities.size(); ++i)
		{
			AtomPositionTerm termLeft = m_defEqualities.get(i).m_term1;
			AtomPositionTerm termRight = m_defEqualities.get(i).m_term2;
			
			if (!m_leftChild.hasAtom(termLeft.m_atom)){ //maybe the equality is inversed
				AtomPositionTerm aux = termLeft; termLeft = termRight; termRight = aux;
			}
			
			int j = 0;
			for (j = 0; j<termsneededLeft.size(); ++j)
				if (termsneededLeft.get(j).isSameAs(termLeft)) break;
			m_eqsLeft[i] = j;
			if (j==termsneededLeft.size())
				termsneededLeft.add(termLeft);
			
			for (j = 0; j<termsneededRight.size(); ++j)
				if (termsneededRight.get(j).isSameAs(termRight)) break;
			m_eqsRight[i] = j;
			if (j==termsneededRight.size())
				termsneededRight.add(termRight);
		}
		
	
		m_leftChild.computeNeededTerms(termsneededLeft);
		m_rightChild.computeNeededTerms(termsneededRight);
		
		refreshDummyMapping();
		m_joinMappingsId = new AdaptiveMappingsIndex(10); //number of bits-function of projected attrs?
	}
	

	public void flush()
	{
		m_leftChild.flush();
		m_rightChild.flush();
		
		m_localMappingsLeft.flush();
		m_localMappingsRight.flush();
		
		m_joinMappingsId.flush();
	}
	
	
	private void makeJoinMapping(LocalMapping leftMapping, LocalMapping rightMapping,
								 PHFormula phLeft, PHFormula phRight,
								 FlatFormula flatLeft, FlatFormula flatRight)
	{
		if (!checkMatchingEqualities(leftMapping.m_termsForEqsInParent, rightMapping.m_termsForEqsInParent)) return;
		
		//fill dummy mapping
		m_dummyMapping.fillFrom (leftMapping, rightMapping, phLeft, phRight, flatLeft, flatRight, m_comesFromLeft, m_indices);
		
		//check if exists
		LocalMapping exists = m_joinMappingsId.getAndInsert(m_dummyMapping, m_dummyMapping.m_hashCodeId);
		if (null != exists)
		{
			boolean subsumed = exists.m_flatProvenance.addSumCanonical(m_dummyMapping.m_flatProvenance, true);
			if (ChasedInstance.zeInstance.m_usePlaceHolders)
				exists.m_placeHolderProvenance.addSum(m_dummyMapping.m_placeHolderProvenance);
			else if (subsumed)
				return;
			
			m_parent.addExistingMappingAdditionalProvenance(exists, m_dummyMapping.m_placeHolderProvenance, m_dummyMapping.m_flatProvenance, this);
		}
		else
		{
			m_parent.addNewLocalMapping(m_dummyMapping, this);
			refreshDummyMapping();
		}
	}
	
	public void addNewMappingLeft(LocalMapping mapping)
	{
		mapping.makeResolvedTermsForEqs(m_eqsLeft);
		mapping.computeHashCodeEq();
		m_localMappingsLeft.insert(mapping, mapping.m_hashCodeEq);
		
		LinkedList<LocalMapping> matching = m_localMappingsRight.getMatching(mapping.m_hashCodeEq);
		if (null == matching) return;
			
		for (LocalMapping right:matching) 
			makeJoinMapping(mapping, right, mapping.m_placeHolderProvenance, right.m_placeHolderProvenance, mapping.m_flatProvenance, right.m_flatProvenance);
	}
	
	public void addNewMappingRight(LocalMapping mapping)
	{
		mapping.makeResolvedTermsForEqs(m_eqsRight);
		mapping.computeHashCodeEq();
		m_localMappingsRight.insert(mapping, mapping.m_hashCodeEq);
		
		LinkedList<LocalMapping> matching = m_localMappingsLeft.getMatching(mapping.m_hashCodeEq);
		if (null == matching) return;
		
		for (LocalMapping left:matching) 
			makeJoinMapping(left, mapping, left.m_placeHolderProvenance, mapping.m_placeHolderProvenance, left.m_flatProvenance, mapping.m_flatProvenance);
	}
	
	public void addNewLocalMapping(LocalMapping mapping, Object adder)
	{
		if (adder == m_leftChild)
			addNewMappingLeft(mapping);
		else
			addNewMappingRight(mapping);
	}
			
	public void addExistingMappingLeft(LocalMapping mapping, PHFormula phform, FlatFormula provform)
	{
		LinkedList<LocalMapping> matching = m_localMappingsRight.getMatching(mapping.m_hashCodeEq);
		if (null == matching) return;
			
		for (LocalMapping right:matching) 
			makeJoinMapping(mapping, right, phform, right.m_placeHolderProvenance, provform, right.m_flatProvenance);
	}
	
	public void addExistingMappingRight(LocalMapping mapping, PHFormula phform, FlatFormula provform)
	{
		LinkedList<LocalMapping> matching = m_localMappingsLeft.getMatching(mapping.m_hashCodeEq);
		if (null == matching) return;
		
		for (LocalMapping left:matching) 
			makeJoinMapping(left, mapping, left.m_placeHolderProvenance, phform, left.m_flatProvenance, provform);
	}

	
	public void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula provform, Object adder)
	{
		if (adder == m_leftChild) 
			addExistingMappingLeft(mapping, phform, provform);
		else
			addExistingMappingRight(mapping, phform, provform);
	}
	
	public void registerAsWatcherOnRelations()
	{
		m_leftChild.registerAsWatcherOnRelations();
		m_rightChild.registerAsWatcherOnRelations();
	}
	
	public void pushNew()
	{
		/*
		if (m_leftChild.m_hasNew)
			m_leftChild.pushNew();
		if (m_rightChild.m_hasNew)
			m_rightChild.pushNew();
		
		m_hasNew = m_leftChild.m_hasNew || m_rightChild.m_hasNew;
	   */
		if (m_leftChild.m_hasNew)
			m_leftChild.pushNew();
		else if (m_rightChild.m_hasNew)
			m_rightChild.pushNew();
		else m_hasNew = false;
		
	}

}
