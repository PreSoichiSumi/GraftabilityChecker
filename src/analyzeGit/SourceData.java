package analyzeGit;


public class SourceData {

	private String name;

	private String signature;

	private String program;

	public SourceData(){
		this.name = null;
		this.signature = null;
		this.program = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

}
