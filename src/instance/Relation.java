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
import java.util.HashMap;

import nodes.LeafNode;
import atoms.ResolvedRelAtom;

public class Relation 
{
	public String m_name;
	public ArrayList<LeafNode> m_watchers;
	public HashMap<String, ResolvedRelAtom> m_atomsByKey;
	public ArrayList<ResolvedRelAtom> m_atoms;
	
	public Relation(String name)
	{
		m_name = name;
		m_atomsByKey = new HashMap<String, ResolvedRelAtom>();
		m_watchers = new ArrayList<LeafNode>();
		m_atoms = new ArrayList<ResolvedRelAtom>();
	}
	
	public void addWatcher(LeafNode watcher)
	{
		m_watchers.add(watcher);
	}
	
	public void flushWatchers()
	{
		m_watchers.clear();
	}
	
	public void addFresh(ResolvedRelAtom atom)
	{
		atom.computeKey();
		m_atomsByKey.put(atom.m_key,atom);
		m_atoms.add(atom);
		
		for (int i = 0; i<m_watchers.size(); ++i) 
			m_watchers.get(i).notifyHasNew();
	}
	

	public ResolvedRelAtom addPossiblyExisting(ResolvedRelAtom atom)
	{
		atom.computeKey();
		ResolvedRelAtom existing = m_atomsByKey.get(atom.m_key);
		if (null != existing)
			return existing;
		else
		{
			m_atoms.add(atom);
			m_atomsByKey.put(atom.m_key,atom);
			for (int i = 0; i<m_watchers.size(); ++i) 
				m_watchers.get(i).notifyHasNew();
			return atom;
		}
	}
	
	
	public void rePushAllAtoms()
	{
		for (int j = 0; j<m_watchers.size(); ++j) 
			m_watchers.get(j).notifyHasNew();
	}

	public void displayContents()
	{
		for (int i = 0; i<m_atoms.size(); ++i)
			System.out.println(m_atoms.get(i));
	}
}
