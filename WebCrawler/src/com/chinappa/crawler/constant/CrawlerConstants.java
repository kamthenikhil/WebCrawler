package com.chinappa.crawler.constant;

import java.io.File;

public class CrawlerConstants {

	public static final String HTTP_PROTOCOL_PREFIX = "http";

	public static final String FTP_PROTOCOL_PREFIX = "ftp";

	public static final String HTTPS_PROTOCOL_PREFIX = "https";

	public static final String HTML_HREF = "href";

	public static final String STOP_WORDS_FILE_PATH_EN = "config"
			+ File.separator + "stopWords_en";

	public static final String OUTPUT_FILE_EXTENTION_HTML = ".html";

	public static final int DEFAULT_NUMBER_OF_THREADS = 16;

	public static final long DEFAULT_MAXIMUM_NUMBER_OF_PAGES_TO_DOWNLOAD = Long.MAX_VALUE;

	public static final int DEFAULT_LEVELS = 5;

	public static final String DEFAULT_INPUT_DIRECTORY = "input";

	public static final String DEFAULT_OUTPUT_DIRECTORY = "output";

	public static final String DEFAULT_MAPPINGS_FILENAME = "mappings.properties";
	
	public static final String DEFAULT_PAGERANK_FILENAME = "pageRanks.properties";

	public static final String DEFAULT_SEED_FILENAME = "seed";

	public static final String PDF_SUFFIX = ".pdf";

	public static final String JPEG_SUFFIX = ".jpeg";

	public static final String JPG_SUFFIX = ".jpg";

	public static final String PNG_SUFFIX = ".png";

	public static final String BMP_SUFFIX = ".bmp";

	public static final String GIF_SUFFIX = ".gif";

	public static final String EDU_SUFFIX = ".edu";
	/**
	 * Resource bundle constants.
	 */
	public static final String RB_NUMBER_OF_THREADS = "threads";

	public static final String RB_LEVELS = "levels";

	public static final String RB_PAGE_LIMIT = "page.limit";

	public static final String RB_INPUT_DIRECTORY = "input.directory";

	public static final String RB_OUTPUT_DIRECTORY = "output.directory";

	public static final String RB_SEED_FILENAME = "seed.file.name";

	public static final String RB_COMPRESSION_ENABLED = "compression.enabled";

	public static final String RB_DUPLICATE_DETECTION_ENABLED = "duplicate.detection.enabled";

	public static final String DEFAULT_SEED_VALUE = "http://www.ucr.edu/";

}
