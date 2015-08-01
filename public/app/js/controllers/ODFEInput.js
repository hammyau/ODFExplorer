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


odfeControllers.controller('ODFEInputCtrl', ['$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
	$scope.mode = "Singles";
	$scope.depth = "all";
	$scope.angFiles = [];
	$scope.trgFiles = [];
        $scope.aggregates = [];
        $http.get('records/Aggregations/docs.json').success(function(data) {
            $scope.aggregates = data.docs;
        });

    $scope.filechanged= function(element){
    	$scope.angFiles = [];
    	$scope.$apply(function() {
	        for (var i = 0; i < element.files.length; i++) {
	            $scope.angFiles.push(element.files[i].name);
	        }
    	});
    };

    $scope.trgfilechanged= function(element){
    	$scope.trgFiles = [];
    	$scope.$apply(function() {
	        for (var i = 0; i < element.files.length; i++) {
	            $scope.trgFiles.push(element.files[i].name);
	        }
    	});
    };

    $scope.submitable = function() {
      var retval = false;
      return retval;
    };
}]);
