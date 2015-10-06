package jp.univ.utils;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
//import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class SourceVisitor extends ASTVisitor {

	CompilationUnit root;

	//private List<MethodData> methodList;
	private boolean normVariable=	false;
	private boolean normMethod	=	false;
	private boolean normType	=	false;
	private boolean normLiteral	=	false;
	private boolean normModifier=	false;
	private boolean normPackage	= 	false;
	private boolean normImport 	=	false;

	public Map<Integer, Map<String,String>> dictionary= new HashMap<Integer,Map<String,String>>();

	private List<String> nonVariableNameList = Arrays.asList(
			"$methodName","$typeName","$modifierName","$packageName","$importName");

	public SourceVisitor(CompilationUnit root) {
		this.root = root;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}
/*
	@Override
	public boolean visit(SimpleName node) {
		 binding取得できないからだめやね．Eclipseプラグインなら実装簡単だったのにね．
		int kind = node.resolveBinding().getKind();
		switch (kind) {
		case IBinding.VARIABLE:
			node.setIdentifier("$variableName");
			break;
		case IBinding.METHOD:
			node.setIdentifier("$methodName");
			break;
		default:
			//何もしない
			break;
		}
		return true;
	}*/
	//------------VARIABLE------------------------
	//型宣言とかでなければ名前とする法
	@Override
	public boolean visit(SimpleName node) {//各行で最初にたどり着いた変数のノードを$0とする
		if(normVariable){
			if( isVariableName(node) ){
				int parent= node.getParent().getNodeType();
				if((parent==ASTNode.TYPE_DECLARATION) ||
					(parent==ASTNode.TYPE_DECLARATION_STATEMENT) ){
					//node.setIdentifier("$variableName");
				}else{
	//				if(node.getIdentifier().endsWith("\n"))
					Integer variableLineNum= Integer.valueOf( root.getLineNumber( node.getStartPosition() ));
					if(dictionary.containsKey( variableLineNum ) ) {	//その行の変数が登録されてるなら.その行のmapのサイズは必ず1以上
						if (dictionary.get(variableLineNum).containsKey(node.getIdentifier())) {	//既に変換後の変数が登録されてるなら
							node.setIdentifier(dictionary.get(variableLineNum).get(node.getIdentifier()));
						}else{
							String nextString="$"+(dictionary.get(variableLineNum).size());
							dictionary.get(variableLineNum).put(node.getIdentifier(), nextString );
							node.setIdentifier(nextString);
						}
					}else{												//その行の変数が登録されてなければ
						dictionary.put(variableLineNum, new HashMap<String,String>());
						dictionary.get(variableLineNum).put(node.getIdentifier(), "$0");
						node.setIdentifier("$0");
					}
//					node.setIdentifier("$variableName");
				}

			}
		}
		return true;
	}

	//-------------TYPE---------------------------
	@Override
	public boolean visit(SimpleType node) {
		if(normType){
			//int parent = node.getParent().getNodeType();
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$typeName");
			node.setName(name);
		}
		return false;
	}
	@Override
	public boolean visit(TypeDeclaration node) {
		if(normType){
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$typeName");
			node.setName(name);
		}
		return true;
	}
	//------------METHOD---------------------------
	/*public boolean visit(MethodDeclaration node) {
		if (normMethod) {
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$methodName");
			node.setName(name);
		}
		return true;
	}
	public boolean visit(MethodInvocation node) {
		if (normMethod) {
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$methodName");
			node.setName(name);
		}
		return true;
	}*/
	@Override
	public boolean visit(MethodDeclaration node) {
		if (normMethod) {
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$methodName");
			node.setName(name);
			for (Object obj : node.modifiers()) {
				((ASTNode)obj).accept(this);
			}
			if(node.getBody()!=null){
				node.getBody().accept(this);
			}
			if(node.getReturnType2()!=null){
				node.getReturnType2().accept(this);
			}
			for(Object o: node.parameters()){
				((ASTNode)o).accept(this);
			}
			return false;
		}else{
			for (Object obj : node.modifiers()) {
				((ASTNode)obj).accept(this);
			}
			if(node.getBody()!=null){
				node.getBody().accept(this);
			}
			if(node.getReturnType2()!=null){
				node.getReturnType2().accept(this);
			}
			for(Object o: node.parameters()){
				((ASTNode)o).accept(this);
			}
			return false;
		}
	}
	@Override
	public boolean visit(MethodInvocation node) {
		if (normMethod) {
			if(node.getExpression()!=null){
				node.getExpression().accept(this);
			}
			if (0 < node.arguments().size()) {
				for (Object obj : node.arguments()) {
					((ASTNode)obj).accept(this);
				}
			}
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$methodName");
			node.setName(name);
			return false;
		}else{
			if(node.getExpression()!=null){
				node.getExpression().accept(this);
			}
			if (0 < node.arguments().size()) {
				for (Object obj : node.arguments()) {
					((ASTNode)obj).accept(this);
				}
			}
			//nameはacceptしない
			return false;
		}
	}
	//------------MODIFIER---------------------------
	public boolean visit(Modifier node) {
		if(normModifier){	//全てfinalにすることで正規化とする
			node.setKeyword(Modifier.ModifierKeyword.FINAL_KEYWORD);
		}
		return true;
	}
	//------------PACKAGE---------------------------
	public boolean visit(PackageDeclaration node) {
		if(normPackage){
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$packageName");
			node.setName(name);
			return false;
		}else{
			return false;
		}
	}
	//------------IMPORT---------------------------
	public boolean visit(ImportDeclaration node) {
		if(normImport){
			AST ast = node.getAST();
			SimpleName name = ast.newSimpleName("$importName");
			node.setName(name);
			return false;
		}else{
			return false;
		}
	}
	//------------LITERAL-------------------------
	@Override
	public boolean visit(BooleanLiteral node) {
		if(normLiteral){		//trueに正規化
			node.setBooleanValue(true);
		}
		return true;
	}
	@Override
	public boolean visit(CharacterLiteral node) {
		if(normLiteral){		//Nに正規化
			node.setCharValue('N');
		}
		return true;
	}
	@Override
	public boolean visit(NullLiteral node) {
								//正規化しようがない
		return true;
	}
	@Override
	public boolean visit(StringLiteral node) {
		if (normLiteral) {		//$normLiteralに正規化
			node.setLiteralValue("$normLiteral");
		}
		return true;
	}
	@Override
	public boolean visit(TypeLiteral node) {
		if (normLiteral) {
			AST ast = node.getAST();

			//settypeの内部では名前を設定してるだけなので，これでよい
			//voidに正規化
			node.setType(ast.newSimpleType(ast.newSimpleName("void")));
		}
		return true;
	}
	@Override
	public boolean visit(NumberLiteral node) {
		if(normLiteral){	//0に正規化
			node.setToken("0");
		}
		return true;
	}

	//------------ANNOTATION-------------------------
	// @ TypeName ( [ MemberValuePair { , MemberValuePair } ] )
	public boolean visit(NormalAnnotation node) {
		for (Object o : node.values()) {
			((ASTNode)o).accept(this);
		}
		return false;
	}

	//@ TypeName ( Expression  )
	public boolean visit(SingleMemberAnnotation node) {
		node.getValue().accept(this);
		return false;
	}

	//   [ Javadoc ] { ExtendedModifier } @ interface Identifier
    //   { { AnnotationTypeBodyDeclaration | ; } }
	//
	// AnnotationTypeBodyDeclaration:
	//  	AnnotationTypeMemberDeclaration
	// 	 	FieldDeclaration
	//  	TypeDeclaration
	//  	EnumDeclaration
	//  	AnnotationTypeDeclaration
	public boolean visit(AnnotationTypeDeclaration node) {
		for (final Object o : node.bodyDeclarations()) {
			((ASTNode) o).accept(this);
		}
		return false;
	}

    // [ Javadoc ] { ExtendedModifier }
    //  Type Identifier ( ) [ default Expression ] ;
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		node.getDefault().accept(this);
		return false;
	}
	//@ TypeName
	public boolean visit(MarkerAnnotation node) {
		return false;
	}

	//----------ENDnormalize------------------------------------

	private boolean isVariableName(SimpleName node){
		boolean flag=true;
		if(nonVariableNameList.contains(node.getIdentifier())){		//variableでないなら
			flag=false;												//falseを返す
		}
		return flag;
	}

	public void setNormalizeOptions(boolean val,boolean method ,boolean type,
					boolean literal,boolean modifier,boolean pack,boolean impo){
		normVariable=	val;
		normMethod	=	method;
		normType	=	type;
		normLiteral	=	literal;
		normModifier=	modifier;
		normPackage	= 	pack;
		normImport 	=	impo;

	}
}
