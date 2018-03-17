package qmove.utils;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ViewUtils {

	public static void hideView() {
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		// Find desired view :
		IViewPart myView = wp.findView("qmove.views.QMoveView");

		// Hide the view :
		wp.hideView(myView);

	}

	public static void openView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("qmove.views.QMoveView");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
