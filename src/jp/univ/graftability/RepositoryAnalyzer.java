package jp.univ.graftability;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jp.univ.utils.FileUtils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import analyzeGit.Watch;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.Multiset;

/**
 * @author s-sumi 1つのリポジトリを解析する ・バグ修正コミットの特定 ・コミットで追加されたソースコード行の特定
 *         ・親リビジョンまたはデータセット中にソースコード行が現れるか調べる
 */
public class RepositoryAnalyzer {
	private final File repos;
	private final String outputPath;
	private Repository repository;
	private final Set<String> dataSet;
	private final JiraIssueDumper jIssueDumper; // そのリポジトリに関係したissueを取得する
	private final CommitRetriever cRetriever;
	private final GraftableLineExtracter gLineExtracter;

	public RepositoryAnalyzer(File rep, Set<String> dSet,
			String output) throws Exception {
		this.outputPath = output;
		this.repos = rep;
		this.dataSet = dSet;
		setRepository();
		String projectName = rep.getName();
		this.jIssueDumper = new JiraIssueDumper(projectName);
		this.cRetriever = new CommitRetriever(repository,
				jIssueDumper.getIssueList(), projectName);
		this.gLineExtracter = new GraftableLineExtracter(repository, dataSet,
				cRetriever.retrieveCommits());
	}

	public void execute() throws Exception {
		PrintWriter pw = getPrintWriter(repos);

		Watch wtch = new Watch();

		System.out.println("GettingClosedCommit-" + repos.getName());
		List<Pair<RevCommit, Issue>> closedChildCommits = cRetriever
				.retrieveCommits();// returnされたcommitは必ず親を持つ
		wtch.check("GetClosedChildCommit");

		System.out.println("End\nGettingGraftableLineList-" + repos.getName());
		List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList = gLineExtracter
				.getGraftableLineList134();
		wtch.check("GetGraftableLineList");

		System.out.println("End\nCalcingGraftability-Step1-" + repos.getName());
		List<Pair<String, List<Double>>> grdentGrftblty = calcGraftabliry13(
				graftableLineList, repos.getName());
		wtch.check("CalcGraftability-Step1");

		System.out.println("End\nCalcingGraftability-Step2-" + repos.getName());
		Double grftblty = calcGrafability(grdentGrftblty);
		System.out.println("End");
		wtch.check("CalcGraftability-Step2");
		printGraftableLinenNum(graftableLineList, repos.getName());
		outputIssueType(closedChildCommits, outputPath + repos.getName() + "\\"
				+ "CommitAndIssueType.txt");
		wtch.check("outputIssueType");
		System.out.println("OutputIssueType");

		pw.println("graftability : " + grftblty);
		pw.println();
		pw.println(wtch);

		outputAddedLines(outputPath + repos.getName() + "\\" + "addedLines.txt",
				graftableLineList);
		outputGraftableLines134(outputPath + repos.getName() + "\\"
				+ "graftableLines.txt", graftableLineList); // otherProjAddedLineはなし
		System.out.println("graftability : " + grftblty);
		pw.close();
	}
	private void printGraftableLinenNum(List<Pair<RevCommit, List<Multiset<String>>>> lines ,String projName)throws Exception{
		PrintWriter pw = getPrintWriterString(outputPath+projName+"\\"+"GraftableLines.csv");

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
	/**
	 * AddedLineをしゅつりょくする
	 * @param filePath
	 * @param lines
	 * @throws Exception
	 */
	private void outputAddedLines(String filePath,List<Pair<RevCommit, List<Multiset<String>>>> lines )throws Exception{
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
	private Double calcGrafability(List<Pair<String, List<Double>>> graftabliries){
		List<Double> tmp = new ArrayList<>();
		Double sum= new Double(0);
		for (Pair<String, List<Double>> dbl : graftabliries) {
			tmp.add(dbl.getRight().get(0));
			sum=sum+dbl.getRight().get(0);
		}
		return sum/Double.valueOf((double)tmp.size());
	}
	/**
	 * 各コミットのGraftabilityの計算，出力を行う． 各コミットのGraftabilityを返す．
	 *
	 * @param lines
	 * @param projName
	 * @return
	 * @throws Exception
	 */
	private List<Pair<String, List<Double>>> calcGraftabliry13(
			List<Pair<RevCommit, List<Multiset<String>>>> lines, String projName)
			throws Exception {
		List<Pair<String, List<Double>>> graftabilities = new ArrayList<>();
		// SimpleDateFormat format = new
		// SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		PrintWriter pw = getPrintWriterString(outputPath + projName + "\\"
				+ "Graftabilities.csv");

		// pw.println("CommitID,CommitTime[y/m/d h:m:s],CommitSize[lines],Graftability");
		pw.println("CommitID,CommitTime[epoch second],CommitSize[lines],Graftability(Parental),Graftability(OtherProject),Graftability(ALL)");
		for (Pair<RevCommit, List<Multiset<String>>> pair : lines) {
			if (!pair.getRight().get(0).isEmpty()) {// addedLinesが空でないなら計算
													// 0...addedlines
													// 1..Parental-GraftableLines
													// 2..ancestoral
													// 3..OtherProj
				List<Double> grftbrty = setsToGraftability13(pair.getRight()); // 0...parentalGraftability
																				// 1..ancestral
																				// 2..ALL
				pw.println(pair.getLeft().getId().toString()
						+ ","
						// +format.format(new
						// Date((long)(pair.getLeft().getCommitTime()*1000)))+","
						+ pair.getLeft().getCommitTime() + ","
						+ pair.getRight().get(0).size() + "," + grftbrty.get(0)
						+ "," + grftbrty.get(1) + "," + grftbrty.get(2));
				graftabilities.add(new MutablePair<String, List<Double>>(pair
						.getLeft().getId().toString(), grftbrty));
			}
		}
		pw.close();
		return graftabilities;
	}
	private void outputGraftableLines134(String filePath,List<Pair<RevCommit, List<Multiset<String>>>> lines )throws Exception{
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
	 * 各コミットのGraftabilityを返す
	 * @param sets
	 * @return
	 */
	private List<Double> setsToGraftability13(List<Multiset<String>> sets) {
		List<Double> res = new ArrayList<>();
		res.add(Double.valueOf((double) sets.get(1).size()) /
				Double.valueOf((double) sets.get(0).size()));
		res.add(Double.valueOf((double) sets.get(3).size()) /
				Double.valueOf((double) sets.get(0).size()));
		res.add(Double.valueOf((double)( sets.get(1).size()) +sets.get(3).size())/
				Double.valueOf((double) sets.get(0).size()));
		return res;
	}
	private void outputIssueType(List<Pair<RevCommit, Issue>> pairList,String filePath)throws IOException{
		//IssueType type=null;
		PrintWriter pw = getPrintWriterString(filePath);

		for (Pair<RevCommit, Issue> pair : pairList) {
			pw.println(pair.getLeft().getId().toString()+","+pair.getRight().getIssueType().toString());
		}



	}

	private PrintWriter getPrintWriter(File repos) throws IOException {
		FileUtils.makedir(outputPath + repos.getName() + "\\"
				+ "Graftability.txt");
		File file = new File(outputPath + repos.getName() + "\\"
				+ "Graftability.txt");
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}
	private PrintWriter getPrintWriterString(String filePath)
			throws IOException {
		FileUtils.makedirParent(filePath);
		File file = new File(filePath);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}


	/**
	 * Reposさえ設定しておけば、後はexecuteの中でRepositoryを設定する
	 *
	 * @throws IOException
	 */
	public void setRepository() throws IOException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository tmp = builder
				.setGitDir(
						new File(repos.getAbsolutePath() + "\\"
								+ Constants.DOT_GIT)).readEnvironment() // scan
																		// environment
																		// GIT_*
																		// variables
				.findGitDir() // scan up the file system tree
				.build();
		this.repository = tmp;
	}

}
