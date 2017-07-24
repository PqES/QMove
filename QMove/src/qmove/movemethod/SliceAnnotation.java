package qmove.movemethod;

import org.eclipse.jface.text.source.Annotation;

public class SliceAnnotation extends Annotation {
	public static final String EXTRACTION = "qmove.extractionAnnotation";

	public SliceAnnotation(String type, String text) {
		super(type, false, text);
	}
}
