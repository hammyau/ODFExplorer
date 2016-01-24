/************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * Copyright 2009, 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. You can also
 * obtain a copy of the License at http://odftoolkit.org/docs/license.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ************************************************************************/
package net.amham.odfe.generator;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.SAXParserFactory;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import schema2template.model.XMLModel;

import com.sun.msv.grammar.Expression;
import com.sun.msv.reader.trex.ng.RELAXNGReader;

/**
 * Generate ODF Gauge Information
 *
 */
public class OdfGauges {

	private static final Logger LOG = Logger.getLogger(OdfGauges.class.getName());
	public static final Boolean DEBUG = Boolean.FALSE;
	public static final String ODF12_RNG_FILE_NAME = "OpenDocument-v1.2-cs01-schema.rng";
//	public static final String ODFTOOLKIT_DIR = "./src/resources/";
	public static final String TEMPLATE_DIR = "./src/resources/templates";
	public static final String GENERATE_TO_DIR = "./src/main/java/net/amham/odfe/gauges/";
	public static String odf12RngFile;
	private static XMLModel mOdf12SchemaModel;
	private static Expression mOdf12Root;
		
	static {
		odf12RngFile = "./src/resources/odf-schemas/OpenDocument-v1.2-os-schema.rng";
	}

	public static void main(String[] args) throws Exception {
		Handler handler = new FileHandler("Gauges.log");
		handler.setFormatter(new SimpleFormatter());
		LOG.addHandler(handler);
        LOG.setLevel(Level.ALL);
		LOG.info("Generate Gauge Information");
		initialize();
		fillTemplates(null, mOdf12Root, null, null);
	}

	private static void initialize() throws Exception{
		// calling MSV to parse the ODF 1.2 RelaxNG, returning a tree
		mOdf12Root = loadSchema(new File(odf12RngFile));
		LOG.info("Loaded schema " + odf12RngFile);
		
		mOdf12SchemaModel = new XMLModel(mOdf12Root);
	}

	private static void fillTemplates(String sourceDir, Expression root, String outputRuleTemplate, String outputRuleFile) throws Exception {
		// intialising template engine (ie. Velocity)
		Properties props = new Properties();
		props.setProperty("file.resource.loader.path", TEMPLATE_DIR);
		VelocityEngine ve = new VelocityEngine(props);
		ve.init();

		createGauges(ve, "Gauges.vm", GENERATE_TO_DIR +"OdfGaugeStore.java");
		LOG.info("Gauges created done.");
	}

	/**
	 * Load and parse a Schema from File.
	 *
	 * @param rngFile
	 * @return MSV Expression Tree (more specific: The tree's MSV root expression)
	 * @throws Exception
	 */
	public static Expression loadSchema(File rngFile) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		// Parsing the Schema with MSV
		String absolutePath = rngFile.getAbsolutePath();
		com.sun.msv.reader.util.IgnoreController ignoreController = new com.sun.msv.reader.util.IgnoreController();
		Expression root = RELAXNGReader.parse(absolutePath,	factory, ignoreController).getTopLevel();

		if (root == null) {
			throw new Exception("Schema could not be parsed.");
		}
		return root;
	}

	private static void createGauges(VelocityEngine ve, String template, String output) throws Exception {
		VelocityContext context = new VelocityContext();
		context.put("model", mOdf12SchemaModel);
		FileWriter listout = new FileWriter(new File(output));
		String encoding = "utf-8";
		ve.mergeTemplate(template, encoding, context, listout);
		listout.close();
	}
}
