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

package tests;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Tester 
{
	//chain-of-stars configuration
	public static void generatePABackChasePartial(int countHubs, int countCorners, int countViews, int viewLength, String fileName) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		//number of provenance symbols
		writer.write((countHubs+countHubs*countViews+countHubs*countCorners)+"\n");

		
		
		//query
		writer.write("query\n");
		//relationals
		for (int i = 1; i<=countHubs; ++i)
		{
			writer.write("R"+i+"(");
			writer.write("k"+i);
			for (int j = 1; j<=countCorners; j++)
				writer.write(",a1_"+i+"_"+j);
			writer.write(",f"+i);
			writer.write("),");
			
			for (int j = 1; j<=countCorners; j++)
			{
				writer.write("S"+i+"_"+j+"("+"a2_"+i+"_"+j+",b"+i+"_"+j+")");
				if (j<countCorners || i<countHubs) writer.write(",");
			}
		}	
		writer.write("\n");
		//equalities
		for (int i = 1; i<=countHubs; ++i)
		{
			for (int j = 1; j<=countCorners; j++)
			{
				writer.write("a1_"+i+"_"+j+"=a2_"+i+"_"+j);
				if (i<countHubs || j<countCorners)
					writer.write(",");
			}
			if (i<countHubs)
				writer.write("f"+i+"=k"+(i+1)+",");
		}
		writer.write("\n");
		//empty conclusion
		writer.write("\n");
	
		
		
		//constraints
		writer.write("constraints\n");
		//number of constraints
		writer.write((countHubs+ countHubs*countViews)+"\n");
	
		//EGDs (key constraints)
		for (int i = 1; i<=countHubs;++i)
		{
			writer.write("EGD\n");
			//premise
			writer.write("R"+i+"(");
			writer.write("k1");
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a1"+k);
			writer.write(",f1),");
			
			writer.write("R"+i+"(");
			writer.write("k2");
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a2"+k);
			writer.write(",f2)\n");
			//equalities
			writer.write("k1=k2\n");
			//conclusion
			for (int j = 1; j<=countCorners; ++j)
				writer.write("a1"+j+"=a2"+j+",");
			writer.write("f1=f2\n");
		}
	
		//TGDs(view definitions)
		for (int i = 1; i<=countHubs; ++i)
			for (int j = 1; j<=countViews; ++j)
			{
				writer.write("TGD\n");
				//premise
				writer.write("V"+i+"_"+j);
				writer.write("(k");
				for (int k = 0; k<viewLength; ++k)
					writer.write(",b"+(j+k));
				//equalities
				writer.write(")\n\n");
				//conclusion
				writer.write("R"+i+"(");
				writer.write("k");
				for (int k = 1; k<=countCorners; k++)
					writer.write(",a"+k);
				writer.write(",f)");
				
				for (int k = 0; k<viewLength; ++k)
					writer.write(",S"+i+"_"+(j+k)+"(a"+(j+k)+",b"+(j+k)+")");
				writer.write("\n");
			}
	
		
		
		//instance
		writer.write("instance\n");
		for (int i = 1; i<=countHubs; ++i)
		{
			//r atoms
			writer.write("R"+i);
			writer.write("(k"+i);
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a"+i+"_"+k);
			writer.write(",k"+(i+1)+")");
			writer.write(")  r"+i+"\n");
			
			//s atoms
			for (int k = 1; k<=countCorners; k++)
				writer.write("S"+i+"_"+k+"(a"+i+"_"+k+",b"+i+"_"+k+")  s"+i+"_"+k+"\n");
			
			//views
			for (int j = 1; j<=countViews; j++)
			{
				writer.write("V"+i+"_"+j);
				writer.write("(k"+i);
				for (int k = 0; k<viewLength; k++)
					writer.write(",b"+i+"_"+(j+k));
				writer.write(")  v"+i+"_"+j+"\n");
			}
		}
		
		writer.close();
	}
	
	
	
	
	
	public static void generatePABackChasePartialPlus(int countHubs, int countCorners, int countViews, int viewLength, String fileName) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		//number of provenance symbols
		writer.write((countHubs+countHubs*countViews+countHubs*countCorners)+"\n");

		
		
		//query
		writer.write("query\n");
		//relationals
		for (int i = 1; i<=countHubs; ++i)
		{
			writer.write("R"+i+"(");
			writer.write("k"+i);
			for (int j = 1; j<=countCorners; j++)
				writer.write(",a1_"+i+"_"+j);
			writer.write(",f"+i);
			writer.write("),");
			
			for (int j = 1; j<=countCorners; j++)
			{
				writer.write("S"+i+"_"+j+"("+"a2_"+i+"_"+j+",b"+i+"_"+j+")");
				if (j<countCorners || i<countHubs) writer.write(",");
			}
		}	
		writer.write("\n");
		//equalities
		for (int i = 1; i<=countHubs; ++i)
		{
			for (int j = 1; j<=countCorners; j++)
			{
				writer.write("a1_"+i+"_"+j+"=a2_"+i+"_"+j);
				if (i<countHubs || j<countCorners)
					writer.write(",");
			}
			if (i<countHubs)
				writer.write("f"+i+"=k"+(i+1)+",");
		}
		writer.write("\n");
		//empty conclusion
		writer.write("\n");
	
		
		
		//constraints
		writer.write("constraints\n");
		//number of constraints
		writer.write((countHubs+countHubs*countViews+countHubs*countCorners)+"\n");
	
		//EGDs (key constraints)
		for (int i = 1; i<=countHubs;++i)
		{
			writer.write("EGD\n");
			//premise
			writer.write("R"+i+"(");
			writer.write("k1");
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a1"+k);
			writer.write(",f1),");

			writer.write("R"+i+"(");
			writer.write("k2");
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a2"+k);
			writer.write(",f2)\n");
			//equalities
			writer.write("k1=k2\n");
			//conclusion
			for (int j = 1; j<=countCorners; ++j)
				writer.write("a1"+j+"=a2"+j+",");
			writer.write("f1=f2\n");
		}
	
		//TGDs(view definitions and foreign keys)
		for (int i = 1; i<=countHubs; ++i)
		{
			for (int j = 1; j<=countViews; ++j)
			{
				writer.write("TGD\n");
				//premise
				writer.write("V"+i+"_"+j);
				writer.write("(k");
				for (int k = 0; k<viewLength; ++k)
					writer.write(",b"+(j+k));
				//equalities
				writer.write(")\n\n");
				//conclusion
				writer.write("R"+i+"(");
				writer.write("k");
				for (int k = 1; k<=countCorners; k++)
					writer.write(",a"+k);
				writer.write(",f)");

				for (int k = 0; k<viewLength; ++k)
					writer.write(",S"+i+"_"+(j+k)+"(a"+(j+k)+",b"+(j+k)+")");
				for (int k = 0; k<viewLength; ++k)
					writer.write(",T"+i+"_"+(j+k)+"(b"+(j+k)+",c"+(j+k)+")");
				writer.write("\n");
			}
			//foreign keys
			for (int j = 1; j<=countCorners; j++)
			{
				writer.write("TGD\n");
				writer.write("S"+i+"_"+j+"(a,b)\n");
				writer.write("\n");
				writer.write("T"+i+"_"+j+"(b,c)\n");
			}
		}
	
		
		
		//instance
		writer.write("instance\n");
		for (int i = 1; i<=countHubs; ++i)
		{
			//r atoms
			writer.write("R"+i);
			writer.write("(k"+i);
			for (int k = 1; k<=countCorners; k++)
				writer.write(",a"+i+"_"+k);
			writer.write(",k"+(i+1)+")");
			writer.write(")  r"+i+"\n");

			//s atoms
			for (int k = 1; k<=countCorners; k++)
				writer.write("S"+i+"_"+k+"(a"+i+"_"+k+",b"+i+"_"+k+")  s"+i+"_"+k+"\n");

			//views
			for (int j = 1; j<=countViews; j++)
			{
				writer.write("V"+i+"_"+j);
				writer.write("(k"+i);
				for (int k = 0; k<viewLength; k++)
					writer.write(",b"+i+"_"+(j+k));
				writer.write(")  v"+i+"_"+j+"\n");
			}
		}

		writer.close();
	}
	
}
