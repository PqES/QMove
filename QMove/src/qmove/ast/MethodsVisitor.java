package qmove.ast;

import java.util.ArrayList;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;



public class MethodsVisitor extends ASTVisitor {
	

	private CompilationUnit fullClass;
	private ArrayList<MethodDeclaration> methods = null;
	
	
	public MethodsVisitor(ICompilationUnit unit) throws JavaModelException {
		
		
				
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(unit);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.setProject(unit.getJavaProject());
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			
			this.fullClass = (CompilationUnit) parser.createAST(null);// parse
			this.fullClass.accept(this);
	}
	
	
	
	@Override
	public boolean visit(MethodDeclaration node) {
		
		//IMethod imeth = (IMethod) node.resolveBinding().getJavaElement();
		
		//MethodObjects.getInstance().insertMapping(imeth, node);
		
		if(methods==null){
			methods = new ArrayList<MethodDeclaration>();
		}
		
		methods.add(node);
		return true;
	}
	
	public ArrayList<MethodDeclaration> getMethods(){
		return methods;
	}
}