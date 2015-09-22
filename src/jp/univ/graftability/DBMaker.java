package jp.univ.graftability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jp.univ.utils.FileUtils;
import analyzeGit.CommentRemover;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * データセットのソースコード行、 またはJIRAのIssueのデータベースを作成する
 *
 * @author s-sumi
 *
 */
public class DBMaker {
	private String dataSetDBPath;
	private String rawDataSetPath;
	private String issueDBPath;
	private List<Iterable<Issue>> issueList;
	private final int BATCHSIZE = 1000;
	@Deprecated
	public DBMaker(String dataSetOutputPath, String rawDataSetPath,
			String issueDBPath,List<Iterable<Issue>> issueList) {
		super();
		this.dataSetDBPath = dataSetDBPath;
		this.rawDataSetPath = rawDataSetPath;
		this.issueDBPath = issueDBPath;
		this.issueList=issueList;
	}
	public DBMaker(String dataSetDBPath, String rawDataSetPath,
			String issueDBPath) {
		super();
		this.dataSetDBPath = dataSetDBPath;
		this.rawDataSetPath = rawDataSetPath;
		this.issueDBPath = issueDBPath;
	}

	public static Connection createConnection(String file) {
		try {
			Class.forName("org.sqlite.JDBC");
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + file);
			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	// ソースコード行の種類は符号付4バイト整数の範囲内
	public void makeDataSetDataBase() {
		Connection con = createConnection(dataSetDBPath);
		HashFunction func = Hashing.murmur3_32(); //seed=0
		int batchCount = 0;
		try {
			PreparedStatement pStatement = con
					.prepareStatement("INSERT INTO LINETABLE VALUES (?,?);");

			File sourceFolder = new File(rawDataSetPath);
			File folders[] = sourceFolder.listFiles();

			for (File folder : folders) {

				System.out.println("Processing " + folder.getPath());
				List<File> sourceList = new ArrayList<>();
				FileUtils.makeFileList(folder, sourceList);

				for (File source : sourceList) {
					System.out.println("   process " + source.getPath());
					for (String line : fileToString(source).split("\n")) {
						String tmp=CommentRemover.removeDelimiter(line).replaceAll("\n|\r", "");

						//pStatement.setString(1,tmp);
						//pStatement.setInt(2, (int)func.hashBytes(tmp.getBytes()).asInt());
						pStatement.setInt(1, (int)func.hashBytes(tmp.getBytes()).asInt());
						pStatement.setString(2, tmp);
						pStatement.addBatch();
						batchCount++;
						if (batchCount == BATCHSIZE) {
							pStatement.executeBatch();
							pStatement.clearBatch();
							batchCount=0;
						}
					}
				}
				sourceList = null;
			}
			pStatement.executeBatch();
			pStatement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.commit();
				con.close();
			} catch (SQLException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		return;
	}
	public void createDataSetDB(){
		Connection con= createConnection(dataSetDBPath);
		try{	//java se7よりtry
			Statement smt=con.createStatement();
			smt.execute("create table linetable(hash integer, line text unique on conflict ignore)");
			con.commit();
			smt.close();
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void dropDataSetDB(){
		Connection con= createConnection(dataSetDBPath);
		try{
			Statement smt=con.createStatement();
			smt.execute("drop table if exists linetable");
			con.commit();
			smt.close();
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void createDataSetIndex(){
		Connection con= createConnection(dataSetDBPath);
		try {
			Statement smt=con.createStatement();
			smt.execute("create index hashindex on linetable (hash)");
			con.commit();
			smt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void createIssueDB(){
		Connection con= createConnection(issueDBPath);
		try{	//java se7よりtry
			Statement smt=con.createStatement();
			smt.execute("create table issuetable(ID INTEGER, ISSUEKEY TEXT, PROJECT TEXT, ISSUETYPE TEXT, STATUS TEXT, RESOLUTION TEXT)");
			con.commit();
			smt.close();
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void dropIssueTable(){
		Connection con= createConnection(issueDBPath);
		try{
			Statement smt=con.createStatement();
			smt.execute("drop table if exists issuetable");
			con.commit();
			smt.close();
			con.close();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void createIssueIndex(){
		Connection con= createConnection(issueDBPath);
		try {
			Statement smt=con.createStatement();
			smt.execute("create index issuekeytable on issuetable (ISSUEKEY)");
			con.commit();
			smt.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void makeIssueDataBase() {
		int batchCount = 0;
		Connection con = createConnection(issueDBPath);
		Statement smt = null;
		try {

			smt = con.createStatement();
			//smt.execute("drop table if exists issuetable");
			// IDはuniqueなものが渡されるはずだが、念のため。
			// Issueは比較的数が少ないはずなので、この仕様に決定
			//smt.execute("create table issuetable(ID INTEGER, ISSUEKEY TEXT, PROJECT TEXT, ISSUETYPE TEXT, STATUS TEXT, RESOLUTION TEXT)");
			PreparedStatement pStatement = con
					.prepareStatement("insert into issuetable values(?,?,?,?,?,?)");

			for (Iterable<Issue> iterable : issueList) {
				for (Issue issue : iterable) {
					pStatement.setLong(1, issue.getId());
					pStatement.setString(2, issue.getKey());
					pStatement.setString(3, issue.getProject().getName());	//getIdにすれば容量の節約になる
					pStatement.setString(4, issue.getIssueType().getName());	//getIdにすれば..
					pStatement.setString(5, issue.getStatus().getName());	//getId..
					pStatement.setString(6, issue.getResolution().getName());
					pStatement.addBatch();
					batchCount++;
					if (batchCount == BATCHSIZE) {
						pStatement.executeBatch();
						pStatement.clearBatch();
						batchCount=0;
					}
				}
			}
			pStatement.executeBatch();
			pStatement.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.commit();
				con.close();
				smt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return;

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

	public String getDataSetOutputPath() {
		return dataSetDBPath;
	}

	public void setDataSetOutputPath(String dataSetOutputPath) {
		this.dataSetDBPath = dataSetOutputPath;
	}

	public String getRawDataSetPath() {
		return rawDataSetPath;
	}

	public void setRawDataSetPath(String rawDataSetPath) {
		this.rawDataSetPath = rawDataSetPath;
	}

	public String getIssueOutputPath() {
		return issueDBPath;
	}

	public void setIssueOutputPath(String issueDBPath) {
		this.issueDBPath = issueDBPath;
	}

}
