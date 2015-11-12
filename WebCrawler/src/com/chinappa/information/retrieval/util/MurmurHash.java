package com.chinappa.information.retrieval.util;

import java.nio.charset.Charset;

public class MurmurHash {

	public static int hash32(String doc) {
		byte[] buffer = doc.getBytes(Charset.forName("utf-8"));
		return murmurhash3_32(buffer, 0, buffer.length, 0);
	}

	public static int murmurhash3_32(byte[] data, int offset, int len, int seed) {

		int c1 = 0xcc9e2d51;
		int c2 = 0x1b873593;

		int h1 = seed;
		int roundedEnd = offset + (len & 0xfffffffc);

		for (int i = offset; i < roundedEnd; i += 4) {
			int k1 = (data[i] & 0xff) | ((data[i + 1] & 0xff) << 8)
					| ((data[i + 2] & 0xff) << 16) | (data[i + 3] << 24);
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 *= c2;

			h1 ^= k1;
			h1 = (h1 << 13) | (h1 >>> 19);
			h1 = h1 * 5 + 0xe6546b64;
		}

		// tail
		int k1 = 0;

		switch (len & 0x03) {
		case 3:
			k1 = (data[roundedEnd + 2] & 0xff) << 16;
		case 2:
			k1 |= (data[roundedEnd + 1] & 0xff) << 8;
		case 1:
			k1 |= data[roundedEnd] & 0xff;
			k1 *= c1;
			k1 = (k1 << 15) | (k1 >>> 17);
			k1 *= c2;
			h1 ^= k1;
		default:
		}

		h1 ^= len;

		h1 ^= h1 >>> 16;
		h1 *= 0x85ebca6b;
		h1 ^= h1 >>> 13;
		h1 *= 0xc2b2ae35;
		h1 ^= h1 >>> 16;

		return h1;
	}
}
