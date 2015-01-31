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

import instance.LocalMapping;

import java.util.ArrayList;
import java.util.LinkedList;

public class AdaptiveMappingsIndex {
	int m_countBuckets;
	int m_countBits;
	int m_complementCountBits;
	int m_moduloBitMask;
	int m_countEntries;
	
	public ArrayList<LinkedList<LocalMapping>> m_storage;
	
	public AdaptiveMappingsIndex(int countBits)
	{
		m_countEntries = 0;
		m_countBits = countBits;
		m_complementCountBits = 32-m_countBits;
		m_moduloBitMask = 0;
		for (int i = 0; i<countBits; ++i)
			m_moduloBitMask+=1<<i;
		
		m_countBuckets = 1<<m_countBits;
		m_storage = new ArrayList<LinkedList<LocalMapping>>(m_countBuckets);
		for (int  i = 0; i<m_countBuckets; ++i)
			m_storage.add(null);
	}
	private void insertLocal(LocalMapping mapping, int key)
	{
		int index = (key>>m_complementCountBits)&m_moduloBitMask;
		LinkedList<LocalMapping> crt = m_storage.get(index);
		if (null == crt)
		{
			crt = new LinkedList<LocalMapping>();
			m_storage.set(index,crt);
		}
		crt.add(mapping);
		m_countEntries++;
	}
	
	public void insert(LocalMapping mapping, int key)
	{
		 insertLocal(mapping, key);
	}
	
	
	public LinkedList<LocalMapping> getMatching(int key)
	{
		return m_storage.get((key>>m_complementCountBits)&m_moduloBitMask);
	}
	
	
	public void flush()
	{
		for (int  i = 0; i<m_countBuckets; ++i)
			if (null!=m_storage.get(i))
				m_storage.get(i).clear();
	}
	
	
	private LocalMapping getAndInsertLocal(LocalMapping mapping, int key)
	{
		int index = (key>>m_complementCountBits)&m_moduloBitMask;
		LinkedList<LocalMapping> crt = m_storage.get(index);
		if (null == crt)
		{
			crt = new LinkedList<LocalMapping>();
			m_storage.set(index,crt);
			crt.add(mapping);
			m_countEntries++;
			return null;
		}
		for (LocalMapping ex:crt)
			if (ex.isSameAs(mapping))
				return ex;
		crt.add(mapping);
		m_countEntries++;
		return null;
	}
	
	/*
	public void reHash()
	{
		int oldCountBuckets = m_countBuckets;
		m_countBuckets*=2;
		m_moduloBitMask+=1<<m_countBits;
		m_countBits++;
		m_complementCountBits--;
		
		ArrayList<LinkedList<LocalMapping>> newStorage = new ArrayList<LinkedList<LocalMapping>>(m_countBuckets);
		for (int  i = 0; i<m_countBuckets; ++i)
			newStorage.add(null);
		for (int i = 0; i<oldCountBuckets; ++i) 
		{
			LinkedList<LocalMapping> currentList = m_storage.get(i);
			if (null == currentList) continue;
			
			for (LocalMapping mapping:currentList)
			{
				int index = (mapping.m_hashCodeId>>m_complementCountBits)&m_moduloBitMask;
				LinkedList<LocalMapping> crt = newStorage.get(index);
				if (null == crt)
				{
					crt = new LinkedList<LocalMapping>();
					newStorage.set(index,crt);
				}
				crt.add(mapping);
			}		
		}	
		m_storage=newStorage;
	}
	*/
	
	public LocalMapping getAndInsert(LocalMapping mapping, int key)
	{
		//if (m_countEntries >= m_countBuckets)
		//	reHash();
		return getAndInsertLocal(mapping, key);
	}
}
