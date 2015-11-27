package com.chinappa.crawler.test;

import com.chinappa.crawler.WebCrawler;

/**
 * The class is used for initializing the crawler.
 * 
 * @author nikhil
 *
 */
public class CrawlerTest {

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		WebCrawler crawler = new WebCrawler();
		crawler.init();
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Time elapsed: " + elapsedTime / 1000 + " secs");
		System.exit(0);
	}
}