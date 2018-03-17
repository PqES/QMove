package qmove.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SingletonNullProgressMonitor {

	static private IProgressMonitor nullProgressMonitor = new NullProgressMonitor();

	private SingletonNullProgressMonitor() {

	}

	public static IProgressMonitor getNullProgressMonitor() {
		if (nullProgressMonitor == null)
			nullProgressMonitor = new NullProgressMonitor();
		return nullProgressMonitor;
	}
}