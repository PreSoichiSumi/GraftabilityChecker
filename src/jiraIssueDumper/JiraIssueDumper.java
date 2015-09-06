package jiraIssueDumper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

public class JiraIssueDumper {

	final private static URI jiraServerUri = URI.create("https://issues.apache.org/jira/");
	//private static String jira_login ="warmingup";
	//private static String pass = "sumi1375";
	//private static List<String> analysisTargetList =Arrays.asList("OPENJPA");//("","","","")
	private static int counter=0;


	//あるプロジェクトの所望のissueを取得し，KeyMapにあるissueのみを取得する．
	//このあと，取得したissueをについてJIRAに問い合わせるゾ
	//target.. OPENJPA , 大文字小文字区別あるゾ．詳しくはjira apacheで検索時のURLみて調べてね
	//keyMap..commitに含まれてたkey
	//return: commitId,issueType
    public static Map<String,String> dumpIssue(String target,Map<String,String> keyMap) throws URISyntaxException, JSONException, IOException,Exception {

        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient restClient = factory.create(jiraServerUri, new AnonymousAuthenticationHandler());
        Map<String,String> targetIssueId=new HashMap<String,String>();
        List<Iterable<Issue>> issueList= new ArrayList<Iterable<Issue>>();
        //final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "jira_login", "pass");
        //Promise<User> promise = restClient.getUserClient().getUser("warmingup");
        //User user = promise.claim()
        Set<String> search = new HashSet<String>();
        search.add("*all");
        try {

    		Promise<SearchResult> searchJqlPromise = restClient.getSearchClient().
        			searchJql("project = "+target+" AND status in Closed AND resolution = Fixed", 50, 0, search );
    		int resTotal =searchJqlPromise.claim().getTotal();
    		int pageItrt=0;
        	if(resTotal!=0){
        		while(pageItrt<=resTotal){		//ページイテレータがAPIから見つからなかったから自分でめくる
        			searchJqlPromise=restClient.getSearchClient().
	            			searchJql("project = "+target+" AND status in Closed AND resolution = Fixed", 50, pageItrt, search );
        			issueList.add(searchJqlPromise.claim().getIssues());
		        	//Project project = restClient.getProjectClient().getProject("OPENJPA").get();
		            //final Issue issue = restClient.getIssueClient().getIssue("TST-63239").claim();
		            //System.out.println(issue);

		        	pageItrt+=50;
        		}
    		}

        	for (Iterable<Issue> iterable : issueList) {	//所望のissueがcommitに含まれてるならissueIdを格納
				for (Issue issue : iterable) {
					if( ! keyMap.containsKey(issue.getKey()) ){
    					System.out.println(issue.getKey()+" is not contained\n");
    				}else {
    					targetIssueId.put( keyMap.get( issue.getKey() ),issue.getIssueType().toString() );
					}
				}
			}
        }
        finally {
        	System.out.println("counter : "+counter);
            restClient.close();
        }
        return targetIssueId;
    }

}

/*
JQLは以下のようになっている．

以下は，OPENJPAからBUG，IMPROVEMENTで，かつResolvedなissueを取り出す場合のもの
https://issues.apache.org/jira/browse/OPENJPA-2576?
jql=project%20%3D%20OPENJPA%20AND%20issuetype%20in%20(Bug%2C%20Improvement)%20
AND%20status%20%3D%20Resolved

%20 半角スペース
%3D 半角イコール
%2C 半角カンマ

条件を追加するとき
" AND ..."

複数条件のとき
" AND issuetype in (BUG, Improvement) "

issueの名前が分かっているとき
" AND key = OPENJPA-2576 "
を追加
*/

/*メモ
 /String auth = new String(Base64.encode("warmingup:sumi1375"));
		//Client client = Client.create();
		//WebResource webResource = client.resource("https://issues.apache.org/jira/projects/");
		/*
		ClientResponse response = webResource.header("Authorization", "Basic " + auth).
				type("application/json").accept("application/json").get(ClientResponse.class);
		int statuscode=response.getStatus();
		if( statuscode == 401){
			throw new AuthenticationException("");
		}
		String entity = response.getEntity(String.class);
		System.out.println(entity);
*/

