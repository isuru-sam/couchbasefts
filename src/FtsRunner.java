

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FtsRunner implements CommandLineRunner {
	
	/** The Constant logger. */
	private static final Logger log = LoggerFactory.getLogger(FtsRunner.class);
	
	@Autowired
	FullTextSearchService fts;


	@Override
	public void run(String... arg0) throws Exception {
        log.info("...Searching initiated...");

		       
        fts.findByTextMatch("developer");
        
		fts.findByTextFuzzy("sysops");
		
		fts.findByRegExp("[a-z]*lization");
		
        fts.findByRegExp("[a-z]*\\s*reality");
        
        fts.findByPrefix("micro");
        
        fts.findByMatchPhrase("DevOps System Administrators");
        
        fts.findByMatchPhrase("Docker with couchbase");
        
        fts.findByNumberRange(5000, 30000);
        
        fts.findByMatchCombination("aws", "containers");
        
        log.info("...Searching completed...");
	}
	
		

}
