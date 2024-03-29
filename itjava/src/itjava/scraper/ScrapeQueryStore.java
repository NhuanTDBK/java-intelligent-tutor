package itjava.scraper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

/**
 * @author Vasanth
 *
 */
public class ScrapeQueryStore {
	private Connection _conn;
	ScrapeFaqStore faqStore = new ScrapeFaqStore();
	public ScrapeResult ScrapeQueryRepoUpdate(LinkedHashSet <ScrapeData> scrapeResultsObj, String scrapeQuery, int tutorialInfoId) {
		/*Writing file name & URL to DB*/
		int searchQueryId = 0;
		try {
			GetConnection();
			String insertSql = "insert into ScrapeQuery(searchQueryText, tutorialInfoId) values(?,?);";
			PreparedStatement insertScrapeQuery = _conn.prepareStatement(insertSql);
			insertScrapeQuery.setString(1, scrapeQuery);
			insertScrapeQuery.setInt(2, tutorialInfoId);
			int rowsInserted = insertScrapeQuery.executeUpdate();
			System.out.println("Num of rows inserted in table ScrapeQuery: " + rowsInserted);
			PreparedStatement fileNameSql = _conn.prepareStatement("select max(searchQueryId) as searchQueryId from ScrapeQuery where searchQueryText = ?",PreparedStatement.RETURN_GENERATED_KEYS);
			
			
			fileNameSql.setString(1, scrapeQuery);
			ResultSet rs = fileNameSql.executeQuery();
			while (rs.next()) {
				System.out.println("Query Id : " + rs.getInt("searchQueryId"));
				System.out.println("Search Query : " + scrapeQuery);
				System.out.println("Tutorial Info Id : " + tutorialInfoId);
			}
			System.out.println("Search Query inserted in table successfully");
			ResultSet rsKey = fileNameSql.getGeneratedKeys();
			searchQueryId = rsKey.getInt(1);
			while (rsKey.next()) {
				scrapeResultsObj = faqStore.ScrapeFaqRepoUpdate(rsKey.getInt(1), scrapeResultsObj);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				DBConnection.CloseConnection(_conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		ScrapeResult scrapeResult = new ScrapeResult();
	    scrapeResult.setScrapeResultsObj(scrapeResultsObj);
	    scrapeResult.setSearchQueryId(searchQueryId);
		return scrapeResult;
	}
		private void GetConnection() throws Exception {
			_conn = DBConnection.GetConnection();
	}
	
}