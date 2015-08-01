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

odfeControllers.controller('ODFEStylesCtrl', ['$scope', '$routeParams', '$http',  'd3Service',
          function($scope, $routeParams, $http,  d3Service) {

                        $scope.modeIn = $routeParams.mode;
	   		$scope.doc = $routeParams.odfName;
	   		$scope.extract = $routeParams.extract;
	   		$scope.mode = "To be set";

			d3Service.d3().then(function(d3) {

	   		var w = 960, h = 3000, i = 0, barHeight = 20, barWidth = w * .8, duration = 200, root;

	   		var src = null;
	   		var mode = null;
	   		var diff = null;

	   		var tree = d3.layout.tree().size([ h, 100 ]);

	   		var diagonal = d3.svg.diagonal().projection(function(d) {
	   			return [ d.y, d.x ];
	   		});

	   		var vis = d3.select("#chart").append("svg:svg").attr("width", w).attr("height",
	   				h).append("svg:g").attr("transform", "translate(20,30)");

			var jsonfile = "records/" + $scope.modeIn + '/' +$scope.doc + "/" + $scope.extract + "/odfestyles.json";

	   		d3.json(jsonfile, function(json) {
	   			json.x0 = 0;
	   			json.y0 = 0;
	   			update(root = json);
	   		});

	   		var hitcolours = {
	   		  'diffCollapsed'    : 'red',
	   		  'diffExpanded'     : 'lightpink',
	   		  'missedCollapsed'  : '#3182bd',
	   		  'missedExpanded'   : 'lightblue',
	   		  'partialCollapsed' : '#FFEB03',
	   		  'partialExpanded'  : '#FAF07F',
	   		  'allCollapsed'     : '#11AB00',
	   		  'allExpanded'      : '#1CFF03',
	   		  'attribute'        : '#fd8d3c'
	   		};

	   		function update(source) {

	   			mode = root.mode;
	            $scope.$apply(function() {
	            	$scope.mode = mode;
	            });
	   			// Compute the flattened node list. TODO use d3.layout.hierarchy.
	   			var nodes = tree.nodes(root);

	   			// Compute the "layout".
	   			nodes.forEach(function(n, i) {
	   				n.x = i * barHeight;
	   			});

	   			// Update the nodes…
	   			var node = vis.selectAll("g.node").data(nodes, function(d) {
	   				return d.id || (d.id = ++i);
	   			});

	   			var nodeEnter = node.enter().append("svg:g").attr("class", "node").attr(
	   					"transform", function(d) {
	   						return "translate(" + source.y0 + "," + source.x0 + ")";
	   					}).style("opacity", 1e-6);

	   			// Enter any new nodes at the parent's previous position.
	   			nodeEnter.append("svg:rect").attr("y", -barHeight / 2).attr("height",
	   					barHeight).attr("width", barWidth).style("fill", color).on("click",
	   					click);

	   			nodeEnter.append("svg:text").attr("dy", 3.5).attr("dx", 5.5).text(
	   					function(d) {
	   						var txt;
	   						if (d.name == null) {
	   							txt = "Unknown";
	   						} else {
	   							txt = d.name;
	   							if (d.value != null) {
	   								txt += " : " + d.value;
	   							}
	   							if (d.value1 != null) {
	   								txt += " : " + d.value1 + " ---> " + d.value2;
	   							}
	   							if (d.hits != null) {
	   								txt += " hits : " + d.hits;
	   							}
	   						}
	   						if (d.totalHits) {
	   							txt += " : total hits = " + d.totalHits;
	   						}
	   						if(d._children) {
	   							txt += " (" + deepLength(d) + ")"
	   						}
	   						if(d.children) {
	   							txt += " (" + deepLength(d) + ")"
	   						}
	   						return txt;
	   					});

	   			// Transition nodes to their new position.
	   			nodeEnter.transition().duration(duration).attr("transform", function(d) {
	   				return "translate(" + d.y + "," + d.x + ")";
	   			}).style("opacity", 1);

	   			node.transition().duration(duration).attr("transform", function(d) {
	   				return "translate(" + d.y + "," + d.x + ")";
	   			}).style("opacity", 1).select("rect").style("fill", color);

	   			// Transition exiting nodes to the parent's new position.
	   			node.exit().transition().duration(duration).attr("transform", function(d) {
	   				return "translate(" + source.y + "," + source.x + ")";
	   			}).style("opacity", 1e-6).remove();

	   			// Update the links…
	   			var link = vis.selectAll("path.link").data(tree.links(nodes), function(d) {
	   				return d.target.id;
	   			});

	   			// Enter any new links at the parent's previous position.
	   			link.enter().insert("svg:path", "g").attr("class", "link").attr("d",
	   					function(d) {
	   						var o = {
	   							x : source.x0,
	   							y : source.y0
	   						};
	   						return diagonal({
	   							source : o,
	   							target : o
	   						});
	   					}).transition().duration(duration).attr("d", diagonal);

	   			// Transition links to their new position.
	   			link.transition().duration(duration).attr("d", diagonal);

	   			// Transition exiting nodes to the parent's new position.
	   			link.exit().transition().duration(duration).attr("d", function(d) {
	   				var o = {
	   					x : source.x,
	   					y : source.y
	   				};
	   				return diagonal({
	   					source : o,
	   					target : o
	   				});
	   			}).remove();

	   			// Stash the old positions for transition.
	   			nodes.forEach(function(d) {
	   				d.x0 = d.x;
	   				d.y0 = d.y;
	   			});
	   		}

	   		// Toggle children on click.
	   		function click(d) {
	   			if (d.children) {
	   				d._children = d.children;
	   				d.children = null;
	   			} else {
	   				d.children = d._children;
	   				d._children = null;
	   			}
	   			update(d);
	   		}

	   		//here we can catch the node and figure out what its colour should be
	   		//and initialise its children
	   		// need to do something different for styles
//	   			if current node hits 0  or > 0 is probably allwe care about
//	   			diff mode different
	   		//
	   		//
	   		function color(d) {
	   			var clr = hitcolours.missedCollapsed;
	   			var diff = false;
	   			if (mode == 'DIFF') { // look for diffs
	   			    if(d.value1 != null) {
	   				clr = "red";
	   			    }
	   			    else if(d.diff == "ChangedStyle") {
	   				    clr = "red";
	   				    if(d.children) {
	   					    clr = "lightpink";
	   				    }
	   				    diff = true;
	   			    }
	   			    else if(d.diff == "NewStyle") {
	   				    clr = "#11AB00";
	   				    if(d.children) {
	   					    clr = "#1CFF03";
	   				    }
	   				    diff = true;
	   			    }
	   			}

	   			if (diff == false) {
            var hits = 0;
            if(d.totalHits) {
              hits += d.totalHits;
            }
            if(d.hits) {
              hits += d.hits[0];
            }
	   				if (d._children) { // Collapsed
	   					hits += getChildHits(d._children);
	   					if (hits < 0) {
	   						clr = hitcolours.diffCollapsed;
	   					} else if (hits == 0) {
	   						clr = hitcolours.missedCollapsed;
	   					} else if (hits > 0  > 0) {
	   						clr = hitcolours.allCollapsed;
	   					}
	   				} else { // Expanded
	   					if (d.children) {
	   						hits +=getChildHits(d.children);
	   						if (hits < 0) {
	   							clr = hitcolours.diffExpanded;
	   						} else if (hits == 0) {
	   							clr = hitcolours.missedExpanded;
	   						} else if (hits > 0 || d.hits ) {
	   							clr = hitcolours.allExpanded;
	   						}
	   					} else {
	   						if (d.hits && d.hits > 0 || d.totalHits > 0) {
	   							clr = hitcolours.allExpanded;
	   						} else {
	   							clr = hitcolours.attribute;
	   						}
	   					}
	   				}
	   			}
	   			return clr;
	   		}

	   		// We can use negative return to indicate difference
	   		// More playing about for multisource diff
	   		//
	   		// return the number of children with hits
	   		function getChildHits(children) {
	   			var nodeshit = 0;
	   			diff = false;
	   			children.forEach(function(d) {
	   				// just need to count the existence of an hits array
	   				if (d.hits && d.hits > 0) {
	   					nodeshit++;
	   					if (mode == 'DIFF') { // look for diffs
	   						if (diffFound(d)) {
	   							diff = true;
	   						}
	   					}
	   				}
	   			});
	   			if (diff == true) {
	   				nodeshit = 0 - nodeshit;
	   			}
	   			return nodeshit;
	   		}

	   		// Get how many children are below this point
	   		function deepLength (d) {
	   			var numChildren = (d.children ? d.children.length : 0) + (d._children ? d._children.length : 0);

	   		/*	if (d.children) {
	   				for(i=0; i<d.children.length; i++) {
	   					numChildren += deepLength(d.children[i]);
	   				}
	   			}
	   			else {
	   				if (d._children) {
	   					for(i=0; i<d._children.length; i++) {
	   						numChildren += deepLength(d._children[i]);
	   					}
	   				}
	   			}*/

	   			return 	numChildren;
	   		}
			});
}]);
