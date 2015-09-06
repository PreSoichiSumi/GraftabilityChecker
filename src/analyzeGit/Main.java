package analyzeGit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Main {

	static String filePath = "test.java";
	static final String datasetPath ="H:\\dataset\\raw\\UCI";
	//static final String datasetPath="sources";
	static final String outPath="H:\\dataset\\raw\\UCI_rmNormalized";
	//static final String outPath="out";

	static File folders[] =null;
	static List<File> fileList = new ArrayList<File>();

	public static void main(String[] args) throws Exception {
		File dataset = new File(datasetPath);
		folders= dataset.listFiles();
		for (File folder : folders) {
		//	String name = folder.getName();
		//	if(!( name.equals("57") || name.equals("58") || name.equals("59") || name.equals("60")) )continue;
			

			fileList.clear();
			System.out.println("processing : "+folder.getName());

			if(folder.exists()){
				makeFileList(folder);
			}else {
				System.out.println("target directory does not exist.");
				System.exit(0);
			}

			for(File file: fileList)
				System.out.println(file.getName().toString());



			for(File file:fileList){
				//PrintWriter pw = getPrintWriter("revised-"+file.getName());
		//	if(file.getCanonicalPath().indexOf("395")==-1)continue;
				//-------------定型処理-------------
				PrintWriter pw =  getPrintWriter(file.getCanonicalPath().replaceFirst("UCI","UCI_NormValRm"));
				//PrintWriter pw = getPrintWriter(file.getCanonicalPath().replaceFirst("sources", "result"));
				String sourceString=fileToString(file);

			//	removedString = CommentRemover.removeDelimiter(removedString);	//ここ実験によって変更
				/*
				BufferedReader reader = new BufferedReader(new FileReader(file));
				StringBuffer sb = new StringBuffer();
				String st = null;
				while ((st = reader.readLine()) != null) {
					sb.append(st + System.getProperty("line.separator"));
				}
				reader.close();*/
				ASTParser parser = ASTParser.newParser(AST.JLS4);
				parser.setSource(sourceString.toCharArray());
				//parser.setSource(sb.toString().toCharArray());

				//parser.setKind(ASTParser.K_COMPILATION_UNIT);
				//parser.setResolveBindings(true);	//構文解析レベル2　はじめからレベル2にするのがいいか
													//Nodeにたどり着いてからresolveBindingするのがいいかは分からない
				CompilationUnit unit = (CompilationUnit) parser
						.createAST(new org.eclipse.core.runtime.NullProgressMonitor());

				SourceVisitor visitor = new SourceVisitor(unit);
				visitor.setNormalizeOptions(true, false,
						false, false, false, false, false);
				//unit.recordModifications();
				unit.accept(visitor);
				//-------------ここまで------------
				pw.print(CommentRemover.deleteETC(unit.toString()));
				pw.close();
			}
		}

			//System.out.println("list size is " + methodList.size());
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

	private static PrintWriter getPrintWriter(String path) throws IOException {
		makedirParent(path);
		File file = new File(path);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fileWriter);
		PrintWriter pw = new PrintWriter(bw);
		return pw;
	}

/*	private void applyDocument(IDocument document, CompilationUnit unitNode) throws MalformedTreeException, BadLocationException, Exception {
		// 変更箇所を生成する
		TextEdit edit = unitNode.rewrite(document, null);

		// 変更をドキュメントに反映する
		edit.apply(document);
	}*/
	 /* ディレクトリを作成する.
	 * @param dirPath
	 */
	public static void makedir(String dirPath){
	    File dir = new File(dirPath);
	    if(!dir.exists()){
	        dir.mkdirs();
	    }
	}
	 /* ファイルの親ディレクトリを作成する.
	 * @param filePath
	 */
	public static void makedirParent(String filePath){
	    File file = new File(filePath);
	    makedir(file.getParent());
	}
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
	    } finally {
	      // リーダを閉じます。
	      br.close();
	    }
	  }
}
