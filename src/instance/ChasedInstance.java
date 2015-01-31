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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import provenanceEvents.ProvenanceEvent;
import atoms.AtomPositionTerm;
import atoms.ResolvedRelAtom;
import atoms.ResolvedTerm;
import flatProvenance.FlatConjunct;
import flatProvenance.FlatFormula;

//the chased instance (global container)
public class ChasedInstance 
{
	public static ChasedInstance zeInstance = new ChasedInstance();
	
	public int countSpecialTerms = 0;
	public int countProvenanceSymbols = 0;
	public int countConnectedComponents = 0;
	
	public HashMap<String, Relation> m_relationsByName;
	
	public HashMap<String, ResolvedTerm> m_specialTermsByName;
	public ArrayList<ResolvedTerm> m_specialTerms;
	
	public HashMap<String, ProvenanceSymbol> m_provenanceSymbolsByName;
	public ArrayList<ProvenanceSymbol> m_provenanceSymbols;
	
	public ArrayList<ConnectedComponent> m_connectedComponents;
	
	public ArrayList<Tgd> m_tgds;
	public ArrayList<Egd> m_egds;
	
	public Query m_query;
	
	public LinkedList<ProvenanceEvent> m_events;	
	
	public boolean m_needsFlush;
	public boolean m_usePlaceHolders;
	
	
	public ChasedInstance()
	{
		m_relationsByName = new HashMap<String,Relation>();
		m_specialTermsByName = new HashMap<String, ResolvedTerm>();
		m_specialTerms = new ArrayList<ResolvedTerm>();
		m_provenanceSymbolsByName = new HashMap<String, ProvenanceSymbol>();
		m_provenanceSymbols = new ArrayList<ProvenanceSymbol>();
		
		m_connectedComponents = new ArrayList<ConnectedComponent>();
		
		m_tgds = new ArrayList<Tgd>();
		m_egds = new ArrayList<Egd>();
		
		m_query = new Query();
		
		m_events=new LinkedList<ProvenanceEvent>();
		
		m_needsFlush = false;
		m_usePlaceHolders = true;
	}
	
	
	public long readFromPABackChaseFile(String fileName) throws Exception
	{
		long time = 0;
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		int countSymbols = Integer.valueOf(line).intValue();
		FlatConjunct.m_countLongsProv = countSymbols/64+1;
		
		//read query
		line = br.readLine();
		long startTime = System.currentTimeMillis();
		m_query.readFromFile(br);
		time+=System.currentTimeMillis()-startTime;
	
	    line = br.readLine();
	    line = br.readLine();
	    int countConstraints = Integer.valueOf(line).intValue();
	    
	    //read constraints
	    for (int i = 0; i<countConstraints; ++i)
	    {
	    	line = br.readLine();
	        if (line.equals("TGD")) //read TGD
	        {
	        	Tgd crtTgd = new Tgd();
	        	crtTgd.readFromFile(br);
	        	crtTgd.computeNeededTerms(new ArrayList<AtomPositionTerm>());
	        	crtTgd.registerAsWatcherOnRelations();
	        	m_tgds.add(crtTgd);
	        } 
	        else //read EGD
	        {
	        	Egd crtEgd = new Egd();
	        	crtEgd.readFromFile(br);
	        	crtEgd.computeNeededTerms(new ArrayList<AtomPositionTerm>());
	        	crtEgd.registerAsWatcherOnRelations();
	        	m_egds.add(crtEgd);
	        }
	    }
	    
	    //read instance
	    Parser parser = new Parser();
	    line=br.readLine();
	    startTime = System.currentTimeMillis();
	    while (true)
	    {
	    	line=br.readLine();
	    	if (null==line) break;
	    	parser.flush();
	    	
	    	ArrayList<String> atom = parser.parseAtom(line);
	    	if (null == atom) break;
	    	Relation crtRelation = getRelationByName(atom.get(0));
    		ResolvedRelAtom crtAtom = new ResolvedRelAtom(crtRelation,atom.size()-1);

    		for (int j = 0; j<atom.size()-1; ++j)
    			crtAtom.m_terms[j] = getSpecialTermByName(atom.get(j+1));
    		
	    	ArrayList<String> provenance = parser.parseProvenance(line);
	    	crtAtom.setProvenance(getFlatFormula(provenance), null);

	    	crtRelation.addFresh(crtAtom);
	    }
	    br.close();
	    time+=System.currentTimeMillis()-startTime;
	    return time;
	}
	
	
	public long PABackChase()
	{
		long startTime = System.currentTimeMillis();
		boolean changed = false;
		do
		{
			changed = false;
			for (int i = 0; i<m_tgds.size(); ++i)
				changed = changed || m_tgds.get(i).enforce();
			for (int i = 0; i<m_egds.size(); ++i)
				changed = changed || m_egds.get(i).enforce();
		} while (changed);
		
		treatEvents();

		return System.currentTimeMillis()-startTime;
	}
	
	
	public void computeRewritings(String filename) throws Exception
	{
		long time=readFromPABackChaseFile(filename);
		
		long timeChase=PABackChase();
		System.out.println("time in chase: "+timeChase);
		time+=timeChase;
		
		//start computing rewritings
		long startTime = System.currentTimeMillis();
		
		removeWatchers();
		flushConstraints();
		m_query.computeNeededTerms(new ArrayList<AtomPositionTerm>());
		m_query.registerAsWatcherOnRelations();
		m_usePlaceHolders = false;
		rePushAtoms();
		
		m_query.enforce();
		
		long timeInRW = System.currentTimeMillis()-startTime;
		System.out.println((timeInRW+time)+" "+m_query.m_provenance.m_conjuncts.size());
		
		//m_query.m_provenance.displayRows();
	}
	
	
	public void resetNeedsFlush()
	{
		m_needsFlush = false;
	}
	
	public void setNeedsFlush()
	{
		m_needsFlush = true;
	}
	
	public Relation getRelationByName(String s)
	{
		Relation r = m_relationsByName.get(s);
		if (null == r)
		{
			r = new Relation(s);
			m_relationsByName.put(s, r);
		}
		return r;
	}
	
	public ResolvedTerm getSpecialTermByName(String s)
	{
		ResolvedTerm t = m_specialTermsByName.get(s);
		if (null == t)
		{
			t = createFreshSpecialTerm(s);
			m_specialTermsByName.put(s, t);
		}
		return t;
	}
	
	private ResolvedTerm createFreshSpecialTerm(String s)
	{
		ResolvedTerm t = new ResolvedTerm(s,countSpecialTerms);
		m_specialTerms.add(t);
		countSpecialTerms++;
		return t;
	}
	
	public ResolvedTerm createFreshSkolemSpecialTerm()
	{
		return createFreshSpecialTerm("sk"+countSpecialTerms);
	}
	
	
	public ProvenanceSymbol getProvenanceSymbolByName(String name)
	{
		ProvenanceSymbol symbol = m_provenanceSymbolsByName.get(name);
		if (null == symbol)
		{
			symbol = new ProvenanceSymbol(name, countProvenanceSymbols);
			m_provenanceSymbols.add(symbol);
			countProvenanceSymbols++;
			m_provenanceSymbolsByName.put(name,symbol);
		}
		return symbol;
	}
	
	public FlatFormula getFlatFormula(ArrayList<String> symbols)
	{
		FlatConjunct conjunct = new FlatConjunct();
		
		for (int i = 0; i<symbols.size();++i)
			if (!symbols.get(i).equals("none"))
				conjunct.addSymbol(getProvenanceSymbolByName(symbols.get(i)));
		return new FlatFormula(conjunct);
	}
	
	public void addConnectedComponent(ConnectedComponent comp)
	{
		m_connectedComponents.add(comp);
	}
	

	public void addEvent(ProvenanceEvent event)
	{
		m_events.add(event);
	}
	
	public void treatEvents()
	{
		//System.out.println("treating events: "+m_events.size());
		while (m_events.size()!=0)
		{
			ProvenanceEvent event = m_events.removeFirst();
			event.propagate();
		}
	}
	
	public void flushConstraints()
	{
		for (int i = 0; i<m_tgds.size(); ++i)
			m_tgds.get(i).flush();
		
		for (int i = 0; i<m_egds.size(); ++i)
			m_egds.get(i).flush();;
	}
	
	public void removeWatchers()
	{
		for (String name : m_relationsByName.keySet()) 
		{
			Relation rel = m_relationsByName.get(name);
			rel.flushWatchers();
		}
	}
	
	public void rePushAtoms()
	{
		for (String name : m_relationsByName.keySet()) 
		{
			Relation rel = m_relationsByName.get(name);
			rel.rePushAllAtoms();
		}
	}
	
	public void display()
	{
		for (String name : m_relationsByName.keySet()) 
		{
			Relation rel = m_relationsByName.get(name);
			rel.displayContents();
		}
		
		for (int i = 0; i<m_connectedComponents.size(); ++i)
			m_connectedComponents.get(i).displayContents();
	}
	
	
}
