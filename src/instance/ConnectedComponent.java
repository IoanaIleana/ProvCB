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

import java.util.ArrayList;
import java.util.HashSet;

import provenanceEvents.ProvChangedInComp;
import atoms.ResolvedEquality;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;
import flatProvenance.FlatFormula;

public class ConnectedComponent 
{
	private ArrayList<ResolvedTerm> m_specialTerms;
	private ArrayList<ResolvedEquality> m_equalities; //leaf equalities
	public HashSet<ResolvedRelAtom> m_atomWatchers;
	public HashSet<ResolvedEquality> m_equalityWatchers;
	
	public ArrayList<ArrayList<FlatFormula>> m_provenanceInClosure;
	public ArrayList<ArrayList<FlatFormula>> m_copyClosure;
	
	
	public static FlatFormula m_dummyFlatFormula = new FlatFormula();
	public int m_index;
	
	public ConnectedComponent(ResolvedTerm term)
	{
		m_index = ChasedInstance.zeInstance.countConnectedComponents;
		ChasedInstance.zeInstance.countConnectedComponents++;
		
		m_specialTerms = new ArrayList<ResolvedTerm>();
		m_specialTerms.add(term);
		
		m_equalities = new ArrayList<ResolvedEquality>();
		m_atomWatchers = new HashSet<ResolvedRelAtom>();
		m_equalityWatchers = new HashSet<ResolvedEquality>();
		
		ChasedInstance.zeInstance.addConnectedComponent(this);
		
		m_provenanceInClosure = new ArrayList<ArrayList<FlatFormula>>();
		m_provenanceInClosure.add(new ArrayList<FlatFormula>());
		
		m_copyClosure = new ArrayList<ArrayList<FlatFormula>>();
	}
	
	public void adjustCopyClosure()
	{
		for(int i = 0; i<m_provenanceInClosure.size(); ++i)
		{
			if (i==m_copyClosure.size())
				m_copyClosure.add(new ArrayList<FlatFormula>());
			
			int initSize = m_copyClosure.get(i).size();
			for (int j = initSize; j<m_provenanceInClosure.get(i).size(); ++j)
				m_copyClosure.get(i).add(null);
		}
	}
	
	public void addSpecialTerm(ResolvedTerm term)
	{
		term.m_indexInComponent = m_specialTerms.size();
		m_specialTerms.add(term);
	}
	
	public void mergeComponentIntoThis(ConnectedComponent other, ResolvedEquality link)
	{
		//closure
		computeMergedClosureProvenance(other, link);
		m_provenanceInClosure.addAll(other.m_provenanceInClosure);
		adjustCopyClosure();
		
		//terms
		for (int i=0; i<other.m_specialTerms.size(); ++i)
		{
			other.m_specialTerms.get(i).m_component = this;
			addSpecialTerm(other.m_specialTerms.get(i));
		}
		
		//equalities
		for (int i = 0; i<other.m_equalities.size(); ++i)
			m_equalities.add(other.m_equalities.get(i));
		m_equalities.add(link);
	
		
		m_atomWatchers.addAll(other.m_atomWatchers);
		m_equalityWatchers.addAll(other.m_equalityWatchers);
	
		//erase other
		other.m_equalities = null;
		other.m_specialTerms = null;
		other.m_atomWatchers = null;
		other.m_equalityWatchers = null;
		other.m_provenanceInClosure = null;
		
		//notify	
		ChasedInstance.zeInstance.setNeedsFlush();
	}
	
	public void computeMergedClosureProvenance(ConnectedComponent other, ResolvedEquality link)
	{
		for (int i = 0; i<m_specialTerms.size(); ++i)
			for (int j = 0; j<other.m_specialTerms.size(); j++)
			{
				ResolvedTerm term1 = m_specialTerms.get(i);
				ResolvedTerm term2 = other.m_specialTerms.get(j);
				
				FlatFormula prov = new FlatFormula();
				prov.initWith(link.m_flatProvenance);
				
				if (term1 != link.m_term1) 
					prov.addProductCanonical(getClosureProvenance(term1, link.m_term1));
		
				if (term2!=link.m_term2) 
					prov.addProductCanonical(other.getClosureProvenance(term2, link.m_term2));
				m_provenanceInClosure.get(term1.m_indexInComponent).add(prov);
			}
	}
	
	
	public FlatFormula getClosureProvenance(ResolvedTerm term1, ResolvedTerm term2)
	{
		int index1=term1.m_indexInComponent;
		int index2=term2.m_indexInComponent;
		
		if (index1 > index2) {int aux = index1; index1=index2; index2=aux;}
		return m_provenanceInClosure.get(index1).get(index2-index1-1);
	}
	
	
	
	public ResolvedEquality addPossiblyExisting(ResolvedEquality eq)
	{
		if (eq.m_term1.m_component!=eq.m_term2.m_component)
		{
			mergeComponentIntoThis(eq.m_term2.m_component, eq);
			return eq;
		}
		else
		{
			//check if it's there
			for (int i = 0; i<m_equalities.size(); ++i)
				if (m_equalities.get(i).isSameAs(eq)) return m_equalities.get(i);
			
			m_equalities.add(eq);
			
			//it's a new equality
			boolean provChanged = recomputeClosureEqualities(eq);
			if (provChanged && (m_atomWatchers.size() != 0 || m_equalityWatchers.size() !=0)) 
				ChasedInstance.zeInstance.addEvent(new ProvChangedInComp(m_specialTerms.get(0)));
			
			return eq;
		}
	}
	
	public void addWatcher(ResolvedRelAtom atom)
	{
		m_atomWatchers.add(atom);
	}
	
	public void addWatcher(ResolvedEquality eq)
	{
		m_equalityWatchers.add(eq);
	}

	
	public boolean recomputeClosureEqualities(ResolvedEquality eq)
	{
		//System.out.println("recomputing closure eq");
		boolean changed = false;
		for (int i = 0; i<m_provenanceInClosure.size(); ++i)
		{
			ResolvedTerm term1 = m_specialTerms.get(i);
			ArrayList<FlatFormula> crtProvenanceRow = m_provenanceInClosure.get(i);
			
			for (int j = 0; j< crtProvenanceRow.size(); j++)
			{
				ResolvedTerm term2 = m_specialTerms.get(j+i+1);
				FlatFormula crtProvenance = crtProvenanceRow.get(j);
				
				m_copyClosure.get(i).set(j, new FlatFormula()); //this can be done by clear!
				FlatFormula newProvenance = m_copyClosure.get(i).get(j);
			
				m_dummyFlatFormula.clear();
				m_dummyFlatFormula.initWith(eq.m_flatProvenance);
				if (term1!=eq.m_term1) 
					m_dummyFlatFormula.addProductCanonical(getClosureProvenance(term1, eq.m_term1));
				if (term2!=eq.m_term2) 
					m_dummyFlatFormula.addProductCanonical(getClosureProvenance(term2, eq.m_term2));
				newProvenance.addSumCanonical(m_dummyFlatFormula, false);
		
				m_dummyFlatFormula.clear();
				m_dummyFlatFormula.initWith(eq.m_flatProvenance);
				if (term1!=eq.m_term2) 
					m_dummyFlatFormula.addProductCanonical(getClosureProvenance(term1, eq.m_term2));
				if (term2!= eq.m_term1) 
					m_dummyFlatFormula.addProductCanonical(getClosureProvenance(term2, eq.m_term1));
				newProvenance.addSumCanonical(m_dummyFlatFormula, false);
				
				if (!newProvenance.isSubsumedBy(crtProvenance))
				{
					changed = true;
				}
			}
		}
		
		if (changed)
		{
			//System.out.println("changed");
			for (int i = 0; i<m_provenanceInClosure.size(); ++i)
				for (int j = 0; j< m_provenanceInClosure.get(i).size(); j++)
					m_provenanceInClosure.get(i).get(j).addSumCanonical(m_copyClosure.get(i).get(j), false);
		}
				
		return changed;
	}
	
	public void provenanceChanged(ResolvedEquality eq)
	{
		boolean provChanged = recomputeClosureEqualities(eq);
		if (provChanged) ChasedInstance.zeInstance.addEvent(new ProvChangedInComp(m_specialTerms.get(0)));
	}

	
	public void displayContents()
	{
		if (null == m_equalities) return;
		System.out.println("\n\nconnected component");
		System.out.println("raw equalities");
		for (int i = 0; i< m_equalities.size(); ++i)
			System.out.println(m_equalities.get(i));
	
		
		System.out.println("closure equalities");
		for (int i = 0; i<m_provenanceInClosure.size(); ++i)
		{
			ArrayList<FlatFormula> crtRow = m_provenanceInClosure.get(i);
			for (int j = 0; j<crtRow.size(); ++j)
			{
				System.out.print(m_specialTerms.get(i).m_name+"="+m_specialTerms.get(j+i+1).m_name+" prov:");
				System.out.println(crtRow.get(j));
			}
		}
		
	}
}
