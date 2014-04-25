/**
 * 
 */
package nl.ktao.lucene.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 * @author ktao
 *
 */
public class Indexer {

	public static final Analyzer ANALYZER = new TweetAnalyzer(Version.LUCENE_47);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// setup index appendable
		String indexPath = "/usr/local/lucene/index-trec2011";
		String tweetsFile = "/usr/local/data/datasift/ds_stream_10000.json";
		boolean create = true; // appending or ... later, first test if the specified index exists and decide the value of this variable
		
		System.out.println("Indexing to directory '" + indexPath + "'...");
		
		int all = 0;
		int indexed = 0;
		
		try {
			Date start = new Date();
			
			Directory dir = FSDirectory.open(new File(indexPath));
			
			//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, Indexer.ANALYZER);
			
			if (create) { // Create a new index in the directory, removing any
		        // previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			} else { // Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: Increase the memory size to 256MB
			// iwc.setRAMBufferSizeMB(256.0);
			
			// get documents - tweets from file
			// DataSift file source
			BufferedReader br = new BufferedReader(new FileReader(tweetsFile));
			String line = null;
			JsonNode node = null;
//			HashMap<Long, String> tweets = new HashMap<Long, String>();
			ObjectMapper mapper = new ObjectMapper();
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			while ((line = br.readLine()) != null) {
				all++;
				node = mapper.readTree(line);
				if (node.path("twitter").path("lang").asText().equals("en") && node.path("twitter").path("retweet").getClass() == MissingNode.class) { // only indexing English tweets
					// analyzer, tokenizer
//					tweets.put(, );
					indexTweets(writer, node.path("twitter").path("id").asLong(), node.path("twitter").path("text").asText());
					if(++indexed % 50 == 0) {
//						indexTweets(writer, tweets);
						System.out.println(indexed + "/" + all + " tweets indexed / all.");
//						tweets = new HashMap<Long, String>();
					}
				}
			}
			br.close();
			
			// feed them into index()		
			
//			indexTweets(writer, tweets);
			System.out.println(indexed + "/" + all + " tweets indexed / all.");
			
			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error at " + all + " line");
		}
	}

	static void indexTweets(IndexWriter writer, Long id, String contents) {
		Document tweet = new Document();
		final FieldType textOptions = new FieldType();
	    textOptions.setIndexed(true);
	    textOptions.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
	    textOptions.setStored(true);
	    textOptions.setTokenized(true);  
	    
		try {
				// the id of the tweet, should be searchable
				tweet.add(new LongField("id", id, Field.Store.YES));
				// the content of the tweet, will be tokenized
				tweet.add(new Field("contents", contents, textOptions));
				
				writer.addDocument(tweet);
		} catch (Exception e) {
			
		}
	}
}
