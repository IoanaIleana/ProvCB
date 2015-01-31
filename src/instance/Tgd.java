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
import provenanceEvents.ProvChangedOnAtom;
import atoms.AtomPositionTerm;
import atoms.DefRelAtom;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;
import flatProvenance.FlatFormula;

public class Tgd extends Node
{
	public PremiseDefinition m_premDef;
	private ArrayList<DefRelAtom> m_conclusions;
	
	//incremental update
	public HashMap<LocalMapping, ArrayList<ResolvedRelAtom>> m_matchedPremiseMappings;
	public LinkedList<LocalMapping> m_newPremiseMappings;
	public AdaptiveMappingsIndex m_joinMappingsId;
	
	//skolems
	private HashMap<String, Integer> m_SkolemsByName;
	private int m_countSkolems;
	public ResolvedTerm[] m_resolvedSkolems;
	
	
	public Tgd()
	{
		super();
		
		m_premDef = new PremiseDefinition();
		m_conclusions = new ArrayList<DefRelAtom>();
		
		m_SkolemsByName = new HashMap<String, Integer>();
		m_countSkolems = 0;
		m_resolvedSkolems = null;
		
		m_matchedPremiseMappings = new HashMap<LocalMapping, ArrayList<ResolvedRelAtom>>();
		m_newPremiseMappings = new LinkedList<LocalMapping>();
		m_joinMappingsId = new AdaptiveMappingsIndex(10); //number of bits-function of projected attrs?
	}
	
	public int getSkolemIndex(String strSkolem)
	{
		Integer skindex = m_SkolemsByName.get(strSkolem);
		if (null == skindex)
		{
			skindex = m_countSkolems;
			m_countSkolems++;
			m_SkolemsByName.put(strSkolem, skindex);
		}
		return skindex;
	}
	
	public void readFromFile(BufferedReader br) throws Exception
	{
		m_premDef.readFromFile(br);
		m_premDef.m_topJoinNode.m_parent = this;
		
		//read conclusions
		Parser parser = new Parser();
		String line = br.readLine();
		parser.parseRelationals(line);
		ArrayList<ArrayList<String>> parsedRelationals = parser.getRelationals();	
		for (int i = 0; i<parsedRelationals.size(); ++i)
		{
			ArrayList<String> crtParsedRelational = parsedRelationals.get(i);
			DefRelAtom conclusion = new DefRelAtom(ChasedInstance.zeInstance.getRelationByName(crtParsedRelational.get(0)), crtParsedRelational.size()-1);
			for (int j = 0; j<crtParsedRelational.size()-1; j++)
			{
				String strTerm = crtParsedRelational.get(j+1);
				conclusion.m_terms[j] = m_premDef.getTermFromString(strTerm);
				if (conclusion.m_terms[j] == null) //this is not a position in the premise
				{
					conclusion.m_terms[j] = new AtomPositionTerm(null,-1);
					conclusion.m_terms[j].m_skolemIndex = getSkolemIndex(crtParsedRelational.get(j+1));
				}
			}
			m_conclusions.add(conclusion);
		}
    	
		m_resolvedSkolems = new ResolvedTerm[m_countSkolems];
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
			for (int k = 0; k<m_conclusions.get(i).m_terms.length; k++) 
			{
				AtomPositionTerm term = m_conclusions.get(i).m_terms[k];
				if (term.m_atom == null) //constant or skolem
					continue;

				m_conclusions.get(i).m_terms[k] = new AtomPositionTerm(null, getIndexFromPremise(m_conclusions.get(i).m_terms[k], termsNeededUpper));		
			}
		m_premDef.m_topJoinNode.computeNeededTerms(termsNeededUpper);
	}
	
	public boolean hasAtom(DefRelAtom atom)
	{
		return false;
	}
	
	private void makeFreshSkolems()
	{
		for (int i = 0; i<m_resolvedSkolems.length; ++i)
			m_resolvedSkolems[i] = ChasedInstance.zeInstance.createFreshSkolemSpecialTerm();
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
	
	
	public void readjustProvenanceOnAtom(ResolvedRelAtom atom, PHFormula phprov, FlatFormula flatprov)
	{
		atom.registerAsWatcher(phprov);
		atom.m_placeHolderProvenance.addSum(phprov);
		

		boolean subsumed = atom.m_flatProvenance.addSumCanonical(flatprov, false);
		if (!subsumed)
			ChasedInstance.zeInstance.addEvent(new ProvChangedOnAtom(atom));
	}
	
	public void readjustProvenanceOnAtoms(ArrayList<ResolvedRelAtom> atoms, PHFormula phprov, FlatFormula flatprov)
	{
		for (int i = 0; i<atoms.size(); ++i)
			readjustProvenanceOnAtom(atoms.get(i), phprov, flatprov);
	}

	public void addNewLocalMapping(LocalMapping mapping, Object adder)
	{
		//it may or may not be new; if a flush was done, it may appear new on the top node
		ArrayList<ResolvedRelAtom> existingConclusions = m_matchedPremiseMappings.get(mapping);
		if (null != existingConclusions) //this has already been treated, we need to update provenance
			readjustProvenanceOnAtoms(existingConclusions, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
		else
		{
			makeFreshSkolems();

			ArrayList<ResolvedRelAtom> matchedConclusions = new ArrayList<ResolvedRelAtom>();
			for (int j = 0; j<m_conclusions.size(); ++j)
			{
				ResolvedRelAtom resolved = getResolvedConclusion(mapping, m_conclusions.get(j));
				resolved.setProvenance(mapping.m_flatProvenance,new PHFormula(mapping.m_placeHolderProvenance));
				ResolvedRelAtom realMatch = resolved.m_relation.addPossiblyExisting(resolved);
				matchedConclusions.add(realMatch);
				
				if (realMatch==resolved)//we added a new atom 
					resolved.registerAsWatcher(resolved.m_placeHolderProvenance);				
				else 
					readjustProvenanceOnAtom(realMatch, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
				
			}
			m_matchedPremiseMappings.put(mapping, matchedConclusions);
		}
	}
	
	
	public void addExistingMappingAdditionalProvenance(LocalMapping mapping, PHFormula phform, FlatFormula flatform, Object adder)
	{
		//the provenance of the mapping has already been updated in the top node
		//we just need to update on conclusions if they exist
		ArrayList<ResolvedRelAtom> existingConclusions = m_matchedPremiseMappings.get(mapping);
		readjustProvenanceOnAtoms(existingConclusions, mapping.m_placeHolderProvenance, mapping.m_flatProvenance);
	}

	ResolvedRelAtom getResolvedConclusion(LocalMapping premiseMapping, DefRelAtom conclusion)
	{
		ResolvedRelAtom tr = new ResolvedRelAtom(conclusion.m_relation,conclusion.m_terms.length);
		for (int i= 0 ; i<tr.m_terms.length; ++i)
		{
			tr.m_terms[i] = Utilities.getResolvedConclusionTerm(conclusion.m_terms[i], premiseMapping);
			if (null == tr.m_terms[i]) //this is a skolem
				tr.m_terms[i] = m_resolvedSkolems[conclusion.m_terms[i].m_skolemIndex];
		}
		return tr;
	}
	
	
	
	public boolean enforce()
	{
		if (!m_hasNew) 
			return false;
		while (m_hasNew) 
			pushNew();
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
