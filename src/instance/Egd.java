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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import nodes.AdaptiveMappingsIndex;
import nodes.Node;
import placeHolderProvenance.PHFormula;
import provenanceEvents.ProvChangedOnEquality;
import atoms.AtomPositionTerm;
import atoms.DefEquality;
import atoms.DefRelAtom;
import atoms.ResolvedEquality;
import flatProvenance.FlatFormula;

public class Egd extends Node
{
	private PremiseDefinition m_premDef;
	private ArrayList<DefEquality> m_conclusions;
	
	//incremental update
	public HashMap<LocalMapping, ArrayList<ResolvedEquality>> m_matchedPremiseMappings;
	public LinkedList<LocalMapping> m_newPremiseMappings;
	public AdaptiveMappingsIndex m_joinMappingsId;
	
	public Egd()
	{
		super();
		m_premDef = new PremiseDefinition();
		m_conclusions = new ArrayList<DefEquality>();
		
		m_matchedPremiseMappings = new HashMap<LocalMapping, ArrayList<ResolvedEquality>>();
		m_newPremiseMappings = new LinkedList<LocalMapping>();
		m_joinMappingsId = new AdaptiveMappingsIndex(10); //number of bits-function of projected attrs?
	}
	
	public void readFromFile(BufferedReader br) throws Exception
	{
		m_premDef.readFromFile(br);
		m_premDef.m_topJoinNode.m_parent = this;
		
		Parser parser = new Parser();
		String line = br.readLine();
		parser.parseEqualities(line);
	
		ArrayList<String[]> equalities = parser.getEqualities();
		for (int i = 0; i<equalities.size(); ++i)
		{
			String[] crtParsedEquality = parser.getEqualities().get(i);
			DefEquality crtEquality = new DefEquality();
			crtEquality.m_term1 = m_premDef.getTermFromString(crtParsedEquality[0]);
			crtEquality.m_term2 =  m_premDef.getTermFromString(crtParsedEquality[1]);
			m_conclusions.add(crtEquality);
		}
	}
	
	private int getIndexFromPremise(AtomPositionTerm term, ArrayList<AtomPositionTerm> termsNeededUpper)
	{
		int index = 0;
		for (index = 0; index<termsNeededUpper.size(); ++index)
			if (termsNeededUpper.get(index) == term) return index;
		termsNeededUpper.add(term);
		return index;
	}
	
	public void computeNeededTerms(ArrayList<AtomPositionTerm> termsNeededUpper)
	{
		for (int i = 0; i<m_conclusions.size(); ++i)
		{
			if (m_conclusions.get(i).m_term1.m_atom !=null) //not constant
				m_conclusions.get(i).m_term1 = new AtomPositionTerm(null, getIndexFromPremise(m_conclusions.get(i).m_term1, termsNeededUpper));
				
			if (m_conclusions.get(i).m_term2.m_atom !=null) //not constant
				m_conclusions.get(i).m_term2 = new AtomPositionTerm(null, getIndexFromPremise(m_conclusions.get(i).m_term2, termsNeededUpper));
		}
		
		m_premDef.m_topJoinNode.computeNeededTerms(termsNeededUpper);
	}
	
	public boolean hasAtom(DefRelAtom atom)
	{
		return false;
	}
	
	public void registerAsWatcherOnRelations()
	{
		m_premDef.m_topJoinNode.registerAsWatcherOnRelations();
	}
	
	public void flush()
	{
		//m_newPremiseMappings.clear();
		m_premDef.m_topJoinNode.flush();
	}
	
	public void readjustProvenanceOnEquality(ResolvedEquality eq, PHFormula phprov, FlatFormula flatprov)
	{
		//make it better for subsumption
		eq.registerAsWatcher(phprov);
		eq.m_placeHolderProvenance.addSum(phprov);
		
		boolean subsumed = eq.m_flatProvenance.addSumCanonical(flatprov, false);
		if (!subsumed)
			ChasedInstance.zeInstance.addEvent(new ProvChangedOnEquality(eq));
	}
	
	public void readjustProvenanceOnEqualities(ArrayList<ResolvedEquality> equalities, PHFormula phprov, FlatFormula flatprov)
	{
		for (int i = 0; i<equalities.size(); ++i)
			readjustProvenanceOnEquality(equalities.get(i), phprov, flatprov);
	}
	
	public void addNewLocalMapping(LocalMapping mapping, Object adder)
	{
		//it may or may not be new; if a flush was done, it may appear new on the top node
		ArrayList<ResolvedEquality> existingConclusions = m_matchedPremiseMappings.get(mapping);
		if (null != existingConclusions) //this has already been treated, we need to update provenance
			readjustProvenanceOnEqualities(existingConclusions, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
		else
		{
			ArrayList<ResolvedEquality> matchedConclusions = new ArrayList<ResolvedEquality>();
			for (int j = 0; j<m_conclusions.size(); ++j)
			{
				ResolvedEquality resolved = getResolvedConclusion(mapping, m_conclusions.get(j));
				if (resolved.m_term1 == resolved.m_term2) continue;		
				
				resolved.setProvenance(mapping.m_flatProvenance,new PHFormula(mapping.m_placeHolderProvenance));
				
				ResolvedEquality realMatch = resolved.m_term1.m_component.addPossiblyExisting(resolved);
				matchedConclusions.add(realMatch);
				
				if (realMatch==resolved) 
					resolved.registerAsWatcher(resolved.m_placeHolderProvenance);
	
				else 
					readjustProvenanceOnEquality(realMatch, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
					
			}
			m_matchedPremiseMappings.put(mapping, matchedConclusions);
		}
	}
	
	
	public void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula flatform, Object adder)
	{
		//the provenance of the mapping has already been updated in the top node
		//we just need to update on conclusions if they exist
		ArrayList<ResolvedEquality> existingConclusions = m_matchedPremiseMappings.get(mapping);
		readjustProvenanceOnEqualities(existingConclusions, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
	}
	
	
	ResolvedEquality getResolvedConclusion(LocalMapping premiseMapping, DefEquality conclusion)
	{
		ResolvedEquality tr = new ResolvedEquality();
		tr.m_term1 = Utilities.getResolvedConclusionTerm(conclusion.m_term1, premiseMapping);
		tr.m_term2 = Utilities.getResolvedConclusionTerm(conclusion.m_term2, premiseMapping);
		return tr;
	}
	
	
	public boolean enforce()
	{
		ChasedInstance.zeInstance.resetNeedsFlush();
		
		if (!m_hasNew) 
			return false;
		while (m_hasNew) 
			pushNew();

		if (ChasedInstance.zeInstance.m_needsFlush)
		{
			ChasedInstance.zeInstance.flushConstraints();
			ChasedInstance.zeInstance.rePushAtoms();
		}

		return true;
	}

	public void pushNew()
	{
		if (m_premDef.m_topJoinNode.m_hasNew)
		{
			m_premDef.m_topJoinNode.pushNew();
			m_hasNew = m_premDef.m_topJoinNode.m_hasNew;
		}
	}
	
	public void refreshDummyMapping()
	{
	}
}
