package jp.univ.graftability;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.text.edits.TextEdit;

import analyzeGit.CommentRemover;
import analyzeGit.SourceVisitor;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

import difflib.Chunk;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;

/**
 * 2リビジョン間で変更があったファイルを解析するクラス
 *
 * @author s-sumi
 */
public class ChangeAnalyzer {
	private RevCommit olderRevision;
	private RevCommit newerRevision;
	private boolean removeBrackets = false;
	private Repository repo;

	public ChangeAnalyzer(RevCommit newRev, Repository repo) {
		super();
		this.newerRevision = newRev;

		if (newRev.getId().toString().equals(newRev.getParent(0).getId().toString()))
			System.out.println("getParent method is invalid\n");

		this.olderRevision = newRev.getParent(0);
		this.repo = repo;
	}

	/**
	 * 与えられた2つのコミットから追加された行を正規化したものの集合を返す 追加がなければ空集合が帰る
	 *
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public Multiset<String> getAddedLineSet() throws Exception {
		if (repo == null) {
			throw new NullPointerException("repo is null");
		}

		Multiset<String> lineList = LinkedHashMultiset.create();
		String oldsourceString;
		String newsourceString;

		AbstractTreeIterator oldTreeParser = prepareTreeParser(repo,
				olderRevision.getId().getName().toString());
		AbstractTreeIterator newTreeParser = prepareTreeParser(repo,
				newerRevision.getId().getName().toString());

		// まず2リビジョン間の差分を取る
		List<DiffEntry> diff = new Git(repo).diff().setOldTree(oldTreeParser)
				.setNewTree(newTreeParser)
				.setPathFilter(PathSuffixFilter.create(".java")). // java以外のときは跡で考えよう
				call();
		// 変更があったファイルについて、変更によって追加されたソースコード行を取得する
		for (DiffEntry entry : diff) { // diffから詳細を読み込んで変更行を取り出し，書き込み
										// DiffEntry:どのファイルで，どんな種類の変更があったかの情報を保持する．
										// ChangeType:ファイルにどのような変更があったか．Deleteはファイルがけされたということ．
			ObjectLoader olold;
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
					if (removeBrackets)
						oldsourceString = CommentRemover
								.removeDelimiter(oldsourceString);

				} else {
					oldsourceString = "";
				}

				if (entry.getNewId().toObjectId().equals(ObjectId.zeroId()) == false) { // NEWが存在するか
					olnew = repo.open(entry.getNewId().toObjectId()); // ソースを読み込んで，コメントなどを消去
					olnew.copyTo(bosnew);
					newsourceString = bosnew.toString();
					// newsourceString = Arrays.toString(olnew.getBytes());
					newsourceString = CommentRemover.deleteETC(newsourceString);
					if (removeBrackets)
						newsourceString = CommentRemover
								.removeDelimiter(newsourceString);
					newsourceString.split("\n");
				} else {
					newsourceString = "";
				}

				// --------------コメントを取り除いたものをDiff

				List<Delta> dltList = DiffUtils.diff(
						Arrays.asList(oldsourceString.split("\n")),
						Arrays.asList(newsourceString.split("\n"))).getDeltas();
				List<Pair<Integer, Integer>> pairlist = new ArrayList<>();
				for (Delta dlt : dltList) {
					// diffResString= dlt.getRevised().getLines().toString();
					if (dlt.getType() == TYPE.INSERT
							|| dlt.getType() == TYPE.CHANGE) { // 追加，または修正されたファイルのうち，挿入または変更された行について処理をおこなう
						final Chunk chunk = dlt.getRevised();
						pairlist.add(new MutablePair<Integer, Integer>(Integer
								.valueOf(chunk.getPosition() + 1), Integer
								.valueOf(chunk.last() + 1))); // 全ての変更について，行番号の範囲を得る．
					}
				}

				String normedSource = normalizeSource(CommentRemover
						.deleteETC(bosnew.toString())); // 正規化：ComRmして、変数を正規化して、CompirationUnit.toStringを使わずに出力

				LineNumberReader lineNumReader = new LineNumberReader(
						new StringReader(CommentRemover.deleteETC(normedSource))); // 正規化したものからコメントを取り除いてよみとり
				String lineString = null;
				while ((lineString = lineNumReader.readLine()) != null) {
					int linenum = lineNumReader.getLineNumber(); // これ
					for (Pair<Integer, Integer> pair : pairlist) {
						if ((pair.getLeft().intValue() <= linenum)
								&& (linenum <= pair.getRight().intValue())) {
							lineList.add(CommentRemover.removeDelimiter(
									lineString).replaceAll("\n|\r", ""));
							break;
						}
					}
				}
			}

		}
		return lineList;
	}

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

	public RevCommit getOldCommit() {
		return olderRevision;
	}

	public void setOldCommit(RevCommit oldCommit) {
		this.olderRevision = oldCommit;
	}

	public RevCommit getNewCommit() {
		return newerRevision;
	}

	public void setNewCommit(RevCommit newCommit) {
		this.newerRevision = newCommit;
	}

	public boolean isRemoveBrackets() {
		return removeBrackets;
	}

	public void setRemoveBrackets(boolean removeBrackets) {
		this.removeBrackets = removeBrackets;
	}

	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}
}
