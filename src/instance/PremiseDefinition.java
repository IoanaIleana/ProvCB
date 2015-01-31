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

import nodes.InnerNode;
import nodes.LeafNode;
import nodes.Node;

import atoms.AtomPositionTerm;
import atoms.DefEquality;
import atoms.DefRelAtom;

public class PremiseDefinition 
{
	public ArrayList<DefRelAtom> m_premiseAtoms;
	public ArrayList<DefEquality> m_premiseEqualities;
	public Node m_topJoinNode;

	public HashMap<String,AtomPositionTerm> m_varsToAtomPositions;
	
	public PremiseDefinition()
	{
		m_varsToAtomPositions = new HashMap<String,AtomPositionTerm>();
		
		m_premiseAtoms = new ArrayList<DefRelAtom>();
		m_premiseEqualities = new ArrayList<DefEquality>();
		
		m_topJoinNode = null;
	}
	
	public AtomPositionTerm getTermFromString(String strTerm)
	{
		AtomPositionTerm term=m_varsToAtomPositions.get(strTerm);
		if (null==term && strTerm.length()>3 && strTerm.substring(0,2).equals("co"))
		{
			term = new AtomPositionTerm(null,0);
			term.m_constant = ChasedInstance.zeInstance.getSpecialTermByName(strTerm);
		}
		return term;
	}
	
	public void readFromFile(BufferedReader br) throws Exception
	{
		Parser parser = new Parser();
		
		//read atoms
		String line = br.readLine();
		parser.parseRelationals(line);
		ArrayList<ArrayList<String>> parsedRelationals = parser.getRelationals();
		
		for (int i = 0; i<parsedRelationals.size(); ++i)
		{
			ArrayList<String> crtParsedRelational = parsedRelationals.get(i);
			Relation crtRelation = ChasedInstance.zeInstance.getRelationByName(crtParsedRelational.get(0));
			DefRelAtom crtAtom = new DefRelAtom(crtRelation,crtParsedRelational.size()-1);
			
			m_premiseAtoms.add(crtAtom);
			for (int j = 1; j<crtParsedRelational.size(); ++j)
					m_varsToAtomPositions.put(crtParsedRelational.get(j),new AtomPositionTerm(crtAtom,j-1) );
		}
		
		//read equalities
		line = br.readLine();
		parser.parseEqualities(line);
		ArrayList<String[]> parsedEqualities = parser.getEqualities();
		for (int i = 0; i< parsedEqualities.size(); ++i)
		{
			String[] crtParsedEquality = parsedEqualities.get(i);
			DefEquality crtEq = new DefEquality();
			crtEq.m_term1 = getTermFromString(crtParsedEquality[0]);
			crtEq.m_term2 = getTermFromString(crtParsedEquality[1]);
			m_premiseEqualities.add(crtEq);
		}
		
		buildTree();
	}
	
	public class NodeCouple
	{
		Node m_node1;
		Node m_node2;
		
		int m_depth;
		
		public int hashCode()
		{
			return m_node1.hashCode()+m_node2.hashCode();
		}
		public NodeCouple(Node node1, Node node2)
		{
			m_node1 = node1;
			m_node2 = node2;
			m_depth = m_node1.m_depth+m_node2.m_depth;
		}
		
		public boolean equals(Object otherobj)
		{
			NodeCouple other =(NodeCouple)otherobj;
			if (  (this.m_node1 == other.m_node1 && this.m_node2 == other.m_node2)
				|| (this.m_node2 == other.m_node1 && this.m_node1 == other.m_node2))
				return true;
			return false;
		}
		
	}

	public void updateIndividualCount(Node node, HashMap<Node, Integer> eqmap, int addval)
	{
		Integer crtval = eqmap.get(node);
		if (null == crtval) eqmap.put(node, addval);
		else eqmap.put(node, crtval+addval);
	}
	
	public void addEqualities(NodeCouple couple, HashMap<NodeCouple, ArrayList<DefEquality>> eqmap, ArrayList<DefEquality> list)
	{
		ArrayList<DefEquality> crtList = eqmap.get(couple);
		if (null == crtList)
			eqmap.put(couple, list);
		else
			crtList.addAll(list);
	}
	
	public void buildTree()
	{
		HashMap<DefRelAtom, Node> nodesForAtoms = new HashMap<DefRelAtom, Node>();
		for (int i = 0; i<m_premiseAtoms.size(); ++i)
			nodesForAtoms.put(m_premiseAtoms.get(i), new LeafNode(m_premiseAtoms.get(i)));
		
		//make initial couples map
		HashMap<NodeCouple, ArrayList<DefEquality>> countEqualitiesForCouples = new HashMap<NodeCouple, ArrayList<DefEquality>>();
		for (DefRelAtom atom1:nodesForAtoms.keySet())
			for (DefRelAtom atom2:nodesForAtoms.keySet())
				if (atom1!=atom2)
				{
					NodeCouple couple = new NodeCouple(nodesForAtoms.get(atom1), nodesForAtoms.get(atom2));
					countEqualitiesForCouples.put(couple,new ArrayList<DefEquality>());
				}
		
		//fill with existing equalities
		for (int i = 0; i<m_premiseEqualities.size(); ++i)
		{
			DefEquality crtEq = m_premiseEqualities.get(i);
			if (crtEq.m_term1.m_atom == null) //constant
				nodesForAtoms.get(crtEq.m_term2.m_atom).addDefEquality(crtEq);
			
			else if (crtEq.m_term2.m_atom == null)//constant
				nodesForAtoms.get(crtEq.m_term1.m_atom).addDefEquality(crtEq);
			
			else
			{
				NodeCouple couple = new NodeCouple(nodesForAtoms.get(crtEq.m_term1.m_atom), nodesForAtoms.get(crtEq.m_term2.m_atom));
				ArrayList<DefEquality> val = countEqualitiesForCouples.get(couple);
				val.add(crtEq);
			}
		}
		
		HashMap<Node, Integer> countEqualitiesPerNode = new HashMap<Node, Integer>();
		for (NodeCouple couple:countEqualitiesForCouples.keySet())
		{
			int val = countEqualitiesForCouples.get(couple).size();
			updateIndividualCount(couple.m_node1, countEqualitiesPerNode, val);
			updateIndividualCount(couple.m_node2, countEqualitiesPerNode, val);
		}
		
		if (countEqualitiesForCouples.size() == 0) //one atom premise
		{
			m_topJoinNode = nodesForAtoms.get(m_premiseAtoms.get(0));
			return;
		}
		
		
		while (true)
		{
			//pick best couple
			int minValue = 0;
			NodeCouple bestCouple = null;
			for (NodeCouple couple:countEqualitiesForCouples.keySet())
			{
				int eqsnode1 = countEqualitiesPerNode.get(couple.m_node1);
				int eqsnode2 = countEqualitiesPerNode.get(couple.m_node2);
				int eqscouple = countEqualitiesForCouples.get(couple).size();
				int min=eqsnode1;
				if (eqsnode2<min) min=eqsnode2;
				int crtValue = min-eqscouple;
				if ((bestCouple == null) || (crtValue < minValue) || (crtValue == minValue && couple.m_depth<bestCouple.m_depth))
				{
					minValue = crtValue;
					bestCouple = couple;
				}
			}
			
			InnerNode mergedNode = new InnerNode(bestCouple.m_node1, bestCouple.m_node2);
			for (DefEquality eq:countEqualitiesForCouples.get(bestCouple))
				mergedNode.addDefEquality(eq);
			//System.out.println("merged node: "+mergedNode.nodeAtoms());
			//for (int i = 0; i<mergedNode.m_defEqualities.size();++i)
			//{
			//	mergedNode.m_defEqualities.get(i).display();
			//	System.out.print(" ");
			//}
			//System.out.println();
		
			
			HashMap<NodeCouple, ArrayList<DefEquality>> auxCountEqualitiesForCouples = new HashMap<NodeCouple, ArrayList<DefEquality>>();
			for (NodeCouple couple:countEqualitiesForCouples.keySet())
			{
				if (couple.equals(bestCouple)) continue;
				else if (couple.m_node1 == bestCouple.m_node1 || couple.m_node1 == bestCouple.m_node2)
				{
					NodeCouple newCouple = new NodeCouple(mergedNode, couple.m_node2);
					addEqualities(newCouple, auxCountEqualitiesForCouples, countEqualitiesForCouples.get(couple));
				}
				else if (couple.m_node2 == bestCouple.m_node1 || couple.m_node2 == bestCouple.m_node2)
				{
					NodeCouple newCouple = new NodeCouple(couple.m_node1, mergedNode);
					addEqualities(newCouple, auxCountEqualitiesForCouples, countEqualitiesForCouples.get(couple));
				}
				else //this is just to be added as is
					addEqualities(couple, auxCountEqualitiesForCouples, countEqualitiesForCouples.get(couple));
			}
			
			if (auxCountEqualitiesForCouples.size() == 0)
			{
				m_topJoinNode = mergedNode;
				//m_topJoinNode.displaySubtree(0);
				break;
			}
			countEqualitiesForCouples = auxCountEqualitiesForCouples;
			countEqualitiesPerNode.clear();
			for (NodeCouple couple:countEqualitiesForCouples.keySet())
			{
				int val = countEqualitiesForCouples.get(couple).size();
				updateIndividualCount(couple.m_node1, countEqualitiesPerNode, val);
				updateIndividualCount(couple.m_node2, countEqualitiesPerNode, val);
			}
		}		
	}
	
}
