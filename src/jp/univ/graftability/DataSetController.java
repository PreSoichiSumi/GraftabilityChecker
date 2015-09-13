package jp.univ.graftability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.univ.utils.FileUtils;
import analyzeGit.CommentRemover;

/**
 * DataSetを読み込んで保持するクラス
 * @author s-sumi
 *
 */
public class DataSetController {
	/**
	 * 与えられたフォルダ内のすべてのjavaファイルの行の集合を取得する
	 * コメントの除去は行わない．
	 * @return
	 */
	public static Set<String> getRawSourceString(String folderPath)throws Exception{

		Set<String> sourceLines = new HashSet<String>(70000000);
		File sourceFolder= new File(folderPath);
		File folders[]= sourceFolder.listFiles();
		for (File folder : folders) {
			System.out.println("Processing "+folder.getPath());
			List<File> sourceList = new ArrayList<>();
			FileUtils.makeFileList(folder,sourceList);
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
	// ファイル内容をを文字列化するメソッドです。
	public static String fileToString(File file) throws IOException {
		BufferedReader br = null;
		try {
			// ファイルを読み込むバッファドリーダを作成します。
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
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
		} catch (Exception e) {
			System.out.println(e);
			return null;
		} finally {
			// リーダを閉じます。
			br.close();
		}
	}
}
