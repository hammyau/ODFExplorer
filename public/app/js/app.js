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
'use strict';

/* App Module */

var odfeApp = angular.module('odfeApp', [ 'ngRoute',
		'odfeControllers', 'odfeFilters', 'd3' ]);

odfeApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/odfs', {
		templateUrl : 'partials/odf-mode.html',
		controller : 'ODFModeCtrl'
    }).when('/mode/:mode', {
        templateUrl : 'partials/odf-list.html',
        controller : 'ODFListCtrl'
        }).when('/run/:mode/:odfName', {
                templateUrl : 'partials/odferuns-list.html',
                controller : 'ODFERunsListCtrl'
	}).when('/doc/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfedoc.html',
		controller : 'ODFEDocCtrl'
	}).when('/gauges/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfegauges.html',
		controller : 'ODFEGaugesCtrl'
	}).when('/progress/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfegaugesProgress.html',
		controller : 'ODFEGaugesProgressCtrl'
	}).when('/styles/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfestyles.html',
		controller : 'ODFEStylesCtrl'
	}).when('/paths/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfepaths.html',
		controller : 'ODFEPathsCtrl'
	}).when('/xpaths/:mode/:odfName/:extract', {
		templateUrl : 'partials/odfexpaths.html',
		controller : 'ODFXPathGraphCtrl'
	}).when('/input', {
		templateUrl : 'partials/odfeinput.html',
		controller : 'ODFEInputCtrl'
	}).otherwise({
		redirectTo : '/odfs'
	});
} ]);
