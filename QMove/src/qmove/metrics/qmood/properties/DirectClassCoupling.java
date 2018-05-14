package qmove.metrics.qmood.properties;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class DirectClassCoupling {

	public static double calcule(IType iType) {
		double dcc;
		Set<String> tipos = new HashSet<String>();
		TypeDeclaration type = getASTNode(iType);
		if (type != null) {
			FieldDeclaration[] fields = type.getFields();
			MethodDeclaration[] methods = type.getMethods();
			for (FieldDeclaration field : fields) {
				tipos.addAll(getSourceTypeNames(field.getType().resolveBinding()));
			}
			try{
				for (MethodDeclaration method : methods) {
					for (ITypeBinding binding : method.resolveBinding().getParameterTypes()) {
						tipos.addAll(getSourceTypeNames(binding));
					}
				}
			} catch(NullPointerException e){
				dcc = tipos.size();
				return dcc;
			}
		}

		dcc = tipos.size();
		return dcc;
	}

	private static TypeDeclaration getASTNode(IType type) {
		CompilationUnit astNode = null;
		astNode = getAST(type);
		ClassTypeFinder cf = new ClassTypeFinder(type);
		astNode.accept(cf);
		return cf.getTypeDeclaration();
	}

	private static CompilationUnit getAST(IType type) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(type.getCompilationUnit());
			return (CompilationUnit) parser.createAST(null);
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Set<String> getSourceTypeNames(ITypeBinding type) {
		Set<String> nomes = new HashSet<String>();

		if (type.isFromSource()) {
			nomes.add(type.getQualifiedName());
		}

		// A[] returns if A is from Source
		if (type.isArray() && type.getElementType().isFromSource())
			nomes.add(type.getElementType().getQualifiedName());

		// T<A>, T<A,B> - true if A is from Source or if A or B are source types
		// and its a Collection
		// Collections with source as parameters are considered because
		// represents a relationship with a source type
		if (type.isParameterizedType()) {
			ITypeBinding[] interfaces = type.getInterfaces();
			boolean isCollection = false;
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}

			if (isCollection) {
				for (ITypeBinding typeArg : type.getTypeArguments()) {
					if (typeArg.isFromSource())
						nomes.add(typeArg.getQualifiedName());
				}
			}
		}
		return nomes;
	}
}

class ClassTypeFinder extends ASTVisitor {
	IType source;
	TypeDeclaration type;

	ClassTypeFinder(IType source) {

		this.source = source;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		type = node;
		return true;
	}

	public TypeDeclaration getTypeDeclaration() {
		return type;
	}
}