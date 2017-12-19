package qmove.moveclass;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import qmove.utils.SingletonNullProgressMonitor;

public class MoveClass {
	
	private PerformChangeOperation perform;

	public void performMoveClassRefactoring(IType origem, IPackageFragment destino) {
		RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.MOVE);
		MoveDescriptor descriptor = (MoveDescriptor) contribution.createDescriptor();
		descriptor.setMoveResources(new IFile[0], new IFolder[0],new ICompilationUnit[] { origem.getCompilationUnit() });
		descriptor.setDestination(destino);
		descriptor.setUpdateReferences(true);
		try {
			RefactoringStatus status = new RefactoringStatus();
			Refactoring refactoring = descriptor.createRefactoring(status);
			refactoring.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());
			refactoring.checkFinalConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			final CreateChangeOperation create = new CreateChangeOperation(
					new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
					RefactoringStatus.FATAL);
			perform = new PerformChangeOperation(create);
			ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());
			

		} catch (CoreException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void undoMoveClassRefactoring(){
		
		try {
			Change change = perform.getUndoChange();
			change.perform(SingletonNullProgressMonitor.getNullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
