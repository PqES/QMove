package qmove.moveclass;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

public class MCRecommendation {
	
	private IType clazz;
	private IPackageFragment target;
	double increase;
	
	public MCRecommendation(IType clazz, IPackageFragment target,  double increase){
		this.clazz = clazz;
		this.target = target;
		this.increase = increase;
	}

	public IType getClazz() {
		return clazz;
	}

	public double getIncrease() {
		return increase;
	}
	
	public IPackageFragment getTarget(){
		return target;
	}

	
}
