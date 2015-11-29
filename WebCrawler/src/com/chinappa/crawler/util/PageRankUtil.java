package com.chinappa.crawler.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRankUtil {

	public static Map<String, Float> calculatePageRank(
			Map<String, List<String>> inLinkMap,
			Map<String, Integer> outLinkCount, float epsilon,
			float dampingFactor) {

		Map<String, Float> initialPageRankMap = new HashMap<String, Float>();
		Map<String, Float> finalPageRankMap = new HashMap<String, Float>();
		for (String page : inLinkMap.keySet()) {

			initialPageRankMap.put(page, 1f);
		}

		int totalPages = inLinkMap.size();

		int counter = 0;
		while (true) {
			counter++;
			System.out.println("Counter: " + counter);
			for (String key : inLinkMap.keySet()) {
				float temp = 0.0f;
				for (String inPages : inLinkMap.get(key)) {
					temp += initialPageRankMap.get(inPages) == null ? 0f
							: initialPageRankMap.get(inPages)
									/ outLinkCount.get(inPages);
				}
				float pageRank = (1 - dampingFactor) / totalPages
						+ dampingFactor * temp;
				finalPageRankMap.put(key, pageRank);
			}
			if (checkConvergence(initialPageRankMap, finalPageRankMap, epsilon)) {
				break;
			}
			for (String key : initialPageRankMap.keySet()) {
				initialPageRankMap.put(key, finalPageRankMap.get(key));
			}
		}
		return finalPageRankMap;
	}

	private static boolean checkConvergence(
			Map<String, Float> initialPageRankMap,
			Map<String, Float> finalPageRankMap, float epsilon) {
		for (String key : finalPageRankMap.keySet()) {
			float diff = Math.abs(initialPageRankMap.get(key)
					- finalPageRankMap.get(key));
			if (diff > new Float(epsilon)) {
				return false;
			}
		}
		return true;
	}
}
