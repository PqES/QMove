package qmove.metrics.qmood.properties;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class DataAccessMetrics {

	public static double calcule(IType type) {
		double dam;
		try {
			IField[] fields = type.getFields();
			int stats = 0;
			int inst = 0;
			int publicAtr = 0;

			if (fields.length > 0) {
				for (IField field : fields) {
					if ((field.getFlags() & Flags.AccStatic) != 0)
						stats++;
					else {
						inst++;
						if (Flags.isPublic(field.getFlags()))
							publicAtr++;
					}

				}
			}

			double totalFields = inst + stats;
			double totalNonPublicFields = inst + stats - publicAtr;
			dam = totalFields > 0 ? totalNonPublicFields / totalFields : 0.0;

		} catch (JavaModelException e) {
			dam = 0;
		}
		return dam;
	}
}
