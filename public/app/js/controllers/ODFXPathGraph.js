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



odfeControllers.controller('ODFXPathGraphCtrl', ['$scope', '$routeParams', '$http',
    function($scope, $routeParams, $http) {
                $scope.modeIn = $routeParams.mode;
		$scope.doc = $routeParams.odfName;
    $scope.sources = [];
		$scope.extract = $routeParams.extract;

                $http.get('records/' + $scope.modeIn + '/' + $scope.doc + '/' + $scope.extract + '/xpath.json').
                success(function(data) {
                  $scope.sources = data.children[0].children;
                  $scope.runs = data.runs;
                  $scope.mode = data.mode;
                  $scope.numpaths = data.numPaths;
                  $scope.maxdepth = data.maxDepth;
                  $scope.mindepth = data.minDepth;
                  $scope.avgdepth = data.avgDepth;
                  $scope.filters = [];
                  for(var i=0; i<data.filters.length; i++){
                      var value = data.filters[i];
                      console.log("filter " + value);
                      for(var n in value) {
                       var id = value[n];
                        var filter = {}
                        filter["name"] = n;
                        filter["id"] = id;
                        $scope.filters.push(filter);
                      };
                  };
                  $scope.filterString = $scope.genFilterString();
                }).
                error(function(data, status, headers, config) {
                        alert("http error " + status + " reading " + 'records/'  + $scope.modeIn + '/' + $scope.doc + '/' + $scope.extract + '/xpath.json')
                });


		$scope.svgDoc = "records/" + $scope.modeIn + '/' + $scope.doc + "/" + $scope.extract + "/xpath.dot.svg";
		$scope.mode = "To be set";
		$scope.numpaths = "To be set";
		$scope.maxdepth = "To be set";
		$scope.mindepth = "To be set";
		$scope.avgdepth = "To be set";
                $scope.filters = [];
                $scope.filterIDs = "";
                $scope.filterString = "";

                $scope.ngnodeClick = function(name, nodeID) {
                  $scope.$apply(function() {
                    var filter = {}
                    filter["name"] = name;
                    filter["id"] = nodeID;
                    $scope.filters.push(filter);
                    $scope.filterString = $scope.genFilterString();
                  });
                };

                $scope.forgetMe = function(f) {
                    $scope.filters.splice(f,1);
                    $scope.filterString = $scope.genFilterString();
                };

                $scope.genFilterString = function() {
                    var retStr = "";
                    for(var i=0; i<$scope.filters.length; i++) {
                      var filter = $scope.filters[i];
                      if(i==0) {
                        retStr = filter.id;
                      } else {
                        retStr += ","+filter.id;
                      }
                    }
                    return retStr;
                };

		}]);

function nodeClick(name, nodeID) {
  angular.element(document.getElementById("svgDiv")).scope().ngnodeClick(name, nodeID);
};
