package qmove.metrics.qmood.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class MeasureOfAggregation {

	public static double calcule(IType type) {
		double moa;
		try {
			IField[] fields = type.getFields();

			int sourceField = 0;

			if (fields.length > 0) {
				Map<IField, FieldDeclaration> fieldsDeclarations = getFieldsDeclarations(fields, type);
				for (IField field : fields) {
					if (fieldsDeclarations.get(field) != null && isFieldFromSource(fieldsDeclarations.get(field)))
						sourceField++;

				}

			}

			moa = sourceField;
			return moa;

		} catch (JavaModelException e) {
			moa = 0;
			return moa;
		}
	}

	private static Map<IField, FieldDeclaration> getFieldsDeclarations(IField[] fields, IType type) {
		Map<IField, FieldDeclaration> map = new HashMap<IField, FieldDeclaration>();
		List<FieldDeclaration> campos = new ArrayList<FieldDeclaration>();
		ASTNode node = getASTNode(type);
		if (node instanceof TypeDeclaration) {
			TypeDeclaration td = (TypeDeclaration) node;
			campos.addAll(Arrays.asList(td.getFields()));
		}

		if (node instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration acd = (AnonymousClassDeclaration) node;
			for (Object bd : acd.bodyDeclarations()) {
				if (bd instanceof FieldDeclaration)
					campos.add((FieldDeclaration) bd);
			}
		}

		if (node instanceof EnumDeclaration) {
			EnumDeclaration acd = (EnumDeclaration) node;
			for (Object bd : acd.bodyDeclarations()) {
				if (bd instanceof FieldDeclaration)
					campos.add((FieldDeclaration) bd);
			}
		}

		for (FieldDeclaration fd : campos) {
			for (IField field : fields) {
				for (Object fragment : fd.fragments()) {
					if (fragment instanceof VariableDeclarationFragment) {
						if (((VariableDeclarationFragment) fragment).getName().toString()
								.equals(field.getElementName()))
							map.put(field, fd);
					}
				}
			}
		}
		return map;
	}

	private static ASTNode getASTNode(IType type) {
		CompilationUnit astNode = null;
		astNode = getAST(type);
		ClassTypeFind cf = new ClassTypeFind(type);
		astNode.accept(cf);
		return cf.getNode();
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

	public static boolean isFieldFromSource(FieldDeclaration fd) {
		Type t = fd.getType();
		if (t.isSimpleType())
			return t.resolveBinding().isFromSource();

		// A[] returns if A is from Source
		if (t.isArrayType()) {
			ArrayType at = (ArrayType) t;
			if (at.getElementType().isSimpleType() || at.getElementType().isQualifiedType())
				return at.getElementType().resolveBinding().isFromSource();
		}

		// T<A>, T<A,B> - true if A is from Source or if A or B are source
		// types.
		// Collections with source as parameters are considered because
		// represents a relationship with a source type
		if (t.isParameterizedType()) {
			ITypeBinding tb = t.resolveBinding();
			ITypeBinding[] interfaces = tb.getInterfaces();
			boolean isCollection = false;
			for (ITypeBinding iTypeBinding : interfaces) {
				isCollection = isCollection || iTypeBinding.getBinaryName().equals("java.util.Collection");
			}
			if (isCollection) {
				for (ITypeBinding typeArg : tb.getTypeArguments()) {
					if (typeArg.isFromSource())
						return true;
				}
			}
		}

		return false;
	}
}

class ClassTypeFind extends ASTVisitor {
	IType source;
	ASTNode type;

	ClassTypeFind(IType source) {

		this.source = source;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		type = node;
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		type = node;
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		type = node;
		return true;
	}

	public ASTNode getNode() {
		return type;
	}

}