import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawler for Edmunds.com.
 * @author namangupta
 *
 */
public class EdmundsCrawler {

	String title;
	String vehicle;
	String date;
	String reviewText;
	Review review = null;
	List<String> fileNames;
	final static String urlFolder = "data/urls";
	final static String outputFolder ="data/Reviews/";
	
	public EdmundsCrawler() {
		fileNames= new ArrayList<String>();
	}

	private void getData(Document doc, PrintWriter pw) throws Exception {

		Elements links = doc.select("a");
		for (Element link : links) {
			String attribute = link.attr("class");
			if (attribute.equalsIgnoreCase("header-3")) {
				
				Document doc1 = Jsoup.connect(link.attr("abs:href"))
						.timeout(15 * 1000).get();
				this.title = doc1
						.getElementsByAttributeValue("class", "header-3")
						.text().trim();
				
				this.vehicle = doc1
						.getElementsByAttributeValue("itemprop", "itemreviewed")
						.text().trim();
				
				this.reviewText = doc1
						.getElementsByAttributeValue("itemprop", "description")
						.text().trim();
				
				this.date = doc1
						.getElementsByAttributeValue("itemprop", "dtreviewed")
						.text().trim();

				
				review = new Review(title, vehicle, reviewText, date.split(" ")[0]);

				System.out.println(review);
				pw.println(review);
				pw.flush();

			}
		}
	}

	private void crawlUrlList() throws Exception {
		
		for(String urlFile : fileNames)
		{
			//Skipping Temporary Files
			if(urlFile.endsWith("~"))
				continue;
			
			BufferedReader reader = new BufferedReader(new FileReader(urlFolder+"//"+urlFile));
			PrintWriter writer = new PrintWriter(outputFolder+urlFile+".txt");
			String url;
			while ((url = reader.readLine()) != null) 
			{
				//Continuous Hit may be detected as bot. So hold for a while and then hit again.
				Thread.sleep(5000);
				url = url.trim();
			
				if (url.length() == 0)
					continue;
				try 
				{
					Document doc = Jsoup.connect(url).timeout(15 * 1000).get();
					getData(doc, writer);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			writer.close();
			reader.close();
		}
	}
	
	public void listFilesForFolder(File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	            fileNames.add(fileEntry.getName());
	            
	        }
	    }
	}


	public static void main(String args[]) throws Exception {
		/*
		 * System.setProperty("http.proxyHost", "****");
		 * System.setProperty("http.proxyPort", "****");
		 */
				
		EdmundsCrawler crawler = new EdmundsCrawler();
		crawler.listFilesForFolder(new File(urlFolder));
		crawler.crawlUrlList();
		
		
	}
}
