package qmove.ast;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ClassMethodVisitor extends ASTVisitor {
	private CompilationUnit fullClass;
	private ArrayList<IType> arrayType;
	private ArrayList<IMethod> arrayMethod;

	public ClassMethodVisitor(ICompilationUnit unit) throws JavaModelException {

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
	public boolean visit(TypeDeclaration node) {

		IType itype = (IType) node.resolveBinding().getJavaElement();
		if (arrayType == null) {
			arrayType = new ArrayList<IType>();
		}
		arrayType.add(itype);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		try {

			IMethod imeth = (IMethod) node.resolveBinding().getJavaElement();

			if (imeth.getDeclaringType().isAnonymous()) {
				return true;
			}

			if (arrayMethod == null) {
				arrayMethod = new ArrayList<IMethod>();
			}
			arrayMethod.add(imeth);

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return true;
	}

	public ArrayList<IMethod> getArrayMethod() {
		return arrayMethod;
	}

	public ArrayList<IType> getArrayTypes() {
		return arrayType;
	}

}
