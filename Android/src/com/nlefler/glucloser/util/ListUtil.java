package com.nlefler.glucloser.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ListUtil {

	public static <T> List<T> unique(Collection<T> list, Comparator<T> comparator) {		
		List<T> uniqueList = new ArrayList<T>(list);

		Collections.sort(uniqueList, comparator);

		for (int i = 0; i < uniqueList.size() - 1; ++i) {
			T a = uniqueList.get(i);
			T b = uniqueList.get(i + 1);

			if (comparator.compare(a, b) == 0) {
				uniqueList.remove(i);
			}
		}

		return uniqueList;
	}

	public static float average(Collection<Integer> list) {
		float average = 0;

		for (Integer val : list) {
			average += val;
		}

		average /= Math.max(list.size(), 1);

		return average;
	}

	public static float sum(Collection<Integer> list) {
		return sum(list.iterator());
	}

	public static float sum(Iterator it) {
		float sum = 0;

		while (it.hasNext()) {
			sum += (Float)it.next();
		}

		return sum;
	}
}
