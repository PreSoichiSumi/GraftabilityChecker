package jp.univ.graftability;

import java.util.ArrayList;
import java.util.List;

import jp.univ.utils.CommentRemover;
import jp.univ.utils.SourceVisitor;

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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

public class GraftableLineExtracter {
	private Repository repo;
	private List<RevCommit> analysisTargets;
	private String dataSetDBPath;
	private DBController dataSetDBController;
	final private int ADDED_LINES = 0;
	final private int PARENT_GRAFTABLE = 1;
	final private int ANCESTOR_GRAFTABLE = 2;
	final private int DATASET_GRAFTABLE = 3;
	final private int UNGRAFTABLE = 4;


	public GraftableLineExtracter(Repository repo, String datasetDBPath,
			List<RevCommit> analysisTargets) {
		super();
		this.repo = repo;
		this.dataSetDBPath = datasetDBPath;
		this.analysisTargets = analysisTargets;
		this.dataSetDBController = new DBController(datasetDBPath);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public List<Pair<RevCommit, List<Multiset<String>>>> getGraftableLineList134()
			throws Exception {
		dataSetDBController.prepareForCheckingDataSet();
		List<Pair<RevCommit, List<Multiset<String>>>> graftableLineList = new ArrayList<Pair<RevCommit, List<Multiset<String>>>>();
		for (RevCommit com : analysisTargets) {
			ChangeAnalyzer cAnalyzer = new ChangeAnalyzer(com, repo);

			List<Multiset<String>> lineSetTmp = new ArrayList<>();

			for (int i = 0; i < 5; i++) {
				lineSetTmp.add(LinkedHashMultiset.create());
			}

			Multiset<String> addedLines = cAnalyzer.getAddedLineSet();

			if (addedLines.isEmpty())
				continue; // 追加された行がなければ処理しない

			lineSetTmp.get(0).addAll(addedLines);
			lineSetTmp = getGraftableLines134(com, lineSetTmp);
			graftableLineList
					.add(new MutablePair<RevCommit, List<Multiset<String>>>(
							com, lineSetTmp));

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
				commit.getParent(ADDED_LINES), repo);
		for (String str : graftableLineList.get(ADDED_LINES)) {
			if (parentLines.contains(str)) {
				graftableLineList.get(PARENT_GRAFTABLE).add(str);
			} else if (dataSetDBController.isContain(str)) {
				graftableLineList.get(DATASET_GRAFTABLE).add(str);
			} else {
				graftableLineList.get(UNGRAFTABLE).add(str);
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
		RevTree rt = commit.getTree();
		TreeWalk tw = new TreeWalk(repo);

		tw.addTree(rt);
		tw.setRecursive(true);

		Multiset<String> sourceLines = HashMultiset.create();

		while (tw.next()) { // commitのすべてのファイルについて
			if (tw.getPathString().toString().endsWith(".java")) {

				ObjectReader reader = repo.newObjectReader();
				byte[] data = reader.open(tw.getObjectId(0)).getBytes();
				String normalizedSourceFile = normalizeSource(CommentRemover
						.deleteETC(new String(data, "utf-8")));
				normalizedSourceFile = CommentRemover
						.removeDelimiter(normalizedSourceFile);
				for (String line : normalizedSourceFile.split("\n")) {
					sourceLines.add(line.replaceAll("\n|\r", ""));
				}
			}
		}
		return sourceLines;

	}

	/**
	 * ソースコードのASTを構築して、ASTVisitorを起動する
	 *
	 * @param source
	 * @return
	 * @throws Exception
	 */
	private String normalizeSource(String source) throws Exception {
		Document doc = new Document(source);

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
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
