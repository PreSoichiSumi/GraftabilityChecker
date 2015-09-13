package jp.univ.graftability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import com.atlassian.jira.rest.client.api.domain.Issue;

/**
 *
 * @author s-sumi
 * リポジトリから所望の条件のコミットを取得するクラス
 * コンストラクタでrepository, projectName, issueListの
 * 設定が必要とされます。
 */
public class CommitRetriever {
	private Repository repo;
	private List<Iterable<Issue>> issueList;
	private String projectName;

	public CommitRetriever(Repository repo, List<Iterable<Issue>> issueList,
			String projectName) {
		super();
		this.repo = repo;
		this.issueList = issueList;
		this.projectName = projectName;
	}
	/**
	 * 子を持ち，closedかつFixedなCommitを返します．
	 * @param repo
	 * @param projectName
	 * @return
	 * @throws Exception
	 */
	public List<Pair<RevCommit, Issue>> retrieveCommits() throws Exception {
		List<RevCommit> comList = getCommits(RevSort.REVERSE);	//一瞬
		List<RevCommit> childComList = getChildCommits(comList);
		//List<Iterable<Issue>> issueList = getIssueList(projectName);	//1473で数分
		List<Pair<RevCommit, Issue>> resList = new ArrayList<Pair<RevCommit, Issue>>();

		for (RevCommit com : childComList) {	//一瞬
			String issueKey = getIssueKey(com.getFullMessage(), projectName);
			if (issueKey != null) {
				Issue tmpIssue = getClosedIssue(issueList, issueKey);
				if (tmpIssue != null)
					resList.add(new MutablePair<RevCommit, Issue>(com, tmpIssue));
			}
		}
		return resList;
	}
	private  List<RevCommit> getCommits(RevSort direction) throws Exception {
		List<RevCommit> commitList = new ArrayList<>();
		RevWalk rw = getInitializedRevWalk(repo, direction);
		RevCommit commit = rw.next();
		while (true) {
			if (commit == null)
				break;
			commitList.add(commit);
			commit = rw.next();
		}
		return commitList;
	}
	private List<RevCommit> getChildCommits(List<RevCommit> commits) throws Exception {
		List<RevCommit> childCommits = new ArrayList<>();
		for (RevCommit commit : commits) {
			if(commit.getParentCount()!=0){	//親をもてば
				childCommits.add(commit);
			}
		}

		return childCommits;
	}
	// Reverseで最古から最新へ
	private RevWalk getInitializedRevWalk(Repository repo,
			RevSort revSort) throws AmbiguousObjectException,
			IncorrectObjectTypeException, IOException, MissingObjectException {
		RevWalk rw = new RevWalk(repo);
		AnyObjectId headId;

		headId = repo.resolve(Constants.HEAD);
		RevCommit root = rw.parseCommit(headId);
		rw.sort(revSort);
		rw.markStart(root); // この時点ではHeadをさしている．nextで最初のコミットが得られる．
		return rw;
	}
	/**
	 * コミットコメントからprojectのキーを捜し，
	 * 大文字でキーかえす
	 * @param target
	 * @param project
	 * @return key (UpperCase)
	 */
	private static String getIssueKey(String target,String project){
		//apacheプロジェクトのIssuekey命名規則
		String regex=project+"-"+"[1-9]+";//プロジェクト名，ハイフン，数字1つ以上

		Pattern p=Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(target);
		List<String> keyArray= new ArrayList<>();
		String key=null;
	    while (m.find()) {
			keyArray.add(m.group());
		}
	    if (keyArray.size()!=1) {
			key=null;
		} else {
			key=keyArray.get(0).toUpperCase(Locale.US);
		}
	    return key;
	  }
	private static Issue getClosedIssue(List<Iterable<Issue>> issuelist,
			String issueKey) {// commitにIssueKeyがふくまれていればそれを返す
		Issue resIssue = null;
		for (Iterable<Issue> iterable : issuelist) {
			for (Issue issue : iterable) {
				if (issue.getKey().equalsIgnoreCase(issueKey)) {
					resIssue = issue;
					break;
				}
			}
		}
		return resIssue;
	}
	public Repository getRepo() {
		return repo;
	}
	public void setRepo(Repository repo) {
		this.repo = repo;
	}
	public List<Iterable<Issue>> getIssueList() {
		return issueList;
	}
	public void setIssueList(List<Iterable<Issue>> issueList) {
		this.issueList = issueList;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	/**
	 * コミットコメントからRegexの文字列をさがし，
	 * コメント内に1つ含まれていたらキーを返す
	 * getIssueKeyの代わりにこっちを使ってもよい
	 * ちょっと条件変えないと使えない。deprecated。
	 * @param target
	 * @param regex
	 * @return
	 */
	@Deprecated
	private static String getIssueKeyRegex(String target,String regex){
		Pattern p=Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(target);
		List<String> keyArray= new ArrayList<>();
		String key=null;
	    while (m.find()) {
			keyArray.add(m.group());
		}
	    if (keyArray.size()!=1) {
			key=null;
		} else {
			key=keyArray.get(0).toUpperCase(Locale.US);
		}
	    return key;
	  }
}
