package net.amham.odfe.report;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.logging.Logger;

import net.amham.odfe.gauges.OdfElementGauge;
import net.amham.odfe.gauges.OdfGaugeStore;

public class ReportBase {

	public enum ProcessMode {
		SINGLE,AGGREGATE,DIFF
	}

	protected static final String GAUGES_EXT = "odfegauges.json";
	protected static final String METAFILE = "meta.xml";
	protected static final String PATHSJSONFILE = "xpath.json";
	protected static final String PATHSXMLFILE = "paths.xml";
	protected static final String PATHSXSLFILE = "odfePathsTree.xsl";
	protected static final Logger LOGGER = Logger.getLogger(ReportBase.class.getName());
	
	protected OdfGaugeStore odfGauges = null;
	
	protected ProcessMode processMode = ProcessMode.SINGLE;


	public ReportBase() {

	}
	protected Iterator<String> getNamespacesIterator() {
		return odfGauges.getNamespaceGaugeMap().keySet().iterator();
	}
	
	/** 
	 * An Aggregation summary needs to get the gauges
	 * Getting tires this is a hack - there must be a better way than this...
	 * @param gauges
	 */
	public void setGauges(OdfGaugeStore gauges) {
		System.out.println("set gauges " + gauges.getName());
		odfGauges = gauges;
	}
}