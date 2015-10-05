package jp.univ.graftability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class DBController {
	private final Connection con;
	private final String dbPath;
	private PreparedStatement checkDataSet;
	private final int HASH=1;
	private final int LINE=2;
	private HashFunction func=null;
	public DBController(String dbPath) {
		super();
		this.dbPath = dbPath;
		this.con=DBMaker.createConnection(dbPath);
		try{
			this.con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}

	/**
	 * コミットコメントにIssueKeyが含まれ、JIRAのIssueKeyと一致するか確認する
	 * @param comList
	 * @param projectName
	 * @return
	 */
	public List<RevCommit> retrieveClosedCommit(List<RevCommit> comList,String projectName){
		List<RevCommit> resComList=new ArrayList<>();
		try{
			PreparedStatement pStatement=con.prepareStatement("select * from issuetable where ISSUEKEY = ?");
			for(RevCommit com: comList){
				String issueKey = getIssueKey(com.getFullMessage(), projectName);
				if (issueKey != null) {
					pStatement.setString(1, issueKey);
					ResultSet rs=pStatement.executeQuery();
					if(rs.next()){	//結果が1件でも存在すれば
						resComList.add(com);
						//System.out.println(rs.getInt(1));
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return resComList;
	}
	/**
	 * データセットに行が含まれてるか探すときは最初に
	 * このメソッドを実行
	 */
	public void prepareForCheckingDataSet(){
		try{
			checkDataSet=con.prepareStatement("select line from linetable where hash = ? and line = ?");
			func=Hashing.murmur3_32();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	public boolean isContain(String line){
		boolean contained=false;
		try{
			checkDataSet.setInt(HASH, (int)func.hashBytes(line.getBytes()).asInt());
			checkDataSet.setString(LINE, line);
			ResultSet rs= checkDataSet.executeQuery();
			if(rs.next())
				contained=true;
		}catch(SQLException e){
			e.printStackTrace();
		}
		return contained;
	}

	/**
	 * コミットコメントからprojectのキーを捜し，
	 * はじめに見つかったキーを大文字でかえす
	 * @param target
	 * @param project
	 * @return key (UpperCase)
	 */
	private String getIssueKey(String target,String project){
		//apacheプロジェクトのIssuekey命名規則
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


}
