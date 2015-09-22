package jp.univ.graftability;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GraftabilityChecker {
	private final String reposPath;
	private final String outputPath;
	private final String dataSetPath;
	private File reposArray[];

	public GraftabilityChecker(String reposPath, String dataSetPath, String outputPath) {
		super();
		this.reposPath = reposPath;
		this.outputPath = outputPath;
		this.dataSetPath = dataSetPath;
	}

	public void execute() throws Exception{
		File reposFolder=new File(reposPath);
		reposArray = reposFolder.listFiles(); // 絶対パスで帰ってくる
		Set<String> dataSet = new HashSet<>();
		System.out.println("gettingucidataset");
		dataSet = DataSetController.getRawSourceString(dataSetPath);

		for (File repos : reposArray) {
			RepositoryAnalyzer rAnalyzer=new RepositoryAnalyzer(repos, dataSet, outputPath,"","");
			rAnalyzer.execute();
		}
	}

}
