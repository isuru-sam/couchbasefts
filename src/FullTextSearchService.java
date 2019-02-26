/**
 * 
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.BooleanQuery;
import com.couchbase.client.java.search.queries.ConjunctionQuery;
import com.couchbase.client.java.search.queries.DisjunctionQuery;
import com.couchbase.client.java.search.queries.MatchPhraseQuery;
import com.couchbase.client.java.search.queries.MatchQuery;
import com.couchbase.client.java.search.queries.NumericRangeQuery;
import com.couchbase.client.java.search.queries.PrefixQuery;
import com.couchbase.client.java.search.queries.RegexpQuery;
import com.couchbase.client.java.search.result.SearchQueryResult;
import com.couchbase.client.java.search.result.SearchQueryRow;
import com.couchbase.fts.FtsConstants;

/**
 * @author eratnch
 *
 */
@Service
public class FullTextSearchService {

	
	private static final Logger log = LoggerFactory.getLogger(FullTextSearchService.class);

	private Bucket bucket;
	
	public FullTextSearchService(Bucket bucket) {
		this.bucket = bucket;
		log.info("******** Bucket :: = " + bucket.name());
	}

	/**
	 * @return the bucket
	 */
	public Bucket getBucket() {
		return bucket;
	}

	public void findByTextMatch(String searchText) throws Exception {
		log.info("findByTextMatch ");
		SearchQueryResult result = bucket.query(
				new SearchQuery(FtsConstants.FTS_IDX_CONF, SearchQuery.matchPhrase(searchText)).fields("summary"));
		log.info("****** total  hits := " + result.hits().size());
		for (SearchQueryRow hit : result.hits()) {
			log.info("****** score := " + hit.score() + " and content := "
					+ bucket.get(hit.id()).content().get("title"));
		}
	}

	public void findByTextFuzzy(String searchText) throws Exception {
		log.info(" findByTextFuzzy ");

		SearchQueryResult resultFuzzy = getBucket()
				.query(new SearchQuery(FtsConstants.FTS_IDX_CONF, SearchQuery.match(searchText).fuzziness(3))
						.fields("topics"));

		log.info("****** total  hits := " + resultFuzzy.hits().size());
		for (SearchQueryRow hit : resultFuzzy.hits()) {

			log.info("****** score := " + hit.score() + " and content := "
					+ bucket.get(hit.id()).content().get("topics"));
		}

	}

	public void findByRegExp(String regexp) throws Exception {
		log.info(" findByRegExp ");
		RegexpQuery rq = new RegexpQuery(regexp).field("topics");
		SearchQueryResult resultRegExp = getBucket().query(new SearchQuery(FtsConstants.FTS_IDX_CONF, rq));
		log.info("****** total  hits := " + resultRegExp.hits().size());
		for (SearchQueryRow hit : resultRegExp.hits()) {

			log.info("****** score := " + hit.score() + " and content := "
					+ bucket.get(hit.id()).content().get("topics"));
		}

	}

	public void findByPrefix(String prefix) throws Exception {
		log.info(" findByPrefix ");
		PrefixQuery pq = new PrefixQuery(prefix).field("summary");
		SearchQueryResult resultPrefix = getBucket()
				.query(new SearchQuery(FtsConstants.FTS_IDX_CONF, pq).fields("summary"));
		log.info("****** total  hits := " + resultPrefix.hits().size());
		for (SearchQueryRow hit : resultPrefix.hits()) {
			log.info("****** score := " + hit.score() + " and content := "
					+ bucket.get(hit.id()).content().get("summary"));
		}
	}

	public void findByMatchPhrase(String matchPhrase) throws Exception {
		log.info(" findByMatchPhrase ");
		MatchPhraseQuery mpq = new MatchPhraseQuery(matchPhrase).field("speakers.talk");
		SearchQueryResult resultPrefix = getBucket()
				.query(new SearchQuery(FtsConstants.FTS_IDX_CONF, mpq).fields("speakers.talk"));
		log.info("****** total  hits := " + resultPrefix.hits().size());
		for (SearchQueryRow hit : resultPrefix.hits()) {
			log.info("****** score := " + hit.score() + " and content := " + bucket.get(hit.id()).content().get("title")
					+ " speakers = " + bucket.get(hit.id()).content().get("speakers"));
		}
	}

	public void findByNumberRange(Integer min, Integer max) throws Exception {
		log.info(" findByNumberRange ");
		NumericRangeQuery nrq = new NumericRangeQuery().min(min).max(max).field("attendees");
		SearchQueryResult resultPrefix = getBucket()
				.query(new SearchQuery(FtsConstants.FTS_IDX_CONF, nrq).fields("title", "attendees", "location"));
		log.info("****** total  hits := " + resultPrefix.hits().size());
		for (SearchQueryRow hit : resultPrefix.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** score := " + hit.score() + " and title := " + row.content().get("title") + " attendees := "
					+ row.content().get("attendees") + " location := " + row.content().get("location"));
		}
	}

	public void findByMatchCombination(String text1, String text2) throws Exception {
		log.info(" findByMatchCombination ");

		MatchQuery mq1 = new MatchQuery(text1).field("topics");

		MatchQuery mq2 = new MatchQuery(text2).field("topics");

		SearchQueryResult match1Result = getBucket().query(
				new SearchQuery(FtsConstants.FTS_IDX_CONF, mq1).fields("title", "attendees", "location", "topics"));

		log.info("****** total  hits for match1 := " + match1Result.hits().size());
		for (SearchQueryRow hit : match1Result.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** scores for match 1 := " + hit.score() + " and title := " + row.content().get("title")
					+ " attendees := " + row.content().get("attendees") + " topics := " + row.content().get("topics"));
		}

		SearchQueryResult match2Result = getBucket().query(
				new SearchQuery(FtsConstants.FTS_IDX_CONF, mq2).fields("title", "attendees", "location", "topics"));
		log.info("****** total  hits for match2 := " + match2Result.hits().size());
		for (SearchQueryRow hit : match2Result.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** scores for match 2:= " + hit.score() + " and title := " + row.content().get("title")
					+ " attendees := " + row.content().get("attendees") + " topics := " + row.content().get("topics"));
		}

		ConjunctionQuery conjunction = new ConjunctionQuery(mq1, mq2);
		SearchQueryResult result = getBucket().query(new SearchQuery(FtsConstants.FTS_IDX_CONF, conjunction)
				.fields("title", "attendees", "location", "topics"));
		log.info("****** total  hits for conjunction query := " + result.hits().size());
		for (SearchQueryRow hit : result.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** scores for conjunction query:= " + hit.score() + " and title := "
					+ row.content().get("title") + " attendees := " + row.content().get("attendees") + " topics := "
					+ row.content().get("topics"));
		}

		DisjunctionQuery dis = new DisjunctionQuery(mq1, mq2);
		SearchQueryResult resultDis = getBucket().query(
				new SearchQuery(FtsConstants.FTS_IDX_CONF, dis).fields("title", "attendees", "location", "topics"));
		log.info("****** total  hits for disjunction query := " + resultDis.hits().size());
		for (SearchQueryRow hit : resultDis.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** scores for disjunction query:= " + hit.score() + " and title := "
					+ row.content().get("title") + " attendees := " + row.content().get("attendees") + " topics := "
					+ row.content().get("topics"));
		}

		BooleanQuery bool = new BooleanQuery().must(mq1).mustNot(mq2);
		SearchQueryResult resultBool = getBucket().query(
				new SearchQuery(FtsConstants.FTS_IDX_CONF, bool).fields("title", "attendees", "location", "topics"));
		log.info("****** total  hits for booelan query := " + resultBool.hits().size());
		for (SearchQueryRow hit : resultBool.hits()) {
			JsonDocument row = bucket.get(hit.id());
			log.info("****** scores for resultBool query:= " + hit.score() + " and title := "
					+ row.content().get("title") + " attendees := " + row.content().get("attendees") + " topics := "
					+ row.content().get("topics"));
		}
	}

}
