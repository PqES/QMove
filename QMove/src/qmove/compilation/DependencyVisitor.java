package qmove.compilation;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
//import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
//import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
//import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
//import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
//import org.eclipse.jdt.core.dom.FieldAccess;
//import org.eclipse.jdt.core.dom.FieldDeclaration;
//import org.eclipse.jdt.core.dom.IBinding;
//import org.eclipse.jdt.core.dom.ITypeBinding;
//import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
//import org.eclipse.jdt.core.dom.MethodInvocation;
//import org.eclipse.jdt.core.dom.Name;
//import org.eclipse.jdt.core.dom.ParameterizedType;
//import org.eclipse.jdt.core.dom.PrimitiveType;
//import org.eclipse.jdt.core.dom.QualifiedName;
//import org.eclipse.jdt.core.dom.SimpleType;
//import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
//import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
//import org.eclipse.jdt.core.dom.Type;
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import qmove.movemethod.MethodObjects;


public class DependencyVisitor extends ASTVisitor {
	//private List<Object[]> dependencies;

	private CompilationUnit fullClass;
	private String className;
	private ArrayList<IMethod> arrayMethod;
	private Map<String, ArrayList<IMethod>> mpMethods;
	
	
	public DependencyVisitor(ICompilationUnit unit) throws JavaModelException {
		
		this.arrayMethod = new ArrayList<IMethod>();
		this.mpMethods =  new HashMap<String, ArrayList<IMethod>>();
		
		this.className = unit.getParent().getElementName() + "." + unit.getElementName().substring(0, unit.getElementName().length() - 5);
		mpMethods.put(this.className, null);
		
		//if(!unit.isOpen()){
				
			ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
			// is now deprecated
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(unit);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.setProject(unit.getJavaProject());
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			
			this.fullClass = (CompilationUnit) parser.createAST(null);// parse
			this.fullClass.accept(this);
		/*}else{
		
			IType[] allTypes = unit.getAllTypes();
				for(IType type : allTypes){
					IMethod[] allMethods = type.getMethods();
					for(IMethod method : allMethods){
						arrayMethod.add(method);
					}
				}
			
			mpMethods.put(this.className, new ArrayList<IMethod>(arrayMethod));
			arrayMethod.clear(); /*desenecessario..to fazendo gra�a
		}*/
	}
	
	public Map<String,ArrayList<IMethod>> getMapMethods(){
		return mpMethods;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		
		IMethod imeth = (IMethod) node.resolveBinding().getJavaElement();
		
		MethodObjects.getInstance().insertMapping(imeth, node);
		
		arrayMethod = mpMethods.get(className);
		if(arrayMethod==null){
			arrayMethod = new ArrayList<IMethod>();
		}
		arrayMethod.add(imeth);
		mpMethods.put(className, new ArrayList<IMethod>(arrayMethod));
		arrayMethod.clear(); /*desenecessario..to fazendo gra�a*/
		return true;
	}
}