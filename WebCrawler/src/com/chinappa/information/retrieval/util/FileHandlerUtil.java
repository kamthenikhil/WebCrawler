package com.chinappa.information.retrieval.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chinappa.information.retrieval.constants.CommonConstants;
import com.chinappa.information.retrieval.constants.CrawlerConstants;

public class FileHandlerUtil {

	/**
	 * The following method will be used to read the list of entries from a file
	 * and return array of strings which contains individual entries.
	 * 
	 * @param filePath
	 */
	public static ArrayList<String> readFile(String filePath) {
		// TODO Auto-generated method stub
		ArrayList<String> listOfEntries = new ArrayList<String>();
		String line = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(filePath)));
			line = reader.readLine();
			while (line != null && !line.isEmpty()) {
				listOfEntries.add(line.trim());
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			System.out.println("Seed file not found on the given location: "
					+ filePath);
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}

		return listOfEntries;
	}

	/**
	 * The following method is used to write the data into a file.
	 * 
	 * @param data
	 * @param outputDirectory
	 * @param fileName
	 * @param url
	 * @param extension
	 * @throws IOException
	 */
	public static void writeToFile(String data, String outputDirectory,
			String fileName, String url, String extension) {

		File file = new File(outputDirectory + File.separator + fileName
				+ extension);
		BufferedWriter writer = null;
		try {
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(url + data);
		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}

	public static void writeToCompressedHTMLFile(String data,
			String outputDirectory, String fileName, String url,
			String extension) {

		File file = new File(outputDirectory + File.separator + fileName
				+ extension);
		FileOutputStream output = null;
		Writer writer = null;
		try {
			output = new FileOutputStream(file);
			writer = new OutputStreamWriter(new GZIPOutputStream(output),
					CrawlerConstants.ENCODING_CHARSET);
			writer.write(data);
			writer.flush();
		} catch (IOException e) {
		} catch (Exception e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
			try {
				if (output != null)
					output.close();
			} catch (IOException e) {
			}
		}
	}

	public static String fetchFromCompressedHTMLFile(String outputDirectory,
			String fileName) {

		File file = new File(outputDirectory + File.separator + fileName);
		FileInputStream input = null;
		Reader reader = null;
		StringBuilder decompressedData = new StringBuilder();
		try {
			input = new FileInputStream(file);
			reader = new InputStreamReader(new GZIPInputStream(input),
					CrawlerConstants.ENCODING_CHARSET);
			int data = reader.read();
			while (data != -1) {
				decompressedData.append((char) data);
				data = reader.read();
			}
		} catch (IOException e) {

		} finally {
			try {
				if (reader != null)
					reader.close();
				if (input != null)
					input.close();
			} catch (IOException e) {
			}
		}
		return decompressedData.toString();
	}

	public static void writeURLFileToDisk(String urlString,
			String outputDirectory, String fileName, String extension) {
		URL url;
		InputStream in = null;
		FileOutputStream fos = null;
		try {
			url = new URL(urlString);
			in = url.openStream();
			fos = new FileOutputStream(new File(outputDirectory
					+ File.separator + fileName + extension));
			int length = -1;
			byte[] buffer = new byte[1024];
			while ((length = in.read(buffer)) > -1) {
				fos.write(buffer, 0, length);
			}
			fos.flush();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}

	public static void writeIntoPropertiesFile(Map<String, String> map,
			String directory, String fileName) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream(directory + File.separator + fileName);
			for (String key : map.keySet()) {
				if (!map.get(key).isEmpty()) {
					prop.setProperty(key, map.get(key));
				}
			}
			prop.store(output, null);
		} catch (IOException io) {
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}

		}
	}

	public static Properties readFromPropertiesFile(String directory,
			String fileName) {
		Properties properties = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(directory + File.separator + fileName);
			properties.load(input);
		} catch (IOException io) {
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}

		}
		return properties;
	}

	/**
	 * The following method extracts the paragraph text from the document.
	 * 
	 * @param doc
	 * @return
	 */
	public static String fetchDocumentText(Document doc) {

		Elements paragraphs = doc.select(CrawlerConstants.HTML_PARAGRAPHS);
		StringBuilder documentText = new StringBuilder();
		if (paragraphs != null && paragraphs.size() > 0) {
			for (Element paragraph : paragraphs) {
				documentText.append(paragraph.text());
				documentText.append(CommonConstants.SPACE);
			}
		}
		paragraphs = doc.select(CrawlerConstants.HTML_LINKS_HREF);
		if (paragraphs != null && paragraphs.size() > 0) {
			for (Element paragraph : paragraphs) {
				documentText.append(paragraph.text());
				documentText.append(CommonConstants.SPACE);
			}
		}
		return documentText.toString();
	}
	
	/**
	 * The following method extracts the metadata content from the document.
	 * 
	 * @param doc
	 * @return
	 */
	public static String fetchDocumentMetadata(Document doc) {

		Elements elements = doc.select(CrawlerConstants.HTML_META_CONTENT);
		StringBuilder metadataContent = new StringBuilder();
		if (elements != null && elements.size() > 0) {
			for (Element element : elements) {
				metadataContent.append(element.attr(CrawlerConstants.CONTENT_FIELD));
				metadataContent.append(CommonConstants.SPACE);
			}
		}
		//System.out.println(metadataContent);
		return metadataContent.toString();
	}
}
