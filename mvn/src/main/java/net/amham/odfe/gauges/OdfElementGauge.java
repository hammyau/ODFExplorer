package net.amham.odfe.gauges;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;



public class OdfElementGauge {
	
	private List<Integer> hitList = new ArrayList<Integer>();
	
	private SortedMap <String, OdfAttributeGauge> attributesGaugeMap = new TreeMap <String, OdfAttributeGauge>();

	public OdfElementGauge(String name) {
		//hits = 0;
	}
	
	public void addAttribute(OdfAttributeGauge attrGauge) {
		attributesGaugeMap.put(attrGauge.getName(), attrGauge);
	}

	public Boolean hit(int ndx, String attribute) {
		Boolean found = false;
		if (attribute != null) {
			OdfAttributeGauge attrGauge = attributesGaugeMap.get(attribute);
			if (attrGauge != null) {
				attrGauge.hit(ndx);
				found = true;
			}
			else {
				//Log that an attribute name for this element was not found
			}
		}
		else {
			hit(ndx);
		}
		return found;
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

	public List<Integer> getHits() {
		return hitList;
	}

	public SortedMap<String, OdfAttributeGauge> getAttributesGaugeMap() {
		return attributesGaugeMap;
	}
	
	public void clear() {
		hitList.clear();
		for (String attribute : attributesGaugeMap.keySet()) {
			OdfAttributeGauge attrGauge = attributesGaugeMap.get(attribute);
			attrGauge.clear();
		}
	}

	//This is just the aggregation writing the total from the previous iteration
	public void setHits(int ndx, int val) {
		hitList.add(new Integer(val));
	}

}
