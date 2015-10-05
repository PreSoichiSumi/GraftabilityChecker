package jp.univ.graftability;

import java.io.File;

public class GraftabilityChecker {
	private final String reposPath;
	private final String outputPath;
	private final String issueDBPath;
	private final String dataSetDBPath;

	private File reposArray[];

	public GraftabilityChecker(String reposPath, String outputPath,
			String dataSetDBPath, String issueDBPath) {
		super();
		this.reposPath = reposPath;
		this.outputPath = outputPath;
		this.dataSetDBPath = dataSetDBPath;
		this.issueDBPath=issueDBPath;
	}

	public void execute() throws Exception{
		File reposFolder=new File(reposPath);
		reposArray = reposFolder.listFiles(); // 絶対パスで帰ってくる
		System.out.println("thread start");

		for (File repos : reposArray) {
			System.out.println(repos.getName()+"-start");
			RepositoryAnalyzer rAnalyzer=new RepositoryAnalyzer(repos, outputPath,issueDBPath,dataSetDBPath);
			rAnalyzer.start();
		}
	}

}
