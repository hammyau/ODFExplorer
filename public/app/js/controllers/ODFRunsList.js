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

var odfeControllers = angular.module('odfeControllers');


odfeControllers.controller('ODFERunsListCtrl', ['$scope', '$routeParams', '$http',
                function($scope, $routeParams, $http) {

		$scope.odfName = $routeParams.odfName;
                $scope.mode = $routeParams.mode;
		//we could/should make this a service?
                $http.get('records/' + $routeParams.mode + '/' + $routeParams.odfName + '/odferuns.json').
                success(function(data) {
                	$scope.runs = data.runs;

                }).
                error(function(data, status, headers, config) {
                	alert("http error " + status + " reading " + 'records/' + $routeParams.mode + '/' +$routeParams.odfName + '/odferuns.json')
                });

                $scope.tooltip = function(){
                	return "Attributes on";
                }
}]);

