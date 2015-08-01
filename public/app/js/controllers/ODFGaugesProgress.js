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


odfeControllers.controller('ODFEGaugesProgressCtrl', ['$scope', '$routeParams', '$http',  'd3Service', 'nsSummary',
  function($scope, $routeParams, $http,  d3Service, nsSummary) {

    $scope.modeIn = $routeParams.mode;
    $scope.doc = $routeParams.odfName;
    $scope.sources = [];
    $scope.extract = $routeParams.extract;
    $scope.mode = "To be set";
    $scope.namespaces = [];
    $scope.nsSum = nsSummary.getSummary();

    d3Service.d3().then(function(d3) {

     d3.tip = d3Tip;

      var i = 0, root;
      var margin = {top: 80, right: 80, bottom: 80, left: 80};
      var w = 960 - margin.left - margin.right;
      var h = 500 - margin.top - margin.bottom;
      var src = null;
      var sources;
      var numSources = null;
      var hitsOnly = false;
      var mode = null;
      var ns = "";


      var vis = d3.select("#chart").append("svg:svg")
      	      .attr("width", w + margin.left + margin.right)
	      .attr("height", h + margin.top + margin.bottom)
	      .append("g")
	      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
/*	  xScale = d3.scale.linear().range([0, w]).domain([2000,2010]),
	  yScale = d3.scale.linear().range([0, h]).domain([134,215]),
	  xAxis = d3.svg.axis().scale(xScale),
	  yAxis = d3.svg.axis().scale(yScale);*/

      var jsonfile = "records/" + $scope.modeIn + '/' + $scope.doc + "/" + $scope.extract + "/odfegauges.json";

      var tip = d3.tip();
      tip.attr('class', 'd3-tip')
          .offset([-10, 0]);
//          .html(function(d) {
//           return "<strong>Frequency:</strong> <span style='color:red'>" + d + "</span>";
//          });

      vis.call(tip);




      d3.json(jsonfile, function(json) {
        json.x0 = 0;
        json.y0 = 0;
        root = json;
        //Can get the summary information here
        sources = root.children[0];
        var gauges = root.children[1];
        var namespaces = gauges._children[0];

        $scope.$apply(function() {
            $scope.namespaces = root.summary;
            $scope.sources = root.children[0].children;
        });

        update(root);
      });

      function update(source) {

          src = root.children[0];
          mode = root.mode; //just get it from there
          $scope.$apply(function() {
            $scope.mode = mode;
          });

	  vis.append("clipPath")
	      .attr("id", "clip")
	      .append("rect");

	  //lets manually get the text ns
	  var formatFixed = d3.format("f");
	  var textEls = $scope.nsSum.elementsHit;
	  var attrEls = $scope.nsSum.attrsHit;
	  var x = d3.scale.linear().range([0, w]).domain([1, textEls.length]);
	  var xAxis = d3.svg.axis().scale(x).orient("bottom").ticks(textEls.length).tickFormat(formatFixed);

	  var formatPercent = d3.format("%");
	  var y = d3.scale.linear().range([h,0]).domain([0, (d3.max(textEls)/$scope.nsSum.elements)]);
	  var yAxis = d3.svg.axis().scale(y).orient("left").tickFormat(formatPercent);

	  var xAxisGroup = vis.append("svg:g")
	        .attr("class", "x axis")
		.attr("transform", "translate(0," + h + ")").call(xAxis);
	  var yAxisGroup = vis.append("svg:g")
	        .attr("class", "Y axis")
		.call(yAxis);

	  var line = d3.svg.line()
	      .interpolate("linear")
	      .x(function(d,i) { return x(i+1); })
	      .y(function(d) { return y(d/$scope.nsSum.elements); });

	  var attrline = d3.svg.line()
	      .interpolate("linear")
	      .x(function(d,i) { return x(i+1); })
	      .y(function(d) { return y(d/$scope.nsSum.attributes); });

	  vis.append("path")
	      .attr("class", "line")
	      .attr("d", line(textEls));
	  vis.append("path")
	      .attr("class", "aline")
	      .attr("d", attrline(attrEls));

	  vis.append("text")      // text label for the x axis
	      .attr("x", w/2 )
	      .attr("y", h+(margin.bottom/2) )
	      .attr("stroke", "black")
	      .style("text-anchor", "middle")
	      .text($scope.nsSum.ns + " iterations");

	  vis.append("text")      // text label for the x axis
	      .attr("x", w/4 )
	      .attr("y", h+(margin.bottom/2) )
	      .attr("stroke", "blue")
	      .style("text-anchor", "middle")
	      .text("elements");
	  vis.append("text")      // text label for the x axis
	      .attr("x", 3*(w/4) )
	      .attr("y", h+(margin.bottom/2) )
	      .attr("stroke", "red")
	      .style("text-anchor", "middle")
	      .text("attributes");
      };
    });
}]);
