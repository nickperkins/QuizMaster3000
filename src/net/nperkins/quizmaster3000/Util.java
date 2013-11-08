package net.nperkins.quizmaster3000;

/* This file is part of QuizMaster3000.

QuizMaster3000 is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

QuizMaster3000 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with QuizMaster3000.  If not, see <http://www.gnu.org/licenses/>. 
*/

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class Util {

	public static Map<Player,Integer> sortScores(HashMap<Player,Integer> map){
        List<Map.Entry<Player,Integer>> entries = new LinkedList<Map.Entry<Player,Integer>>(map.entrySet());
      
        Collections.sort(entries, new Comparator<Map.Entry<Player,Integer>>() {

			@Override
            public int compare(Entry<Player, Integer> o1, Entry<Player, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
      
        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<Player,Integer> sortedMap = new LinkedHashMap<Player,Integer>();
      
        for(Map.Entry<Player,Integer> entry: entries){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
      
        return sortedMap;
    }
	
	public static String formatMessage(String message, Object... args) {
		String prefix = "&d[Quiz]&f "; //TODO: Configuration item
		message = ChatColor.translateAlternateColorCodes('&', prefix + String.format(message, args)); 
		return message;
	}


}
