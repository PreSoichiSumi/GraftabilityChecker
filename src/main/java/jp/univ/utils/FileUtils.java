package jp.univ.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class FileUtils {
	/**
	 * ファイルの親ディレクトリを作成する.
	 *
	 * @param filePath
	 */
	public static void makedirParent(String filePath) {
		File file = new File(filePath);
		makedir(file.getParent());
	}

	/**
	 * ディレクトリを作成する.
	 *
	 * @param dirPath
	 */
	public static void makedir(String dirPath) {
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	/**
	 * ディレクトリを再帰的に読んでfileListに追加
	 *
	 * @param folderPath
	 */
	public static void makeFileList(File dir, List<File> fileList) {

		File[] files = dir.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (!file.exists())
				continue;
			else if (file.isDirectory())
				makeFileList(file, fileList);
			else if (file.isFile())
				fileList.add(file);
		}
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
