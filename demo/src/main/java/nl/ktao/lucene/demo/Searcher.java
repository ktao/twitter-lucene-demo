/**
 * 
 */
package nl.ktao.lucene.demo;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author ktao
 *
 */
public class Searcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String index = "/usr/local/lucene/index-trec2011";;
	    String field = "contents";
	    String rawQuery = "Japan";
	    
	    IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(index)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new LMDirichletSimilarity(2500.0f));
		    // :Post-Release-Update-Version.LUCENE_XY:
		    
		    QueryParser parser = new QueryParser(Version.LUCENE_47, field, Indexer.ANALYZER);
		    
	        Date start = new Date();
	        Query query = parser.parse(rawQuery);
	        TopDocs docs = searcher.search(query, null, 100);
	        
	        int rank = 1;
	        for (ScoreDoc doc : docs.scoreDocs) {
	        	int docid = doc.doc;
	        	Document tweet = searcher.doc(docid);
	        	System.out.println(rank++ + " DOCID " + docid + " " + doc.score + " " + tweet.get("id"));
	        }
	        
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
