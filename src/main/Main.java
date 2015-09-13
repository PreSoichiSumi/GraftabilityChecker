package main;

import jp.univ.graftability.GraftabilityChecker;

public class Main {
	private static final String reposPath = "F:\\s-sumi\\PlasticSurgery";
	private static final String outputPath = "F:\\s-sumi\\Res\\";
	private static final String dataSetPath = "G:\\s-sumi\\NormVarDist\\";

	public static void main(String[] args) throws Exception{
		GraftabilityChecker gChecker=new GraftabilityChecker();
		gChecker.setReposPath(reposPath);
		gChecker.setDataSetPath(dataSetPath);
		gChecker.setOutputPath(outputPath);
		gChecker.execute();
	}

}
