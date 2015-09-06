package analyzeGit;

import java.util.ArrayList;
import java.util.List;

import jp.ac.osaka_u.ist.sdl.prevol.data.VectorData;
import jp.ac.osaka_u.ist.sdl.prevol.methodanalyzer.NodeTypeCounter;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor {

	CompilationUnit root;

	private List<MethodData> methodList;

	public MethodVisitor(CompilationUnit root) {
		this.root = root;
		this.methodList = new ArrayList<MethodData>();
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		constMethodData(node);
		return true;
	}

	private void constMethodData(MethodDeclaration node) {
		MethodData method = new MethodData();
		method.setName(node.getName().toString());
		method.setSignature(node.parameters().toString());
		method.setProgram(node.toString());

		/**
		 * 特徴量の取得
		 */
		final NodeTypeCounter counter = new NodeTypeCounter();
		node.accept(counter);
		VectorData vector = counter.getVectorData();//ベクトルデータの取得
		
		method.setVector(vector);
		
		methodList.add(method);
	}

	public List<MethodData> getMethodList() {
		return methodList;
	}

}
