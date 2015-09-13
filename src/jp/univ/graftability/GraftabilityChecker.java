package jp.univ.graftability;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GraftabilityChecker {
	private String reposPath;
	private String outputPath;
	private String dataSetPath;
	private File reposArray[];

	public String getReposPath() {
		return reposPath;
	}

	public void setReposPath(String reposPath) {
		this.reposPath = reposPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getDataSetPath() {
		return dataSetPath;
	}

	public void setDataSetPath(String dataSetPath) {
		this.dataSetPath = dataSetPath;
	}

	public void execute() throws Exception{
		File reposFolder=new File(reposPath);
		reposArray = reposFolder.listFiles(); // 絶対パスで帰ってくる
		Set<String> dataSet = new HashSet<>();
		System.out.println("gettingucidataset");
		dataSet = DataSetController.getRawSourceString(dataSetPath);

		for (File repos : reposArray) {
			RepositoryAnalyzer rAnalyzer=new RepositoryAnalyzer(repos, dataSet, outputPath);
			rAnalyzer.execute();
		}
	}

}
