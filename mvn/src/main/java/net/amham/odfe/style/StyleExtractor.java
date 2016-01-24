/* 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package net.amham.odfe.style;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.odftoolkit.odfdom.dom.DefaultElementVisitor;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.OdfMetaDom;
import org.odftoolkit.odfdom.dom.OdfStylesDom;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeMetaElement;
import org.odftoolkit.odfdom.dom.element.style.StyleMasterPageElement;
import org.odftoolkit.odfdom.dom.element.text.TextHElement;
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextListElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.element.text.TextSElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.element.text.TextTabElement;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.common.EditableTextExtractor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class StyleExtractor extends DefaultElementVisitor {

	private final static   Logger LOGGER = Logger.getLogger(StyleExtractor.class.getName());
	protected static final char NewLineChar = '\n';
	protected static final char TabChar = '\t';
	OdfElement mElement;
	
	//will there always be a family?
	protected SortedMap<String, Integer> stylesMap = null;
	protected SortedMap<String, OdfStyle> autoStylesMap = null;
	Document mDocument = null;
	
	/**
	 * This class is used to provide the string builder functions to extractor.
	 * It will automatically process the last NewLineChar.
	 * 
	 * @since 0.3.5
	 */
	protected static class ExtractorStringBuilder {
		private StringBuilder mBuilder;
		private boolean lastAppendNewLine;

		ExtractorStringBuilder() {
			mBuilder = new StringBuilder();
			lastAppendNewLine = false;
		}

		/**
		 * Append a string
		 * 
		 * @param str
		 *            - the string
		 */
		public void append(String str) {
			mBuilder.append(str);
		}

		/**
		 * Append a character
		 * 
		 * @param ch
		 *            - the character
		 */
		public void append(char ch) {
			mBuilder.append(ch);
		}

		/**
		 * Append a new line character at the end
		 */
		public void appendLine() {
			mBuilder.append(NewLineChar);
			lastAppendNewLine = true;
		}

		/**
		 * Return the string value.
		 * <p>
		 * If the last character is a new line character and is appended with
		 * appendLine(), the last new line character will be removed.
		 */
		public String toString() {
			if (lastAppendNewLine) {
				mBuilder.deleteCharAt(mBuilder.length() - 1);
			}
			return mBuilder.toString();
		}
	}

	/**
	 * Return the text content of a element as String
	 * 
	 * @param ele
	 *            the ODF element
	 * @return the text content of the element
	 */
	/*public static synchronized SortedMap<String, Integer> getStyles(OdfElement ele) {
		StyleExtractor extractor = newOdfStyleExtractor(ele);
		return extractor.getStyles();
	}*/

	/**
	 * Create a TextExtractor instance using specified ODF element, which text
	 * content can be extracted by <code>getText()</code>.
	 * 
	 * @param element
	 *            the ODF element whose text will be extracted.
	 * @return an instance of TextExtractor
	 */
	public static StyleExtractor newOdfStyleExtractor(OdfElement element) {
		return new StyleExtractor(element);
	}

	/**
	 * Return the text content of specified ODF element as a string.
	 * 
	 * @return the text content as a string
	 */
	public void getStyles() {
		visit(mElement);
	}

	public SortedMap<String, OdfStyle> getmAutoStylesMap() {
		return autoStylesMap;
	}

	/**
	 * Default constructor
	 */
	protected StyleExtractor() {
	}

	/**
	 * Constructor with an ODF element as parameter
	 * 
	 * @param element
	 *            the ODF element whose text would be extracted.
	 */
	protected StyleExtractor(OdfElement element) {
		mElement = element;
	}

	/**
	 * The end users needn't to care of this method, if you don't want to
	 * override the text content handling strategy of <code>OdfElement</code>.
	 * 
	 * @see org.odftoolkit.odfdom.dom.DefaultElementVisitor#visit(org.odftoolkit.odfdom.pkg.OdfElement)
	 */
	@Override
	public void visit(OdfElement element) {
		getElementStyle(element);
	}

	/**
	 * The end users needn't to care of this method, if you don't want to
	 * override the text content handling strategy of text:p.
	 * 
	 * @see org.odftoolkit.odfdom.dom.DefaultElementVisitor#visit(org.odftoolkit.odfdom.dom.element.text.TextPElement)
	 */
	@Override
	public void visit(TextPElement ele) {
		getElementStyle(ele);
	}

	/**
	 * The end users needn't to care of this method, if you don't want to
	 * override the text content handling strategy of text:h.
	 * 
	 * @see org.odftoolkit.odfdom.dom.DefaultElementVisitor#visit(org.odftoolkit.odfdom.dom.element.text.TextHElement)
	 */
	@Override
	public void visit(TextHElement ele) {
		getElementStyle(ele);
	}

	public void visit(TextSpanElement ele) {
		getElementStyle(ele);
	}

	public void visit(TextListElement ele) {
		getElementStyle(ele);
	}

	/**
	 * Append the text content of this element to string buffer.
	 * 
	 * @param ele
	 *            the ODF element whose text will be appended.
	 */
	protected void getElementStyle(OdfElement ele) {
		if(ele.getNodeName().equals("text:p")) {
			String styleName = ((TextPElement) ele).getTextStyleNameAttribute();
			hitStyle(styleName);
			
			//build up a map of used automatic styles
			OdfStyle autoStyle = ((TextPElement) ele).getAutomaticStyle();
			if (autoStyle != null) {
				autoStylesMap.put(styleName,autoStyle);
			}
		}
		else if(ele.getNodeName().equals("text:list")) {
			//found the text list
			String styleName = ((TextListElement) ele).getTextStyleNameAttribute();
			hitStyle(styleName);
		}
		
		Node child = ele.getFirstChild();
		while (child != null) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				OdfElement element = (OdfElement) child;
				element.accept(this);
			}
			child = child.getNextSibling();
		}
	}

	/**
	 * We can also check if the paragraph(element) being processed
	 * requires a comment to be added to it
	 * 
	 * Return the comment and it can be added to the element
	 * 
	 * @param styleName
	 */
	public String hitStyle(String styleName) {
		if(styleName != null) {
			Integer hits = stylesMap.get(styleName);
			if (hits != null) {
				hits++;
			}
			else {
				hits = new Integer(1);
			}
			stylesMap.put(styleName, hits);
		}
		return styleName;
	}

	//this may not be needed really
	private String getStyleName(OdfStylableElement element) {
		String stylename = element.getStyleName();
		if (stylename == null) {
			if (element.getParentNode() instanceof OdfStylableElement) {
				getStyleName((OdfStylableElement) element.getParentNode());
			} else {
				stylename = "defaultstyle";
			}
		}
		return stylename;
	}
	
	public void dump() {
		List<String> styleNames = new ArrayList<String>();
		for(String name: stylesMap.keySet()) {
			styleNames.add("Style " + name + " hits " + stylesMap.get(name).toString()+ "\n");
		}
		LOGGER.fine(styleNames.toString());
		List<String> autoStyleNames = new ArrayList<String>();
		for(String name: autoStylesMap.keySet()) {
			autoStyleNames.add("AutoStyle " + name + " parent " + autoStylesMap.get(name).getStyleParentStyleNameAttribute() + " family " + autoStylesMap.get(name).getFamilyName() + "\n");
		}
		LOGGER.fine(autoStyleNames.toString());
	}

	public SortedMap<String, Integer> getStylesMap() {
		return stylesMap;
	}

	public void setStylesMap(SortedMap<String, Integer> stylesMap) {
		this.stylesMap = stylesMap;
	}

	public SortedMap<String, OdfStyle> getAutoStylesMap() {
		return autoStylesMap;
	}

	public void setAutoStylesMap(SortedMap<String, OdfStyle> autoStylesMap) {
		this.autoStylesMap = autoStylesMap;
	}


}
