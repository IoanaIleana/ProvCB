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
import instance.ChasedInstance;

public class Main {

	public static void main(String[] args) throws Exception
	{	
		if (args[0].equals("starpar"))
			PAChaseStarPartial(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]),Integer.valueOf(args[4]));
		else if (args[0].equals("starparplus"))
			PAChaseStarPartialPlus(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]),Integer.valueOf(args[4]));
	}
	
	public static void PAChaseStarPartial(int hubs, int corners, int views, int viewLength) throws Exception
	{
		Tester.generatePABackChasePartial(hubs,corners,views,viewLength, "testpabackchase.txt");
		ChasedInstance.zeInstance.computeRewritings("testpabackchase.txt");
	}
	
	public static void PAChaseStarPartialPlus(int hubs, int corners, int views, int viewLength) throws Exception
	{
		Tester.generatePABackChasePartialPlus(hubs,corners,views,viewLength, "testpabackchase.txt");
		ChasedInstance.zeInstance.computeRewritings("testpabackchase.txt");
	}
	
}
