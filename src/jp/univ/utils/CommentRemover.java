package jp.univ.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

@SuppressWarnings("restriction")

public class CommentRemover {

	enum STATE {
		CODE, BLOCKCOMMENT, LINECOMMENT, STRING, CHAR;
	}

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	public String result;

	public CommentRemover() {
		this.result = null;
	}
	public static String deleteETC(String sourceFile) throws IOException{
		sourceFile=deleteLineComment(sourceFile);
		sourceFile=deleteBlockComment(sourceFile);
		sourceFile=deleteBlankLine(sourceFile);
		sourceFile=deleteBracketLine(sourceFile);
		sourceFile=deleteIndent(sourceFile);

		return sourceFile;
	}

	public static List<String> removeDelimiter(List<String> lines){
		List<String> res = lines;
		for (String line : res) {
			line = line.replaceAll("[\\{\\}\\;]", "");
		}
		return res;
	}
	public static String removeDelimiter(String lines){
		String res = lines;
		res = res.replaceAll("[\\{\\}\\;]", "");

		return res;
	}

	public void perform(final String[] args) {

		try {

			final Options options = new Options();

			{
				final Option i = new Option("i", "input", true,
						"input directory");
				i.setArgName("input");
				i.setArgs(1);
				i.setRequired(true);
				options.addOption(i);
			}

			{
				final Option o = new Option("o", "output", true,
						"output directory");
				o.setArgName("output");
				o.setArgs(1);
				o.setRequired(false);
				options.addOption(o);
			}

			{
				final Option l = new Option("l", "language", true, "language");
				l.setArgName("language");
				l.setArgs(1);
				l.setRequired(false);
				options.addOption(l);
			}

			{
				final Option x = new Option("x", "encoding", true, "encoding");
				x.setArgName("encoding");
				x.setArgs(1);
				x.setRequired(false);
				options.addOption(x);
			}

			{
				final Option a = new Option("a", "blankline", false,
						"blank line");
				a.setArgName("blankline");
				a.setRequired(false);
				options.addOption(a);
			}

			{
				final Option b = new Option("b", "blockcomment", false,
						"block comment");
				b.setArgName("blockcomment");
				b.setRequired(false);
				options.addOption(b);
			}

			{
				final Option c = new Option("c", "linecomment", false,
						"line comment");
				c.setArgName("linecomment");
				c.setRequired(false);
				options.addOption(c);
			}

			{
				final Option d = new Option("d", "bracketline", false,
						"bracket line");
				d.setArgName("bracketline");
				d.setRequired(false);
				options.addOption(d);
			}

			{
				final Option e = new Option("e", "indent", false, "indent");
				e.setArgName("indent");
				e.setRequired(false);
				options.addOption(e);
			}

			{
				final Option v = new Option("v", "verbose", false,
						"verbose output");
				v.setArgName("verbose");
				v.setRequired(false);
				options.addOption(v);
			}

			{
				final Option q = new Option("q", "quiet", false, "quiet");
				q.setArgName("quiet");
				q.setRequired(false);
				options.addOption(q);
			}

			final CommandLineParser parser = new PosixParser();
			final CommandLine cmd = parser.parse(options, args);

			{
				final String language = cmd.getOptionValue("l");
				if (!language.equalsIgnoreCase("java")
						&& !language.equalsIgnoreCase("c")
						&& !language.equalsIgnoreCase("charp")) {
					System.out.print("unavailable language: ");
					System.out.println(language);
					System.exit(0);
				}
			}

			final String optionI = cmd.getOptionValue("i");
			final String optionO = cmd.hasOption("o") ? cmd.getOptionValue("o")
					: null;
			final String optionL = cmd.hasOption("l") ? cmd.getOptionValue("l")
					: null;
			final File inputFile = new File(optionI);

			// if a file path is specified ...
			if (inputFile.isFile()) {

				final String text = readFile(inputFile,
						cmd.hasOption("x") ? cmd.getOptionValue("x") : null);

				final String[] newArgs = new String[args.length];
				for (int i = 0; i < args.length; i++) {
					if (args[i].equals(optionI)) {
						newArgs[i] = text;
					} else {
						newArgs[i] = args[i];
					}
				}

				main(newArgs);
			}

			// if a directory path is specified ...
			else if (inputFile.isDirectory()) {

				if (null == optionO) {
					System.out
							.println("specify an output file with option \"-o\" when you specify a directory as an input.");
					System.exit(0);
				}

				if (null == optionL) {
					System.out
							.println("specify a programming language with option \"-l\" when you specify a directory as an input");
					System.exit(0);
				}

				int index = 0;
				final Set<File> files = getFiles(new File(optionI), optionL);
				for (final File file : files) {

					if (cmd.hasOption("v")) {
						System.out.print(" [");
						System.out.print(index++ + 1);
						System.out.print("/");
						System.out.print(files.size());
						System.out.print("] ");
					}

					final String outputPath = file.getAbsolutePath().replace(
							optionI, optionO);
					if (cmd.hasOption("v")) {
						System.out.println(outputPath);
					}

					final String[] newArgs = new String[args.length];
					for (int i = 0; i < args.length; i++) {
						if (args[i].equals(optionI)) {
							newArgs[i] = file.getAbsolutePath();
						} else if (args[i].equals(optionO)) {
							newArgs[i] = outputPath;
						} else {
							newArgs[i] = args[i];
						}
					}

					main(newArgs);
				}

			}

			// if a file content is specified ...
			else {

				String text = optionI;
				if (!cmd.hasOption("c")) {
					text = deleteLineComment(text);
				}
				if (!cmd.hasOption("b")) {
					text = deleteBlockComment(text);
				}
				if (!cmd.hasOption("a")) {
					text = deleteBlankLine(text);
				}
				if (!cmd.hasOption("d")) {
					text = deleteBracketLine(text);
				}
				if (!cmd.hasOption("e")) {
					text = deleteIndent(text);
				}

				if (!cmd.hasOption("q")) {
					writeFile(text, optionO);
				}
				this.result = text;
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
	}

	public static void main(String[] args) throws IOException {
		final CommentRemover remover = new CommentRemover();
		remover.perform(args);
	}

	public static Set<File> getFiles(final File file, final String language) {

		final Set<File> files = new HashSet<File>();

		// ディレクトリならば，再帰的に処理
		if (file.isDirectory()) {
			final File[] subfiles = file.listFiles();
			for (int i = 0; i < subfiles.length; i++) {
				files.addAll(getFiles(subfiles[i], language));
			}
		}

		// ファイルならば，拡張子が対象言語と一致すれば登録
		else if (file.isFile()) {

			final String path = file.getAbsolutePath();
			if (language.equals("java")) {
				if (path.endsWith(".java")) {
					files.add(file);
				}
			} else if (language.equals("csharp")) {
				if (path.endsWith(".cs")) {
					files.add(file);
				}
			} else if (language.equals("c")) {
				if (path.endsWith(".c") || path.endsWith("cpp")
						|| path.endsWith("cxx") || path.endsWith(".h")
						|| path.endsWith(".hpp") || path.endsWith(".hxx")) {
					files.add(file);
				}
			}
		}

		else {
			System.err.println("\"" + file.getAbsolutePath()
					+ "\" is not a vaild file!");
			System.exit(0);
		}

		return files;
	}

	public static String readFile(final File file, final String encoding) {

		try {

			final StringBuilder text = new StringBuilder();
			final InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file), null != encoding ? encoding
							: "JISAutoDetect");
			while (reader.ready()) {
				final int c = reader.read();
				text.append((char) c);
			}
			reader.close();

			return text.toString();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * remove line-comment
	 *
	 * @param src
	 * @return
	 */
	private static String deleteLineComment(final String src) {

		StringBuilder buf = new StringBuilder();

		STATE currentSTATE = STATE.CODE;

		for (int i = 0; i < src.length(); i++) {
			final char prev = 0 < i ? src.charAt(i - 1) : '0';
			final char ch = src.charAt(i);
			final char next = (i < src.length() - 1) ? src.charAt(i + 1) : '0';

			if (STATE.BLOCKCOMMENT == currentSTATE) {

				buf.append(ch);

				if (prev == '*' && ch == '/') {
					currentSTATE = STATE.CODE;
				}
			}

			// lineコメントの中にいるとき
			else if (STATE.LINECOMMENT == currentSTATE) {
				// if (ch == LINE_SEPARATOR.charAt(0)) {
				if (ch == '\n' || ((ch != '\n') && (prev == '\r'))) {
					currentSTATE = STATE.CODE;
					buf.append(LINE_SEPARATOR);
				}
			}

			// String型のリテラルの中にいるとき
			else if (STATE.STRING == currentSTATE) {
				buf.append(ch);

				// エスケープシーケンスだったら次の文字も追加
				if (ch == '\\') {
					buf.append(src.charAt(i++));
				}

				// リテラルを抜ける
				else if (ch == '\"') {
					currentSTATE = STATE.CODE;
				}
			}

			// charのリテラルの中にいるとき
			else if (STATE.CHAR == currentSTATE) {
				buf.append(ch);

				// エスケープシーケンスだったら次の文字も追加
				if (ch == '\\') {
					buf.append(src.charAt(i++));
				}

				// リテラルを抜ける
				else if (ch == '\'') {
					currentSTATE = STATE.CODE;
				}
			}

			// ブロックコメント開始
			else if (ch == '/' && next == '*') {
				currentSTATE = STATE.BLOCKCOMMENT;
				buf.append("/*");
				i++; // '*' を二重読みしないための処理
			}

			// ラインコメント開始
			else if (ch == '/' && next == '/') {
				currentSTATE = STATE.LINECOMMENT;
			}

			// Stringのリテラル開始
			else if (ch == '\"') {
				currentSTATE = STATE.STRING;
				buf.append(ch);
			}

			// charの開始
			else if (ch == '\'') {
				currentSTATE = STATE.CHAR;
				buf.append(ch);
			}

			// そのまま処理
			else {
				buf.append(ch);
			}
		}

		return buf.toString();
	}

	/**
	 * remove block-comment
	 *
	 * @param src
	 * @return
	 */
	private static String deleteBlockComment(final String src) {

		StringBuilder buf = new StringBuilder();

		STATE currentSTATE = STATE.CODE;

		for (int i = 0; i < src.length(); i++) {
			final char prev = 0 < i ? src.charAt(i - 1) : '0';
			final char ch = src.charAt(i);
			final char next = (i < src.length() - 1) ? src.charAt(i + 1) : '0';

			// ブロックコメントの中にいるとき
			if (STATE.BLOCKCOMMENT == currentSTATE) {
				if (prev == '*' && ch == '/') {
					currentSTATE = STATE.CODE;
					buf.append(" ");
				}

				else if (ch == '\n' || ((ch != '\n') && (prev == '\r'))) {
					buf.append(LINE_SEPARATOR);
				}
			}

			// ラインコメントの中にいるとき
			else if (STATE.LINECOMMENT == currentSTATE) {
				buf.append(ch);

				if (ch == '\n' || ((ch != '\n') && (prev == '\r'))) {
					currentSTATE = STATE.CODE;
				}
			}

			// String型のリテラルの中にいるとき
			else if (STATE.STRING == currentSTATE) {
				buf.append(ch);

				// エスケープシーケンスだったら次の文字も追記
				if (ch == '\\') {
					buf.append(src.charAt(i++));
				}

				// リテラルを抜ける
				else if (ch == '\"') {
					currentSTATE = STATE.CODE;
				}
			}

			// charのリテラルの中にいるとき
			else if (STATE.CHAR == currentSTATE) {
				buf.append(ch);

				// エスケープシーケンスだったら次の文字も追加
				if (ch == '\\') {
					buf.append(src.charAt(i++));
				}

				// リテラルを抜ける
				else if (ch == '\'') {
					currentSTATE = STATE.CODE;
				}
			}

			// ブロックコメントに入る
			else if (ch == '/' && next == '*') {
				currentSTATE = STATE.BLOCKCOMMENT;
				i++; // '*'を読み飛ばすための処理
			}

			// Stringのリテラル開始
			else if (ch == '\"') {
				currentSTATE = STATE.STRING;
				buf.append(ch);
			}

			// charの開始
			else if (ch == '\'') {
				currentSTATE = STATE.CHAR;
				buf.append(ch);
			}

			// そのまま処理
			else {
				buf.append(ch);
			}
		}

		return buf.toString();
	}

	/**
	 * remove blank-line
	 *
	 * @param src
	 * @return
	 * @throws IOException
	 */
	private static String deleteBlankLine(final String src) throws IOException {

		StringBuilder buf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new StringReader(src));

		String inLine;
		while ((inLine = reader.readLine()) != null) {
			if (!inLine.matches("^\\s*$")) {
				buf.append(inLine);
				buf.append(LINE_SEPARATOR);
			}
		}

		return buf.toString().replaceFirst("\\s*$", "");
	}

	/**
	 * connect close-bracket-line to the previous line
	 *
	 * @param src
	 * @return
	 */
	private static String deleteBracketLine(final String src) {

		final StringBuilder text = new StringBuilder();
		for (final StringTokenizer tokenizer = new StringTokenizer(src,
				LINE_SEPARATOR); tokenizer.hasMoreTokens();) {

			final String line = tokenizer.nextToken();
			if (line.matches("[ \t]*[{][ \t]*")) {
				text.append("{");
			}

			else if (line.matches("[ \t]*[}][ \t]*")) {
				text.append("}");
			}

			else {

				// textの長さが0でなければ，行を分けるためのセパレータを挿入
				if (0 < text.length()) {
					text.append(LINE_SEPARATOR);
				}
				text.append(line);
			}
		}

		return text.toString();
	}

	/**
	 * delete indent
	 *
	 * @param src
	 * @return
	 */
	private static String deleteIndent(final String src) {
		return src.replaceAll(LINE_SEPARATOR + "[ \t]+", LINE_SEPARATOR);
	}

	/**
	 *
	 *
	 * @param text
	 * @param path
	 */
	public static void writeFile(final String text, final String path) {

		try {

			if (null != path) {
				final File file = new File(path);
				file.getParentFile().mkdirs();
			}

			final OutputStream out = null != path ? new FileOutputStream(path)
					: System.out;
			for (int i = 0; i < text.length(); i++) {
				out.write(text.charAt(i));
			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
