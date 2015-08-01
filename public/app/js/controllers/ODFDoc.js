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

/* Controllers */

var odfeControllers = angular.module('odfeControllers');


odfeControllers.controller('ODFEDocCtrl', [ '$scope', '$routeParams', '$http',
		function($scope, $routeParams, $http) {

		// should look into say the gauges.json doc - or one of the others
		// and get the details of the run
	    // sources... mode - filters when we have them
		//
		// or we have the data in a run summary json doc
	    // that stuff which is in each of the underlying json files?
                        $scope.mode = $routeParams.mode;
                        $scope.odfName = $routeParams.odfName;
			$scope.extract = $routeParams.extract;

			$scope.clicked = function(src) {
				$scope.sel = src;
			}
		} ]);

