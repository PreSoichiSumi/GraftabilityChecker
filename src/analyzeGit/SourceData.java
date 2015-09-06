package analyzeGit;

import jp.ac.osaka_u.ist.sdl.prevol.data.VectorData;

public class SourceData {

	private String name;

	private String signature;

	private String program;

	private VectorData vector;	//状態ベクトル

	public SourceData(){
		this.name = null;
		this.signature = null;
		this.program = null;
		this.vector = null;
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

	public void setVector(VectorData vector) {
		this.vector = vector;
	}

	public VectorData getVector(){
		return vector;
	}
}
