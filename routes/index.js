/*
 * Copyright 2015 Ian Cunningham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.Copyright [yyyy] [name of copyright owner]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/index', function(req, res) {
  res.render('index', { title: 'Express' });
});

router.get('/whatisthis', function(req, res) {
	  res.render('whatisodfe', { title: 'What is the ODF Explorer?' });
	});

router.get('/whatisthisAggregations', function(req, res) {
	  res.render('whatisaggs', { title: 'Aggregation Reports' });
	});

router.get('/whatisthisSingles', function(req, res) {
	  res.render('whatissingles', { title: 'Single Document Reports' });
	});

router.get('/whatisthisComparisons', function(req, res) {
	  res.render('whatiscomparisons', { title: 'Comparison Reports' });
	});

router.get('/whatcaniseeComparisons', function(req, res) {
	  res.render('seecomparisons', { title: 'Comparison Reports' });
	});

router.get('/whatcaniseeAggregations', function(req, res) {
	  res.render('seeaggs', { title: 'Aggregation Runs' });
	});

router.get('/whatcaniseeSingles', function(req, res) {
	  res.render('seesingles', { title: 'Single Document Runs' });
	});

router.get('/extractselection', function(req, res) {
	  res.render('extractselection', { title: 'Extract Run' });
	});

router.get('/namespaces', function(req, res) {
	  res.render('namespaces', { title: 'Namespace Gauges' });
	});

router.get('/stylefamilies', function(req, res) {
	  res.render('stylefamilies', { title: 'Style Families' });
	});

router.get('/xpathtable', function(req, res) {
	  res.render('xpathtable', { title: 'XPath Table' });
	});

router.get('/xpathgraph', function(req, res) {
	  res.render('xpathgraph', { title: 'XPath Graph' });
	});

router.get('/readme', function(req, res) {
  res.render('readme', { title: 'Read Me' });
});

router.get('/runit', function(req, res) {
	  res.render('howToRun', { title: 'How To Run It' });
	});

router.get('/selections', function(req, res) {
	  res.render('selections', { title: 'What are my options?' });
	});

module.exports = router;
