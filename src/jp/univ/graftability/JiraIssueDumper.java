package jp.univ.graftability;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class JiraIssueDumper {
	private String projectName;
	final private int SLEEP_TIME=5000;
	final private int PAGE_SIZE=50;

	public JiraIssueDumper(String projectName) {
		super();
		this.projectName = projectName;
	}

	//Jira Apacheの検索結果と同じのが全部取得できたで
	public List<Iterable<Issue>> getIssueList()
			throws Exception {
		final URI jiraServerUri = URI.create("https://issues.apache.org/jira/");
		final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		final JiraRestClient restClient = factory.create(jiraServerUri,
				new AnonymousAuthenticationHandler());
		List<Iterable<Issue>> issueList = new ArrayList<Iterable<Issue>>();
		Set<String> search = new HashSet<String>();
		search.add("*all");
		try {

			Promise<SearchResult> searchJqlPromise = restClient
					.getSearchClient()
					.searchJql(
							"project = "
									+ projectName
									+ " AND status = Closed AND resolution = Fixed AND issuetype = Bug",
							PAGE_SIZE, 0, search);
			int resTotal = searchJqlPromise.claim().getTotal();
			int pageItrt = 0;
			if (resTotal != 0) {
				while (pageItrt <= resTotal) { // ページイテレータがAPIから見つからなかったから自分でめくる
					searchJqlPromise = restClient
							.getSearchClient()
							.searchJql(
									"project = "
											+ projectName
											+ " AND status = Closed AND resolution = Fixed AND issuetype = Bug",
									PAGE_SIZE, pageItrt, search);

					issueList.add(searchJqlPromise.claim().getIssues());
					pageItrt += PAGE_SIZE;

					System.out.println("Waiting for 5 seconds.");
					Thread.sleep(SLEEP_TIME);
				}
			}
		} catch(Exception e){
			System.out.println(e);
			System.out.println();
		} finally {
			restClient.close();
		}
		return issueList;
	}
}
