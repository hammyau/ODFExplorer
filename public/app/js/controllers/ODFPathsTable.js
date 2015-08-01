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

odfeControllers.controller('ODFEPathsCtrl', ['$scope', '$routeParams', '$http',  'd3Service',
          function($scope, $routeParams, $http,  d3Service) {

                        $scope.modeIn = $routeParams.mode;
	   		$scope.doc = $routeParams.odfName;
	   		$scope.extract = $routeParams.extract;
	   		$scope.mode = "To be set";
	   		$scope.numpaths = "To be set";
	   		$scope.maxdepth = "To be set";
	   		$scope.mindepth = "To be set";
	   		$scope.avgdepth = "To be set";

			d3Service.d3().then(function(d3) {

				var w = 1800, h = 5000, i = 0, barHeight = 30, barWidth = w * .8, duration = 200, root;

				var hdr = null;
				var numSources = null;
				var mode= null;
				var hitsOnly = false;
				var txt = null;

				var tree = d3.layout.tree().size([ h, 40 ]);

				var diagonal = d3.svg.diagonal().projection(function(d) {
					return [ d.y, d.x ];
				});

				var vis = d3.select("#chart").append("svg:svg").attr("width", w).attr("height",
						h).append("svg:g").attr("transform", "translate(40,60)");

				var jsonfile = "records/" + $scope.modeIn + '/' + $scope.doc + "/" + $scope.extract + "/xpath.json";

		   		d3.json(jsonfile, function(json) {
					json.x0 = 0;
					json.y0 = 0;
					update(root = json);
				});

				var hitcolours = {
				  'originalCollapsed'    : 'red',
				  'originalExpanded'     : 'lightpink',
				  'changedCollapsed'  : '#3182bd',
				  'changedExpanded'   : 'lightblue',
				  'newCollapsed' : '#11AB00',
				  'newExpanded'  : '#FAF07F',
				  'instCollapsed'     : '#1CFF03',
				  'allExpanded'      : '#1CFF03',
				  'attribute'        : '#fd8d3c'
				};

				function update(source) {

		   			mode = root.mode;
		            $scope.$apply(function() {
		            	$scope.mode = mode;
		    	   		$scope.numpaths = root.numPaths;
		    	   		$scope.maxdepth = root.maxDepth;
		    	   		$scope.mindepth = root.minDepth;
		    	   		$scope.avgdepth = root.avgDepth;
		            });
/*					if(hdr == null) {
					  hdr = "fred";
				          d3.select("#header").append("h1").text(root.name);
				          d3.select("#header").append("h2").text("Number of XML Paths: " + root.numPaths);
				          d3.select("#header").append("h2").text("Minimum Depth: " + root.minDepth);
				          d3.select("#header").append("h2").text("Maximum Depth: " + root.maxDepth);
				          d3.select("#header").append("h2").text("Average Depth: " + root.avgDepth);

				          //detail the source(s) from which the data orignates
				          d3.select("#header").append("h2").text("Sources: ");
				          root.children[0].children.forEach(function(src) {
				            d3.select("#header").append("p").text(src.source);
				          });

				          d3.select("#header").append("h2").text("Run date: " + root.run);
					}*/
					// Compute the flattened node list. TODO use d3.layout.hierarchy.
					var nodes = tree.nodes(root.children[1]);

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
								if(d.name != null) {
								  return "translate(" + source.y0 + "," + source.x0 + ")";
								}
							}).style("opacity", 1e-6);

					// Enter any new nodes at the parent's previous position.
//					nodeEnter.append("svg:rect").attr("y", -barHeight / 2).attr("height",
//							barHeight).attr("width", barWidth).style("fill", color).on("click",
//							click);
					nodeEnter.append("svg:rect").attr("y", -barHeight/2).attr("height",
							barHeight).attr("width", barWidth)
							.style("fill", color)
							.on("click", click)
							.append("svg:title")
							.text(function(d) { var txt = "";
							  if(d.attributes) {
							    d.attributes.forEach(function(n) {
										txt += n.name + "=" + n.value +"\n";
									})
							  }
							 return txt; });

					nodeEnter.append("svg:text").attr("dy", 3.5)
							            .style("font-size", "16px")
								    .attr("dx", 3.5)
								    .text(
							function(d) {
								if(d.txt == null) {
								    if (d.parent != null) {
                      d.txt = d.name;
                      if(d.state == "DIFF") {
                        d.txt += " " + d.diffDetails;
                      }
                      else if(d.attributes != null) {
                        d.txt += "[";
                        d.attributes.forEach(function(a) {
                          d.txt += " " + a.name + "=" + a.value;
                        });
                        d.txt += "]";
                      }
                    }
                    else {
                      d.txt = d.name;
                    }
								}
								txt = d.txt;
								if(d.hits) {
                  txt += " : hits = "
                  if(d.hits.length > 1) {
                    if(d.hits[0] > 0) {
                      txt +=  d.hits[0] +"|"+d.hits[1];
                    } else {
                      txt += d.hits[1];
                    }
                  }
                  else {
                    txt += d.hits[0];
                  }
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
				/*
				 * ns - hits	colour
				 * 		0		dark blue
				 * 		< max	yellow
				 * 		max		green
				 *
				 * Element hits					color
				 * 		   0					brown
				 * 		   >0					green
				 * 		   0 and 0 attr hits	brown
				 * 		   >0 and >0 attr hits	yellow
				 * 		   >0 and max attr hits	green
				 *
				 * Attribute	hits			color
				 * 		  		0				brown
				 * 				>0				green
				 *
				 * Diff mode same but pink when difference
				 *
				 */
				function color(d) {
					var clr = "#3182bd";
						if (d._children) { // Collapsed
						  if (d.state == "ORIGINAL") {
						    clr = "#3182bd";
						  }
						  else if (d.state == "CHANGED") {
						    clr = "#FFEB03";
						  }
						  else if (d.state == "INST") {
						    clr = "#3182bd";
						  }
						  else if (d.state == "NEW") {
						    clr = "#11AB00";
						  }
						} else if (d.children){ // Expanded
						    if (d.state == "ORIGINAL") {
						    clr = "#3182bd";
						  }
						  else if (d.state == "CHANGED") {
						    clr = "#FAF07F";
						  }
						  else if (d.state == "INST") {
						    clr = "lightblue";
						  }
						  else if (d.state == "NEW") {
						    clr = "#1CFF03";
						  }
						}
						else {
						    if (d.state == "ORIGINAL") {
						    clr = "lightpink";
						  }
						  else if (d.state == "INST") {
						    clr = "#fd8d3c";
						  }
						  else if (d.state == "NEW") {
						    clr = "#1CFF03";
						  }
						  else if (d.state == "DIFF") {
						    clr = "red";
						  }
						}
					return clr;
				}

				// Get how many children are below this point
				function deepLength (d) {
					var numChildren = (d.children ? d.children.length : 0) + (d._children ? d._children.length : 0);

					return 	numChildren;
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
			});

}]);
