package com.chinappa.information.retrieval.util;

import java.util.HashMap;

public class SimHash {

	public static int simHash32(HashMap<String, Integer> tokenMap) {
		int fingerPrintLength = 32;
		int[] bits = new int[fingerPrintLength];
		for (String token : tokenMap.keySet()) {
			int hash = MurmurHash.hash32(token);
			int termFrequency = tokenMap.get(token);
			for (int i = fingerPrintLength; i >= 1; --i) {
				if (((hash >> (fingerPrintLength - i)) & 1) == 1)
					bits[i - 1] += termFrequency;
				else
					bits[i - 1] -= termFrequency;
			}
		}
		int simhash = 0x00000000;
		int one = 0x00000001;
		for (int i = fingerPrintLength; i >= 1; --i) {
			if (bits[i - 1] > 1) {
				simhash |= one;
			}
			one = one << 1;
		}
		return simhash;
	}
}
