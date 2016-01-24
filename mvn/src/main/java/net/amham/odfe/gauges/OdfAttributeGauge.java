package net.amham.odfe.gauges;

import java.util.ArrayList;
import java.util.List;

public class OdfAttributeGauge {
	
	private String name;
	private List<Integer> hitList = new ArrayList<Integer>();
	private Boolean mandatory;

	public OdfAttributeGauge(String name, Boolean mand) {
		this.name = name;
		mandatory = mand;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer>  getHits() {
		return hitList;
	}

	public void hit(int ndx) {
		Integer val = null;

		if (hitList.size() > ndx) { //hitting an existing array element
			val = hitList.get(ndx);
			val++;
			hitList.set(ndx, val);
		} else { 
			int listSize = hitList.size(); // need to create elements
			while (ndx >= listSize) {
				val = new Integer(0);
				hitList.add(listSize++, val);
			}
			if(val == null) {
				System.out.println("Value to set into hitlist");
			}
			else {
				val++;
				hitList.set(ndx, val);
			}
		}
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void clear() {
		hitList.clear();
	}

	/**
	 * This is the same as the ElementGauge function
	 * 
	 * TODO Refactor to create a common gauge array class ... or something
	 * @param ndx
	 * @param val
	 */
	public void setHits(int ndx, int val) {
		if (hitList.size() > ndx) {
			Integer value = hitList.get(ndx);
			value = val;
			hitList.set(ndx, value);
		} else { 
			int newNdx = 0;
			while (hitList.size() <= ndx) {
				hitList.add(newNdx++, new Integer(0));
			}
			hitList.set(ndx, val);
		}	
	}

}
