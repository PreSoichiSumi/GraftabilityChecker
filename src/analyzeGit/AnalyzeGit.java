package analyzeGit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.text.edits.TextEdit;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

import difflib.Chunk;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;

public class AnalyzeGit {

	final static String reposPath = "F:\\s-sumi\\PlasticSurgery";
	final static String testPath = "F:\\dataset\\Git\\PlasticSurgery\\openjpa\\";
	final static String resPath = "F:\\s-sumi\\Res\\";
	final static String uciPath = "G:\\s-sumi\\NormVarDist\\";
	static String[] reposPaths = { "\\" };

	final static boolean removeBrackets=false;
	final static boolean analyze100Commit=false;


	static List<File> fileList = new ArrayList<File>();

	static File reposFolder = new File(reposPath);
	static File reposArray[];
	static Watch allTime=new Watch();

	static int generalCounter = 0;

	public static void main(String[] args) throws IOException, GitAPIException,
			Exception {

		// ファイル・フォルダ処理
		File repos = new File(reposPath);
		reposPaths = repos.list();

		reposArray = reposFolder.listFiles(); // 絶対パスで帰ってくる

		System.out.println("in");
		expExtendedRange();
		allTime.check("all");
		System.out.println("time:" + allTime.toString());

	}

	private static AbstractTreeIterator prepareTreeParser(
			Repository repository, String objectId) throws IOException,
			MissingObjectException, IncorrectObjectTypeException {
		// from the commit we can build the tree which allows us to construct
		// the TreeParser
		RevWalk walk = new RevWalk(repository);
		RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
		RevTree tree = walk.parseTree(commit.getTree().getId());

		CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
		ObjectReader oldReader = repository.newObjectReader();
		try {
			oldTreeParser.reset(oldReader, tree.getId());
		} finally {
			oldReader.release();
		}

		walk.dispose();

		return oldTreeParser;
	}

	private static void makeFileList(File file) {
		if (file.isDirectory()) {
			File[] innerFiles = file.listFiles();
			for (File tmp : innerFiles) {
				makeFileList(tmp);
			}
		} else if (file.isFile()) {
			if (file.getName().endsWith(".java")) {
				fileList.add(file);
			}
		}
	}
	/**
	 * 書きかけ．ゴミかな?
	 * @throws Exception
	 */
	@Deprecated
	private static void commitMessageSpectater()throws Exception{
		for (File repos : reposArray) {
			PrintWriter pw = getPrintWriter(repos);

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder
					.setGitDir(new File(repos.getAbsolutePath()+"\\" + Constants.DOT_GIT))
					.readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.build();

			//ここはさすがに同じPlastic Surgery と同じはず． 2004-2012っていうのは出力後に処理する
			List<Pair<RevCommit, Issue>> closedChildCommits = getClosedChildCommit(repository, repos.getName());
			List<Pair<RevCommit, List<Set<String>>>> graftableLineList
						= new ArrayList<Pair<RevCommit, List<Set<String>>>>();

			for (Pair<RevCommit, Issue> pair : closedChildCommits) {

		//	Set<String> addedLines = getAddedLines(pair.getLeft(), repository);

			//if(addedLines.isEmpty())continue; //追加された行がなければ処理しない

			//Set<String> graftableLines = getGraftableLines(pair.getLeft(),addedLines, repository); // parentからgraftableか調べる


			System.out.println("commit solved");
			}

		}

	}
	/**
	 * Reproduction:再現の意味
	 * @throws Exception
	 */
	@Deprecated
	private static void executeReproduction()throws Exception{
		Set<String> datasetSources = new HashSet<>();
		datasetSources = getRawSourceString(uciPath);
		//allTime.check("gettingDataset");
		for (File repos : reposArray) {
			PrintWriter pw = getPrintWriter(repos);

			Watch wtch = new Watch();

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder
					.setGitDir(new File(repos.getAbsolutePath()+"\\" + Constants.DOT_GIT))
					.readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.build();

			System.out.println("GettingClosedCommit-"+repos.getName());
			List<Pair<RevCommit, Issue>> closedChildCommits = getClosedChildCommit(repository, repos.getName());//returnされたcommitは必ず親を持つ
			wtch.check("GetClosedChildCommit");

			System.out.println("End\nGettingGraftableLineList-"+repos.getName());
			List<Pair<RevCommit, List<Set<String>>>> graftableLineList = getGraftableLineList(closedChildCommits, repository);
			wtch.check("GetGraftableLineList");

			System.out.println("End\nCalcingGraftability-Step1-"+repos.getName());
			List<Pair<String, List<Double>>> grdentGrftblty =  calcGraftabliry(graftableLineList,repos.getName());
			wtch.check("CalcGraftability-Step1");

			System.out.println("End\nCalcingGraftability-Step2-"+repos.getName());
			Double grftblty = calcGrafability(grdentGrftblty);
			System.out.println("End");
			wtch.check("CalcGraftability-Step2");

			outputIssueType(closedChildCommits, resPath+repos.getName()+"\\"+"CommitAndIssueType.txt");
			wtch.check("outputIssueType");
			System.out.println("OutputIssueType");

			pw.println("graftability : " + grftblty);
			pw.println();
			pw.println(wtch);

		//	outputAddedLines(resPath+repos.getName()+"\\"+"addedLines.txt", graftableLineList);
		//	outputAddedLines(resPath+repos.getName()+"\\"+"graftableLines.txt", graftableLineList);
			System.out.println("graftability : "+ grftblty);
			pw.close();
		}

	}
	/**
	 *
	 * @throws Exception
	 */
	private static void expExtendedRange()throws Exception{
		Set<String> datasetSources = new HashSet<>();
		System.out.println("gettingucidataset");
		datasetSources = getRawSourceString(uciPath);
		allTime.check("getting_Dataset");
		for (File repos : reposArray) {
			PrintWriter pw = getPrintWriter(repos);

			Watch wtch = new Watch();

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder
					.setGitDir(new File(repos.getAbsolutePath()+"\\" + Constants.DOT_GIT))
					.readEnvironment() // scan environment GIT_* variables
					.findGitDir() // scan up the file system tree
					.build();

			System.out.println("GettingClosedCommit-"+repos.getName());
			List<Pair<RevCommit, Issue>> closedChildCommits = getClosedChildCommit(repository, repos.getName());//returnされたcommitは必ず親を持つ
			wtch.check("GetClosedChildCommit");

			System.out.println("End\nGettingGraftableLineList-"+repos.getName());
			List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList = getGraftableLineList134(closedChildCommits, repository,datasetSources);
			wtch.check("GetGraftableLineList");

			System.out.println("End\nCalcingGraftability-Step1-"+repos.getName());
			List<Pair<String, List<Double>>> grdentGrftblty =  calcGraftabliry13(graftableLineList,repos.getName());
			wtch.check("CalcGraftability-Step1");

			System.out.println("End\nCalcingGraftability-Step2-"+repos.getName());
			Double grftblty = calcGrafability(grdentGrftblty);
			System.out.println("End");
			wtch.check("CalcGraftability-Step2");
			printGraftableLinenNum(graftableLineList, repos.getName());
			outputIssueType(closedChildCommits, resPath+repos.getName()+"\\"+"CommitAndIssueType.txt");
			wtch.check("outputIssueType");
			System.out.println("OutputIssueType");

			pw.println("graftability : " + grftblty);
			pw.println();
			pw.println(wtch);

			outputAddedLines(resPath+repos.getName()+"\\"+"addedLines.txt", graftableLineList);
			outputGraftableLines134(resPath+repos.getName()+"\\"+"graftableLines.txt", graftableLineList);	//otherProjAddedLineはなし
			System.out.println("graftability : "+ grftblty);
			pw.close();
		}

	}
	private static void printGraftableLinenNum(List<Pair<RevCommit, List<Multiset<String>>>> lines ,String projName)throws Exception{
		PrintWriter pw = getPrintWriterString(resPath+projName+"\\"+"GraftableLines.csv");

		//pw.println("CommitID,CommitTime[y/m/d h:m:s],CommitSize[lines],Graftability");
		pw.println("CommitID,CommitTime[epoch second],CommitSize[lines],AddedLines,GraftableLines(Parental),GraftableLines(OtherProject),GraftableLines(ALL)");
		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			if(!pair.getRight().get(0).isEmpty()){//addedLinesが空でないなら計算		0...addedlines 1..Parental-GraftableLines	2..ancestoral 3..OtherProj
				pw.println(pair.getLeft().getId().toString()+","
							//+format.format(new Date((long)(pair.getLeft().getCommitTime()*1000)))+","
							+pair.getLeft().getCommitTime()+","
							+pair.getRight().get(0).size()+","
							+pair.getRight().get(1).size()+","
							+pair.getRight().get(3).size()+","
							+(pair.getRight().get(1).size()+ pair.getRight().get(3).size()));
			}
		}
		pw.close();
	}
	private static PrintWriter getPrintWriter(File repos) throws IOException {
		makedirParent(resPath+repos.getName()+"\\"+"Graftability.txt");
		File file = new File(resPath+ repos.getName()+"\\"+"Graftability.txt");
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	private static void outputGraftableLines(String filePath,List<Pair<RevCommit, List<Multiset<String>>>> lines )throws Exception{
		PrintWriter pw = getPrintWriterString(filePath);

		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			pw.println(pair.getLeft().name());
			pw.println("-----------Graftable from parent-------------");
			for (String line : pair.getRight().get(1)) {
				pw.println(line);
			}
			pw.println("-----------Graftable from dataset------------");
			for (String line : pair.getRight().get(3)) {
				pw.println(line);
			}
			pw.println();
		}
		pw.close();
	}

	private static void outputGraftableLines134(String filePath,List<Pair<RevCommit, List<Multiset<String>>>> lines )throws Exception{
		PrintWriter pw = getPrintWriterString(filePath);

		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			pw.println(pair.getLeft().name());
			pw.println("-----------Graftable from parent-------------");
			for (String line : pair.getRight().get(1)) {
				pw.println(line);
			}
			pw.println("-----------Graftable from dataset------------");
			for (String line : pair.getRight().get(3)) {
				pw.println(line);
			}
			pw.println("-----------UnGraftable Lines------------");
			for (String line : pair.getRight().get(4)) {
				pw.println(line);
			}

			pw.println();
		}
		pw.close();
	}

	/**
	 * AddedLineをしゅつりょくする
	 * @param filePath
	 * @param lines
	 * @throws Exception
	 */
	private static void outputAddedLines(String filePath,List<Pair<RevCommit, List<Multiset<String>>>> lines )throws Exception{
		PrintWriter pw = getPrintWriterString(filePath);

		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			pw.println(pair.getLeft().name());

			for (String line : pair.getRight().get(0)) {
				pw.println(line);
			}
			pw.println();
		}
		pw.close();
	}

	private static PrintWriter getPrintWriterString(String filePath)
			throws IOException {
		makedirParent(filePath);
		File file = new File(filePath);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	private static void outputGraftability(Map<String, int[]> resMap) {
		for (int[] res : resMap.values()) {
			if (res[0] != 0)
				System.out.println("graftability : " + res[1] / res[0]);
		}
	}

	private static void outputComments(Repository repo) throws Exception {
		RevCommit oldCommit = null;
		RevCommit newCommit = null;

		RevWalk rw = getInitializedRevWalk(repo, RevSort.REVERSE);

		oldCommit = rw.next();
		newCommit = rw.next();

		while (true) { // コミット　firstRevから最終Revまでループ

			if (oldCommit == null || newCommit == null)
				break;
			System.out.println("Start");
			System.out.println(oldCommit.getFullMessage());
			System.out.println("END\n");
			generalCounter++;
			oldCommit = newCommit;
			newCommit = rw.next();
		}

	}

	private static void outputCommentsToFile(Repository repo, String name)
			throws Exception {
		RevCommit oldCommit = null;
		RevCommit newCommit = null;

		File outCom = new File("comments-" + name + ".txt");
		FileWriter comWriter = new FileWriter(outCom);
		BufferedWriter combw = new BufferedWriter(comWriter);
		PrintWriter compw = new PrintWriter(combw);

		RevWalk rw = getInitializedRevWalk(repo, RevSort.REVERSE);

		oldCommit = rw.next();
		newCommit = rw.next();

		while (true) { // コミット　firstRevから最終Revまでループ

			if (oldCommit == null || newCommit == null)
				break;

			compw.println("Start");
			compw.println(oldCommit.getFullMessage());
			compw.println("END\n");

			oldCommit = newCommit;
			newCommit = rw.next();
		}
		compw.close();

	}
	private static void outputIssueType(List<Pair<RevCommit, Issue>> pairList,String filePath)throws IOException{
		//IssueType type=null;
		PrintWriter pw = getPrintWriterString(filePath);

		for (Pair<RevCommit, Issue> pair : pairList) {
			pw.println(pair.getLeft().getId().toString()+","+pair.getRight().getIssueType().toString());
		}



	}
	private static void outputCommentsToFileFormain() throws Exception {
		reposArray = reposFolder.listFiles(); // 絶対パスで帰ってくる

		for (File rep : reposArray) {
			if (rep.getAbsolutePath().indexOf("hadoop") != -1)
				continue;

			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			Repository repository = builder
					.setGitDir(
							new File(rep.getAbsolutePath() + "\\"
									+ Constants.DOT_GIT)).readEnvironment()
					.findGitDir().build();
			/*
			 * Repository repository = builder.setGitDir(new
			 * File(testPath+Constants.DOT_GIT)) .readEnvironment() // scan
			 * environment GIT_* variables .findGitDir() // scan up the file
			 * system tree .build();
			 */

			outputCommentsToFile(repository, rep.getName());
		}

	}

	private static Double calcGrafability(List<Pair<String, List<Double>>> graftabliries){
		List<Double> tmp = new ArrayList<>();
		Double sum= new Double(0);
		for (Pair<String, List<Double>> dbl : graftabliries) {
			tmp.add(dbl.getRight().get(0));
			sum=sum+dbl.getRight().get(0);
		}
		return sum/Double.valueOf((double)tmp.size());
	}

	//コミットごとに，Parental, Ancestoral, other projects,のgraftabilityガある．
	//NaNは出すな．
	//コミットごとのGraftability，ここで出力しちゃうよ．
	private static List<Pair<String, List<Double>>> calcGraftabliry(List<Pair<RevCommit, List<Set<String>>>> lines,String projName) throws Exception{
		List<Pair<String, List<Double>>> graftabilities = new ArrayList<>();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		PrintWriter pw = getPrintWriterString(resPath+projName+"\\"+"Graftabilities.csv");

		//pw.println("CommitID,CommitTime[y/m/d h:m:s],CommitSize[lines],Graftability");
		pw.println("CommitID,CommitTime[epoch second],CommitSize[lines],Graftability");
		for (Pair<RevCommit, List<Set<String>>> pair : lines) {
			if(!pair.getRight().get(0).isEmpty()){//addedLinesが空でないなら計算		0...addedlines 1..Parental-GraftableLines
				List<Double> grftbrty=setsToGraftability(pair.getRight());		//	0...parentalGraftability 1..ancestral
				pw.println(pair.getLeft().getId().toString()+","
							//+format.format(new Date((long)(pair.getLeft().getCommitTime()*1000)))+","
							+pair.getLeft().getCommitTime()+","
							+pair.getRight().get(0).size()+","
							+grftbrty.get(0));
				graftabilities.add(
					new MutablePair<String, List<Double>>(	pair.getLeft().getId().toString(),
															grftbrty	));
			}
		}
		pw.close();
		return graftabilities;
	}

	/**
	 * 各コミットのGraftabilityの計算，出力を行う．
	 * 各コミットのGraftabilityを返す．
	 * @param lines
	 * @param projName
	 * @return
	 * @throws Exception
	 */
	private static List<Pair<String, List<Double>>> calcGraftabliry13(List<Pair<RevCommit, List<Multiset<String>>>> lines,String projName) throws Exception{
		List<Pair<String, List<Double>>> graftabilities = new ArrayList<>();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		PrintWriter pw = getPrintWriterString(resPath+projName+"\\"+"Graftabilities.csv");

		//pw.println("CommitID,CommitTime[y/m/d h:m:s],CommitSize[lines],Graftability");
		pw.println("CommitID,CommitTime[epoch second],CommitSize[lines],Graftability(Parental),Graftability(OtherProject),Graftability(ALL)");
		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			if(!pair.getRight().get(0).isEmpty()){//addedLinesが空でないなら計算		0...addedlines 1..Parental-GraftableLines	2..ancestoral 3..OtherProj
				List<Double> grftbrty=setsToGraftability13(pair.getRight());		//	0...parentalGraftability 1..ancestral 2..ALL
				pw.println(pair.getLeft().getId().toString()+","
							//+format.format(new Date((long)(pair.getLeft().getCommitTime()*1000)))+","
							+pair.getLeft().getCommitTime()+","
							+pair.getRight().get(0).size()+","
							+grftbrty.get(0)+","
							+grftbrty.get(1)+","
							+grftbrty.get(2));
				graftabilities.add(
					new MutablePair<String, List<Double>>(	pair.getLeft().getId().toString(),
															grftbrty	));
			}
		}
		pw.close();
		return graftabilities;
	}


	/**
	 * 各コミットのGraftabilityの計算，出力を行う．
	 * 各コミットのGraftabilityを返す．
	 * @param lines
	 * @param projName
	 * @return
	 * @throws Exception
	 */
	private static List<Pair<String, List<Double>>> calcGraftabliry134(List<Pair<RevCommit, List<Multiset<String>>>> lines,String projName) throws Exception{
		List<Pair<String, List<Double>>> graftabilities = new ArrayList<>();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		PrintWriter pw = getPrintWriterString(resPath+projName+"\\"+"Graftabilities.csv");

		//pw.println("CommitID,CommitTime[y/m/d h:m:s],CommitSize[lines],Graftability");
		pw.println("CommitID,CommitTime[epoch second],CommitSize[lines],Graftability(Parental),Graftability(OtherProject),Graftability(ALL)");
		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			if(!pair.getRight().get(0).isEmpty()){//addedLinesが空でないなら計算		0...addedlines 1..Parental-GraftableLines	2..ancestoral 3..OtherProj
				List<Double> grftbrty=setsToGraftability13(pair.getRight());		//	0...parentalGraftability 1..ancestral 2..ALL
				pw.println(pair.getLeft().getId().toString()+","
							//+format.format(new Date((long)(pair.getLeft().getCommitTime()*1000)))+","
							+pair.getLeft().getCommitTime()+","
							+pair.getRight().get(0).size()+","
							+grftbrty.get(0)+","
							+grftbrty.get(1)+","
							+grftbrty.get(2));
				graftabilities.add(
					new MutablePair<String, List<Double>>(	pair.getLeft().getId().toString(),
															grftbrty	));
			}
		}
		pw.close();
		return graftabilities;
	}

	//return 0..parental 1..ancestral 2..otherProject
	private static List<Double> setsToGraftability(List<Set<String>> sets) {
		List<Double> res = new ArrayList<>();
		res.add(Double.valueOf((double) sets.get(1).size()) /
				Double.valueOf((double) sets.get(0).size()));

		return res;
	}

	/**
	 * 各コミットのGraftabilityを返す
	 * @param sets
	 * @return
	 */
	private static List<Double> setsToGraftability13(List<Multiset<String>> sets) {
		List<Double> res = new ArrayList<>();
		res.add(Double.valueOf((double) sets.get(1).size()) /
				Double.valueOf((double) sets.get(0).size()));
		res.add(Double.valueOf((double) sets.get(3).size()) /
				Double.valueOf((double) sets.get(0).size()));
		res.add(Double.valueOf((double)( sets.get(1).size()) +sets.get(3).size())/
				Double.valueOf((double) sets.get(0).size()));
		return res;
	}


	/**
	 * ParentalGraftabilityのみ求めるならこっち
	 * List<Set<String>> 0..addedline 1..graftableLine(Parent)
	 * ここで得られたコミットは，ClosedかつFixedで，Modified,またはaddedな変更があったコミット．
	 * @param list
	 * @param repo
	 * @return List<Pair<RevCommit, List<Set<String>>>>
	 * @throws Exception
	 */
	@Deprecated
	private static List<Pair<RevCommit, List<Set<String>>>> getGraftableLineList(
			List<Pair<RevCommit, Issue>> list, Repository repo)
			throws Exception {
		List<Pair<RevCommit, List<Set<String>>>> graftableLineList
											= new ArrayList<Pair<RevCommit, List<Set<String>>>>();
		for (Pair<RevCommit, Issue> pair : list) {

			//Set<String> addedLines = getAddedLines(pair.getLeft(), repo);

		//	if(addedLines.isEmpty())continue; //追加された行がなければ処理しない

		//	Set<String> graftableLines = getGraftableLines(pair.getLeft(),
		//			addedLines, repo); // parentからgraftableか調べる


			List<Set<String>> tmpList = new ArrayList<Set<String>>();
		//	tmpList.add(addedLines);
		//	tmpList.add(graftableLines);
			graftableLineList
					.add(new MutablePair<RevCommit, List<Set<String>>>(pair
							.getLeft(), new ArrayList<Set<String>>(tmpList)));

			System.out.println("commit solved");
		}
		return graftableLineList;
	}
	/**
	 * ParentalGraftability と OtherProjGraftabilityを求めるならこっち
	 * 0..addedlines 1..parentalyGraftable 2..otherProjectalyGraftable
	 * @param list
	 * @param repo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static List<Pair<RevCommit, List<Multiset<String>>>> getGraftableLineList13(
			List<Pair<RevCommit, Issue>> list, Repository repo,Set<String> otherProjectSources)
			throws Exception {
		List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList
											= new ArrayList<Pair<RevCommit, List<Multiset<String>>>>();
		int counterCom=0;
		for (Pair<RevCommit, Issue> pair : list) {
			List<Multiset<String>> lineSetTmp=new ArrayList<>();
			for (int i = 0; i < 4; i++) {
				lineSetTmp.add(LinkedHashMultiset.create());
			}

			Multiset<String> addedLines = getAddedLines(pair.getLeft(), repo);//removeBracket=trueなら{};もはずす

			if(addedLines.isEmpty())continue; //追加された行がなければ処理しない

			lineSetTmp.get(0).addAll(addedLines);
			lineSetTmp = getGraftableLines13(pair.getLeft(), addedLines, repo, otherProjectSources, lineSetTmp);
			graftableLineList.add(new MutablePair<RevCommit, List<Multiset<String>>>(pair.getLeft(),lineSetTmp));

			counterCom++;
			if(analyze100Commit && counterCom==100 )break;
			System.out.println("commit solved");
		}
		return graftableLineList;
	}
	/**
	 * ParentalGraftability と OtherProjGraftabilityを求めるならこっち
	 * 0..addedlines 1..parentalyGraftable 2..otherProjectalyGraftable
	 * @param list
	 * @param repo
	 * @return
	 * @throws Exception
	 */
	private static List<Pair<RevCommit, List<Multiset<String>>>> getGraftableLineList134(
			List<Pair<RevCommit, Issue>> list, Repository repo,Set<String> otherProjectSources)
			throws Exception {
		List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList
											= new ArrayList<Pair<RevCommit, List<Multiset<String>>>>();
		int counterCom=0;
		for (Pair<RevCommit, Issue> pair : list) {
			List<Multiset<String>> lineSetTmp=new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				lineSetTmp.add(LinkedHashMultiset.create());
			}

			Multiset<String> addedLines = getAddedLines(pair.getLeft(), repo);//removeBracket=trueなら{};もはずす

			if(addedLines.isEmpty())continue; //追加された行がなければ処理しない

			lineSetTmp.get(0).addAll(addedLines);
			lineSetTmp = getGraftableLines134(pair.getLeft(), addedLines, repo, otherProjectSources, lineSetTmp);
			graftableLineList.add(new MutablePair<RevCommit, List<Multiset<String>>>(pair.getLeft(),lineSetTmp));

			counterCom++;
			if(analyze100Commit && counterCom==100 )break;
			System.out.println("commit solved");
		}
		return graftableLineList;
	}
	/**
	 * コミットにより追加された行の集合を返す
	 * 追加がなければ空集合が帰る
	 * @param commit
	 * @param repo
	 * @return
	 * @throws Exception
	 */
	private static Multiset<String> getAddedLines(RevCommit commit, Repository repo)
			throws Exception {
		Multiset<String> addedLines = LinkedHashMultiset.create();
		RevCommit comParent = commit.getParent(0);

		if (comParent.getId().toString().equals(commit.getId().toString()))
			System.out.println("getParent method is invalid\n");

		addedLines = getAddedLineSet(comParent, commit, repo);

		return addedLines;
	}
	/*
	@Deprecated
	private static Set<String> getGraftableLines(RevCommit commit,
			Set<String> addedlines, Repository repo) throws Exception {
		Set<String> graftableLines = new HashSet<String>();
		Set<String> parentLines = getNormalizedSourceLines(commit.getParent(0), repo);
		for (String str : addedlines) {
			if (parentLines.contains(str)) {
				graftableLines.add(str);
			}
		}
		return graftableLines;

	}*/

	/**
	 * Listには 0..addedLines 1..ParentalGraftableLines
	 * 2.. AncestoralGraftableLines 3..OtherProjGraftableLines を格納したい．
	 * このメソッドでは，1と3を探し出して格納する
	 * @param commit
	 * @param addedlines
	 * @param repo
	 * @param otherProjectSources
	 * @return list of source Lines
	 * @throws Exception
	 */
	private static List<Multiset<String>> getGraftableLines13(RevCommit commit,
			Multiset<String> addedlines, Repository repo,Set<String> otherProjectSources,List<Multiset<String>> graftableLineList) throws Exception {

		Multiset<String> parentLines = getNormalizedSourceLines(commit.getParent(0), repo);
		for (String str : addedlines) {
			if (parentLines.contains(str)) {
				graftableLineList.get(1).add(str);
			}else if(otherProjectSources.contains(str)){
				graftableLineList.get(3).add(str);
			}
		}
		return graftableLineList;
	}

	/**
	 * Listには 0..addedLines 1..ParentalGraftableLines
	 * 2.. AncestoralGraftableLines 3..OtherProjGraftableLines 4..UnGraftableLinesを格納したい．
	 * このメソッドでは，1と3と4を探し出して格納する
	 * @param commit
	 * @param addedlines
	 * @param repo
	 * @param otherProjectSources
	 * @return list of source Lines
	 * @throws Exception
	 */
	private static List<Multiset<String>> getGraftableLines134(RevCommit commit,
			Multiset<String> addedlines, Repository repo,Set<String> otherProjectSources,List<Multiset<String>> graftableLineList) throws Exception {

		Multiset<String> parentLines = getNormalizedSourceLines(commit.getParent(0), repo);
		for (String str : addedlines) {
			if (parentLines.contains(str)) {
				graftableLineList.get(1).add(str);
			}else if(otherProjectSources.contains(str)){
				graftableLineList.get(3).add(str);
			}else{
				graftableLineList.get(4).add(str);
			}
		}
		return graftableLineList;
	}

	/**
	 * 子を持ち，closedかつFixedなCommitを返します．
	 * @param repo
	 * @param projectName
	 * @return
	 * @throws Exception
	 */
	private static List<Pair<RevCommit, Issue>> getClosedChildCommit(
			Repository repo, String projectName) throws Exception {
		List<RevCommit> comList = getCommits(repo, RevSort.REVERSE);	//一瞬
		List<RevCommit> childComList = getChildCommits(comList);
		List<Iterable<Issue>> issueList = getIssueList(projectName);	//1473で数分
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
	//Jira Apacheの検索結果と同じのが全部取得できたで
	private static List<Iterable<Issue>> getIssueList(String project)
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
									+ project
									+ " AND status = Closed AND resolution = Fixed AND issuetype = Bug",
							50, 0, search);
			//while( ! searchJqlPromise.isDone() ) { }//終わるまでまつ
			//searchJqlPromise.wait(5000);
			//searchJqlPromise.
			int resTotal = searchJqlPromise.claim().getTotal();
			int pageItrt = 0;
			if (resTotal != 0) {
				while (pageItrt <= resTotal) { // ページイテレータがAPIから見つからなかったから自分でめくる
					searchJqlPromise = restClient
							.getSearchClient()
							.searchJql(
									"project = "
											+ project
											+ " AND status = Closed AND resolution = Fixed AND issuetype = Bug",
									50, pageItrt, search);
			//		while( ! searchJqlPromise.isDone() ) { }//終わるまでまつ
			//		searchJqlPromise.;
					issueList.add(searchJqlPromise.claim().getIssues());
					pageItrt += 50;

					System.out.println("Waiting for 5 seconds.");
					Thread.sleep(5000);
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

	private static List<RevCommit> getCommits(Repository repo,
			RevSort direction) throws Exception {
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
	private static List<RevCommit> getChildCommits(List<RevCommit> commits) throws Exception {
		List<RevCommit> childCommits = new ArrayList<>();
		for (RevCommit commit : commits) {
			if(commit.getParentCount()!=0){	//親をもてば
				childCommits.add(commit);
			}
		}

		return childCommits;
	}

	// Reverseで最古から最新へ
	private static RevWalk getInitializedRevWalk(Repository repo,
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

	// projectは大文字小文字なんでもいいよ．返すのは大文字プロジェクト名-xxx
	// return null if issueKey isn't found
	//projectは小文字で与えられることを想定してる
	//感想：openjpa，svnidのとこで必ず含まれるなあ・・計算量は増えるかも，ミスはあまりないと思う，いや，あるわopenjpa-projectとか書かれてるから
	//完璧にとるには，openjpa-数字のものが1個か？ってするべきかなあ→regex
	/*
	private static String getIssueKey(String message, String project) {
		// return IssueKey if CommitMessage contains only one IssueKey
		String key = null;
		String lowMessage=message.toLowerCase(Locale.US);
		int numMatch = countStringInString(lowMessage , project + "-");

		if (numMatch == 0 || numMatch > 1) {
			key = null;
		} else {
			int index = lowMessage.indexOf(project+"-")+project.length()+1;
			System.out.println(index+" : indexof→"+lowMessage.indexOf(project+"-")+"project length→"+project.length());
			if(isNumber(message.substring(index, index+1))){//ここで最初の文字が数字であることは確定
				//key = "";
				int itrt = 1;
				while (isNumber(message.substring(index, index + itrt+1))) {		//次の文字が数字なら
					itrt++;														//増やす
				}
				key = message.substring(index, index +itrt);
			}
		}

		return key;
	}*/

	/**
	 * コミットコメントからprojectのキーを捜し，
	 * 大文字でキーかえすよ
	 * @param target
	 * @param project
	 * @return key (UpperCase)
	 */
	private static String getIssueKey(String target,String project){
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
	/**
	 * コミットコメントからRegexの文字列をさがし，
	 * コメント内に1つ含まれていたらキーを返す
	 *
	 * @param target
	 * @param regex
	 * @return
	 */
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

	/**
	 * Commitのすべてのコメントを除去したjavaファイルから行の集合を得る
	 * @param commit
	 * @param repo
	 * @return
	 * @throws Exception
	 */
	private static Multiset<String> getNormalizedSourceLines(RevCommit commit, Repository repo)
			throws Exception {
		// System.out.println("StartAddHSet");
		RevTree rt = commit.getTree();
		TreeWalk tw = new TreeWalk(repo);

		tw.addTree(rt);
		tw.setRecursive(true);

		Multiset<String> sourceLines = HashMultiset.create();

		while (tw.next()) { // commitのすべてのファイルについて

			// pathList.add(tw.getPathString().toString());
			// System.out.println(tw.getPathString().toString()+"を追加");
			// System.out.println("walk "+ tw.getPathString());
			if (tw.getPathString().toString().endsWith(".java")) {

				ObjectReader reader = repo.newObjectReader();
				// System.out.println("     reading...");
				byte[] data = reader.open(tw.getObjectId(0)).getBytes();
				// System.out.println("     parsing...");
				//String normalizedSourceFile = normalizeSource(new String(data,"utf-8"));
				String normalizedSourceFile = normalizeSource(CommentRemover.deleteETC(new String(data,"utf-8")));
				//if(removeBrackets)sourceFile=CommentRemover.removeDelimiter(sourceFile);
				normalizedSourceFile=CommentRemover.removeDelimiter(normalizedSourceFile);
				// System.out.println("     loading to hashset...");
				for(String line :  Arrays.asList(normalizedSourceFile.split("\n"))){
					sourceLines.add(line.replaceAll("\n|\r", ""));
				}
			}
		}

		// System.out.println("EndAddHSet\n");
		return sourceLines;

	}
	/**
	 * 与えられたフォルダ内のすべてのjavaファイルの行の集合を取得する
	 * コメントの除去は行わない．
	 * @return
	 */
	private static Set<String> getRawSourceString(String folderPath)throws Exception{

		Set<String> sourceLines = new HashSet<String>(70000000);
		File sourceFolder= new File(folderPath);
		File folders[]= sourceFolder.listFiles();
		for (File folder : folders) {
			System.out.println("Processing "+folder.getPath());
			List<File> sourceList = new ArrayList<>();
			makeFileList(folder,sourceList);
			for (File source : sourceList) {
				System.out.println("   process "+source.getPath());
				for (String line : fileToString(source).split("\n")) {
					sourceLines.add(CommentRemover.removeDelimiter(line).replaceAll("\n|\r", ""));
				}
			}
			sourceList=null;
		}
		return sourceLines;
	}


	/**
	 * 与えられた2つのコミットから追加された行を正規化したものの集合を返す
	 * 追加がなければ空集合が帰る
	 * @param oldrCommit
	 * @param newrCommit
	 * @param repo
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	private static Multiset<String> getAddedLineSet(final RevCommit oldrCommit,
			final RevCommit newrCommit, final Repository repo)
			throws Exception {
		// String lines = null;
		Multiset<String> lineList = LinkedHashMultiset.create();
		String oldsourceString;
		String newsourceString;

		RevCommit oldCommit = oldrCommit;
		RevCommit newCommit = newrCommit;

		AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, oldCommit
				.getId().getName().toString());
		AbstractTreeIterator newTreeParser = prepareTreeParser(repo, newCommit
				.getId().getName().toString());

		// then the procelain diff-command returns a list of diff entries
		List<DiffEntry> diff = new Git(repo).diff().setOldTree(oldTreeParser)
				.setNewTree(newTreeParser)
				.setPathFilter(PathSuffixFilter.create(".java")). // java以外のときは跡で考えよう
				call();
		for (DiffEntry entry : diff) { // diffsから詳細を読み込んで変更行を取り出し，書き込み
										// DiffEntry:どのファイルで，どんな種類の変更があったかの情報を保持する．
										// ChangeType:ファイルにどのような変更があったか．Deleteはファイルがけされたということ．
			ObjectLoader olold; // repo.openで初期化されるのでこのままで．
			ObjectLoader olnew;

			ByteArrayOutputStream bosold = new ByteArrayOutputStream();
			ByteArrayOutputStream bosnew = new ByteArrayOutputStream();

			if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY
					|| entry.getChangeType() == DiffEntry.ChangeType.ADD) { // ほしい情報はADDとMODIFYのみ

				if ((entry.getOldId().toObjectId().equals(ObjectId.zeroId())) == false) { // OLDが存在するか
					olold = repo.open(entry.getOldId().toObjectId()); // ソースを読み込んで，コメントなどを消去
					olold.copyTo(bosold);
					oldsourceString = bosold.toString();
					// oldsourceString = Arrays.toString(olold.getBytes());
					oldsourceString = CommentRemover.deleteETC(oldsourceString);
					if(removeBrackets)oldsourceString=CommentRemover.removeDelimiter(oldsourceString);

				} else {
					oldsourceString = "";
				}

				if (entry.getNewId().toObjectId().equals(ObjectId.zeroId()) == false) { // NEWが存在するか
					olnew = repo.open(entry.getNewId().toObjectId()); // ソースを読み込んで，コメントなどを消去
					olnew.copyTo(bosnew);
					newsourceString = bosnew.toString();
					// newsourceString = Arrays.toString(olnew.getBytes());
					newsourceString = CommentRemover.deleteETC(newsourceString);
					if(removeBrackets)newsourceString=CommentRemover.removeDelimiter(newsourceString);
					newsourceString.split("\n");
				} else {
					newsourceString = "";
				}


				//--------------コメントを取り除いたものをDiff

				List<Delta> dltList = DiffUtils.diff(
						Arrays.asList(oldsourceString.split("\n")),
						Arrays.asList(newsourceString.split("\n"))).getDeltas();
				List<Pair<Integer, Integer>> pairlist = new ArrayList<>();
				for (Delta dlt : dltList) {
					// diffResString= dlt.getRevised().getLines().toString();
					if (dlt.getType() == TYPE.INSERT
							|| dlt.getType() == TYPE.CHANGE) { // 追加，または修正されたファイルのうち，挿入または変更された行について処理をおこなう
						final Chunk chunk = dlt.getRevised();
						pairlist.add(new MutablePair<Integer,Integer>(Integer.valueOf(chunk.getPosition()+1), Integer.valueOf(chunk.last()+1)));	//全ての変更について，行番号の範囲を得る．
						/*
						for (String lineString : (List<String>) chunk
								.getLines()) {
							lineList.add(lineString);
						}*/
					}
				}

				String normedSource = normalizeSource(CommentRemover.deleteETC(bosnew.toString())); //正規化：ComRmして、変数を正規化して、CompirationUnit.toStringを使わずに出力

				LineNumberReader lineNumReader = new LineNumberReader(new StringReader(CommentRemover.deleteETC(normedSource)));	//正規化したものからコメントを取り除いてよみとり
				String lineString=null;
				while((lineString=lineNumReader.readLine())!=null){
		            int linenum = lineNumReader.getLineNumber();		//これ
		            for (Pair<Integer, Integer> pair : pairlist) {
						if((pair.getLeft().intValue() <= linenum) && (linenum <= pair.getRight().intValue() )){
							lineList.add(CommentRemover.removeDelimiter(lineString).replaceAll("\n|\r", ""));
							break;
						}
					}

		         }
			}

		}
		return lineList;
	}

	/**
	 * ソースコードを正規化したものを返す．
	 * @param source
	 * @return
	 */
	public static String normalizeSource(String source)throws Exception{
		//-------------正規化----------------------
		Document doc = new Document(source);

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);	//ここ新しい
		CompilationUnit unit = (CompilationUnit) parser
				.createAST(new org.eclipse.core.runtime.NullProgressMonitor());
		SourceVisitor visitor = new SourceVisitor(unit);
		visitor.setNormalizeOptions(true, false,
				false, false, false, false, false);
		unit.recordModifications();
		unit.accept(visitor);

		try {
			TextEdit edit = unit.rewrite(doc, null);
			edit.apply(doc);

		}catch(Exception e){
			System.out.println(e);
		}

		return doc.get();
	}
	public static boolean isMatch(String data, String ptn) {
		Pattern pattern = Pattern.compile(ptn);
		Matcher matcher = pattern.matcher(data);
		return matcher.matches();
	}

	public static int countStringInString(String target, String searchWord) {
		return (target.length() - target.replaceAll(searchWord, "").length())
				/ searchWord.length();
	}
	/**
	 * ファイルの親ディレクトリを作成する.
	 * @param filePath
	 */
	public static void makedirParent(String filePath){
	    File file = new File(filePath);
	    makedir(file.getParent());
	}
	/**
	 * ディレクトリを作成する.
	 * @param dirPath
	 */
	public static void makedir(String dirPath){
	    File dir = new File(dirPath);
	    if(!dir.exists()){
	        dir.mkdirs();
	    }
	}
	/**
	   * ディレクトリを再帰的に読んでfileListに追加
	   * @param folderPath
	   */
	  private static void makeFileList( File dir,List<File> fileList ) {

	    File[] files = dir.listFiles();
	    if( files == null )
	      return;
	    for( File file : files ) {
	      if( !file.exists() )
	        continue;
	      else if( file.isDirectory() )
	        makeFileList( file,fileList );
	      else if( file.isFile() )
	        fileList.add( file );
	    }
	  }
	// ファイル内容をを文字列化するメソッドです。
		  public static String fileToString(File file) throws IOException {
		    BufferedReader br = null;
		    try {
		      // ファイルを読み込むバッファドリーダを作成します。
		      br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		      // 読み込んだ文字列を保持するストリングバッファを用意します。
		      StringBuffer sb = new StringBuffer();
		      // ファイルから読み込んだ一文字を保存する変数です。
		      int c;
		      // ファイルから１文字ずつ読み込み、バッファへ追加します。
		      while ((c = br.read()) != -1) {
		        sb.append((char) c);
		      }
		      // バッファの内容を文字列化して返します。
		      return sb.toString();
		    } catch(Exception e){
		    	System.out.println(e);
		    	return null;
		    } finally{
		      // リーダを閉じます。
		      br.close();
		    }
		  }
}
