package qmove.utils;

import java.util.ArrayList;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.corext.refactoring.structure.MoveInstanceMethodProcessor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;

import qmove.handlers.QMoveHanlder;
import qmove.persistence.MethodTargets;

@SuppressWarnings("restriction")
public class MoveMethodUtils {

	public static MethodTargets canMoveMethod(IMethod method) {

		ArrayList<String> validTargets;

		try {

			System.out.println("------------------------------------------------------------------------");
			System.out.print("Trying method " + method.getElementName() + "... ");
			if (method.isConstructor()) {
				System.out.println("is constructor!");
				return null;
			}

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();

			if (targets.length == 0 || targets == null) {
				System.out.println("Do not move to any place.");
				return null;
			}

			else {

				validTargets = new ArrayList<String>();

				System.out.println();

				for (int i = 0; i < targets.length; i++) {

					IVariableBinding candidate = targets[i];
					System.out.print("Destiny: " + candidate.getType().getName() + ": ");

					if (candidate.getType().isEnum() || candidate.getType().isInterface()
							|| candidate.getType().isGenericType()) {
						System.out.println("It is enumerated, interface or generic");
						continue;
					}

					if (candidate.getType().getQualifiedName()
							.equals(method.getDeclaringType().getFullyQualifiedName())) {
						System.out.println("Destination is the same class as the method already located");
						continue;
					}

					if (validTargets.contains(candidate.getType().getQualifiedName())) {
						System.out.println("This destination is already saved among the possible destinations");
						continue;
					}

					processor = new MoveInstanceMethodProcessor(method,
							JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

					processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

					processor.setTarget(candidate);
					processor.setInlineDelegator(true);
					processor.setRemoveDelegator(true);
					processor.setDeprecateDelegates(false);

					Refactoring ref = new MoveRefactoring(processor);
					RefactoringStatus status = null;

					status = ref.checkAllConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

					if (status.isOK()) {

						System.out.println("OK!");

						validTargets.add(candidate.getType().getQualifiedName());

					}

					else {
						System.out.println("Failed");
					}

				}
			}

			if (validTargets.size() > 0) {
				return new MethodTargets(method, validTargets);
			}

			else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

	public static ArrayList<IVariableBinding> getTargets(IMethod method, ArrayList<String> targetsNames) {
		try {
			// Array for already found targets for method
			ArrayList<IVariableBinding> arrayTargets = new ArrayList<IVariableBinding>();

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();

			// loop that fills arrayTargets
			for (IVariableBinding target : targets) {
				for (String targetDetected : targetsNames) {
					if (targetDetected.compareTo(target.getType().getQualifiedName()) == 0) {
						arrayTargets.add(target);
					}
				}
			}
			return arrayTargets;
		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static PerformChangeOperation moveCandidate(IMethod method, IVariableBinding target) {
		try {

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			processor.setTarget(target);
			processor.setInlineDelegator(true);
			processor.setRemoveDelegator(true);
			processor.setDeprecateDelegates(false);

			Refactoring refactoring = new MoveRefactoring(processor);
			refactoring.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			final CreateChangeOperation create = new CreateChangeOperation(
					new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
					RefactoringStatus.FATAL);

			PerformChangeOperation perform = new PerformChangeOperation(create);

			// move method
			ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());

			return perform;

		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void moveBestMethod(IMethod method, String bestTarget) {
		try {

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(method,
					JavaPreferencesSettings.getCodeGenerationSettings(method.getJavaProject()));

			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();

			for (IVariableBinding target : targets) {
				if (target.getType().getQualifiedName().compareTo(bestTarget) == 0) {
					processor.setTarget(target);
					processor.setInlineDelegator(true);
					processor.setRemoveDelegator(true);
					processor.setDeprecateDelegates(false);

					Refactoring refactoring = new MoveRefactoring(processor);
					refactoring.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

					final CreateChangeOperation create = new CreateChangeOperation(
							new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
							RefactoringStatus.FATAL);

					PerformChangeOperation perform = new PerformChangeOperation(create);

					ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());

					break;
				}
			}
		} catch (Exception e) {
			return;
		}
	}

	public static PerformChangeOperation moveMethodInOriginalProject(String method, String[] parameters,
			String target) {
		try {

			IMethod imethod = getIMethodFromOriginalProject(method, parameters);

			MoveInstanceMethodProcessor processor = new MoveInstanceMethodProcessor(imethod,
					JavaPreferencesSettings.getCodeGenerationSettings(imethod.getJavaProject()));
			processor.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			IVariableBinding[] targets = processor.getPossibleTargets();
			IVariableBinding candidate = null;
			for (IVariableBinding t : targets) {
				if (target.equals(t.getType().getQualifiedName())) {
					candidate = t;
					break;
				}
			}

			processor.setTarget(candidate);
			processor.setInlineDelegator(true);
			processor.setRemoveDelegator(true);
			processor.setDeprecateDelegates(false);

			Refactoring refactoring = new MoveRefactoring(processor);
			refactoring.checkInitialConditions(SingletonNullProgressMonitor.getNullProgressMonitor());

			final CreateChangeOperation create = new CreateChangeOperation(
					new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
					RefactoringStatus.FATAL);

			PerformChangeOperation perform = new PerformChangeOperation(create);

			// move method
			ResourcesPlugin.getWorkspace().run(perform, SingletonNullProgressMonitor.getNullProgressMonitor());

			return perform;

		} catch (OperationCanceledException | CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IMethod getIMethodFromOriginalProject(String method, String[] parameters) {

		try {

			IMethod[] methods = QMoveHanlder.projectOriginal.findType(method.substring(0, method.lastIndexOf("::")),
					SingletonNullProgressMonitor.getNullProgressMonitor()).getMethods();

			for (IMethod imethod : methods) {

				if (imethod.getElementName().equals(method.substring(method.lastIndexOf("::") + 2))) {

					if (imethod.getNumberOfParameters() == 0 && parameters.length == 0) {
						return imethod;
					}

					if (imethod.getNumberOfParameters() == parameters.length) {
						String[] parametersMethod = imethod.getParameterTypes();
						boolean todosBatem = true;
						for (int i = 0; i < imethod.getNumberOfParameters(); i++) {
							if (!parametersMethod[i].substring(0, parametersMethod[i].length() - 1)
									.equals(parameters[i])) {
								todosBatem = false;
							}
						}

						if (todosBatem) {

							return imethod;

						}

					}

				}
			}

			return null;

		} catch (JavaModelException e) {
			return null;
		}
	}

}
