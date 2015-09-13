package jp.univ.graftability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.text.edits.TextEdit;

import analyzeGit.CommentRemover;
import analyzeGit.SourceVisitor;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

public class GraftableLineExtracter {
	private Repository repo;
	private Set<String> dataSet;
	private List<Pair<RevCommit, Issue>> analysisTargets;
	final private int ADDED_LINES = 0;
	final private int PARENT_GRAFTABLE = 1;
	final private int ANCESTOR_GRAFTABLE = 2;
	final private int DATASET_GRAFTABLE = 3;
	final private int UNGRAFTABLE = 4;

	public GraftableLineExtracter(Repository repo, Set<String> dataSet,
			List<Pair<RevCommit, Issue>> analysisTargets) {
		super();
		this.repo = repo;
		this.dataSet = dataSet;
		this.analysisTargets = analysisTargets;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public List<Pair<RevCommit, List<Multiset<String>>>> getGraftableLineList134()
			throws Exception {
		List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList = new ArrayList<Pair<RevCommit, List<Multiset<String>>>>();
		for (Pair<RevCommit, Issue> pair : analysisTargets) {
			ChangeAnalyzer cAnalyzer = new ChangeAnalyzer(pair.getLeft(), repo);

			List<Multiset<String>> lineSetTmp = new ArrayList<>();

			for (int i = 0; i < 5; i++) {
				lineSetTmp.add(LinkedHashMultiset.create());
			}

			Multiset<String> addedLines = cAnalyzer.getAddedLineSet();

			if (addedLines.isEmpty())
				continue; // 追加された行がなければ処理しない

			lineSetTmp.get(0).addAll(addedLines);
			lineSetTmp = getGraftableLines134(pair.getLeft(),lineSetTmp);
			graftableLineList
					.add(new MutablePair<RevCommit, List<Multiset<String>>>(
							pair.getLeft(), lineSetTmp));

			System.out.println("commit solved");
		}
		return graftableLineList;
	}

	/**
	 * @param graftableLineList
	 *            (addedLinesを除いて空)
	 * @return graftableLineList
	 * @throws Exception
	 */
	private List<Multiset<String>> getGraftableLines134(RevCommit commit,
			List<Multiset<String>> graftableLineList) throws Exception {

		Multiset<String> parentLines = getNormalizedSourceLines(
				commit.getParent(0), repo);
		for (String str : graftableLineList.get(0)) {
			if (parentLines.contains(str)) {
				graftableLineList.get(1).add(str);
			} else if (dataSet.contains(str)) {
				graftableLineList.get(3).add(str);
			} else {
				graftableLineList.get(4).add(str);
			}
		}
		return graftableLineList;
	}

	/**
	 * Commitのすべてのコメントを除去したjavaファイルから行の集合を得る
	 *
	 * @param commit
	 * @param repo
	 * @return
	 * @throws Exception
	 */
	private Multiset<String> getNormalizedSourceLines(RevCommit commit,
			Repository repo) throws Exception {
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
				// String normalizedSourceFile = normalizeSource(new
				// String(data,"utf-8"));
				String normalizedSourceFile = normalizeSource(CommentRemover
						.deleteETC(new String(data, "utf-8")));
				// if(removeBrackets)sourceFile=CommentRemover.removeDelimiter(sourceFile);
				normalizedSourceFile = CommentRemover
						.removeDelimiter(normalizedSourceFile);
				// System.out.println("     loading to hashset...");
				for (String line : Arrays.asList(normalizedSourceFile
						.split("\n"))) {
					sourceLines.add(line.replaceAll("\n|\r", ""));
				}
			}
		}

		// System.out.println("EndAddHSet\n");
		return sourceLines;

	}
	/**
	 * ソースコードのASTを構築して、ASTVisitorを起動する
	 * @param source
	 * @return
	 * @throws Exception
	 */
	private String normalizeSource(String source) throws Exception {
		// -------------正規化----------------------
		Document doc = new Document(source);

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT); // ここ新しい
		CompilationUnit unit = (CompilationUnit) parser
				.createAST(new org.eclipse.core.runtime.NullProgressMonitor());
		SourceVisitor visitor = new SourceVisitor(unit);
		visitor.setNormalizeOptions(true, false, false, false, false, false,
				false);
		unit.recordModifications();
		unit.accept(visitor);

		try {
			TextEdit edit = unit.rewrite(doc, null);
			edit.apply(doc);

		} catch (Exception e) {
			System.out.println(e);
		}

		return doc.get();
	}
}
