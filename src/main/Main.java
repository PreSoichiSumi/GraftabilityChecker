package main;

import jp.univ.graftability.GraftabilityChecker;


public class Main {
	private static final String reposPath = "F:\\s-sumi\\PlasticSurgery";
	private static final String outputPath = "F:\\s-sumi\\Res\\";
	//private static final String dataSetPath = "G:\\s-sumi\\NormVarDist\\";
	private static final String issueDBPath = "F:\\s-sumi\\Res\\issues.db";
	private static final String dataSetDBPath="F:\\s-sumi\\Res\\dataSet.db";

	public static void main(String[] args) throws Exception {

		/*{
			DBMaker dbMaker=new DBMaker(dataSetDBPath, dataSetPath, issueDBPath);
			//dbMaker.createDataSetDB();
			//dbMaker.makeDataSetDataBase();
			System.out.println("creating index");
			dbMaker.createDataSetIndex();
		}*/


		//RepositoryAnalyzer rAnalyzer = new RepositoryAnalyzer(
		//		new File(reposPath	 + "\\felix"), outputPath, issueDBPath, dataSetDBPath);
		//rAnalyzer.execute();
		 GraftabilityChecker gChecker=new GraftabilityChecker(reposPath,
				 outputPath, dataSetDBPath,issueDBPath);
		 gChecker.execute();
	}

}
