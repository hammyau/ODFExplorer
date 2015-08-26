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


odfeControllers.controller('ODFEGaugesCtrl', ['$scope', '$routeParams', '$http',  'd3Service', 'nsSummary',
  function($scope, $routeParams, $http,  d3Service, nsSummary) {

    $scope.modeIn = $routeParams.mode;
    $scope.doc = $routeParams.odfName;
    $scope.sources = [];
    $scope.extract = $routeParams.extract;
    $scope.mode = "To be set";
    $scope.namespaces = [];
    $scope.sumSrvs = nsSummary;
    nsSummary.init();
    $scope.summaryStart = 0;
    $scope.summaryEnd = $scope.sources.length - 1;
    $scope.startDir = 'down';
    $scope.endDir = 'up';

    $scope.nsClicked = function(ns) {
      nsSummary.setSummary(ns);
    }

    $scope.docSelected = function(doc, ndxIn) {
      var offset = $scope.namespaces[0].elementsHit.length - $scope.sources.length;
      var ndx =  offset + ndxIn;
      if(ndx ==  $scope.summaryStart) { //move down unless hit End
	if($scope.summaryEnd == $scope.summaryStart + 1) {
	  $scope.startDir = 'up';
	  if(ndx != offset) {
	    $scope.summaryStart--;
	  }
	} else if($scope.summaryEnd > $scope.summaryStart + 1) {
	  if($scope.startDir == 'down') {
	    $scope.summaryStart++;
	  } else {
	    if(ndx != offset) {
	      $scope.summaryStart--;
	    } else {
	      $scope.startDir = 'down';
	      $scope.summaryStart++;
	    }
	  }
	}
      } else if(ndx ==  $scope.summaryEnd) { //move up unless hit Start
	if($scope.summaryEnd == $scope.summaryStart + 1) {
	  $scope.endDir = 'down';
	  if(ndx < $scope.namespaces[0].elementsHit.length - 1) {
	    $scope.summaryEnd++;
	  }
	} else if($scope.summaryEnd > $scope.summaryStart + 1) {
	  if($scope.endDir == 'up') {
	    $scope.summaryEnd--;
	  } else {
	    if(ndx < $scope.namespaces[0].elementsHit.length - 1) {
	      $scope.summaryEnd++;
	    } else {
	      $scope.endDir = 'up';
	      $scope.summaryEnd--;
	    }
	  }
	}
      }
    }

    $scope.getCursor = function(ndx) {
      var offset = $scope.namespaces[0].elementsHit.length - $scope.sources.length;
      if(ndx == $scope.summaryStart -offset) {
	return "bg-success";
      } else if (ndx== $scope.summaryEnd - offset) {
	return "bg-danger";
      }
    }

    //rather than iterating on each row or cell
    //have a service populate a table that can be displayed directly

    //Or give the elements Hit array as a parameter - it has already been found

    $scope.getElementsUsed = function(nsSum) {
      //get us out of here if not initialised yet
      if(nsSum != null && nsSum instanceof Object) {
	var elshit = nsSum.elementsHit; //yeah ok bad variable name
	var nslen = elshit.length;
	var last = elshit[$scope.summaryEnd];
	if(nslen > 1) {
	  //compare last two and look for change
	  var notlast = elshit[$scope.summaryStart];
	  if( last > notlast) {
	    return notlast + " -> " + last;
	  }
	}
	return last;
      }
    };

    $scope.getAttrsUsed = function(nsSum) {
      //get us out of here if not initialised yet
      if(nsSum != null && nsSum instanceof Object) {
	var attrshit = nsSum.attrsHit; //yeah ok bad variable name
	var nslen = attrshit.length;
	var last = attrshit[$scope.summaryEnd];
	if(nslen > 1) {
	  //compare last two and look for change
	  var notlast = attrshit[$scope.summaryStart];
	  if( last > notlast) {
	    return notlast + " -> " + last;
	  }
	}
	return last;
      }
    };

    $scope.getElementPercentage = function(nsSum) {
      //get us out of here if not initialised yet
      if(nsSum != null && nsSum instanceof Object) {
	var elshit = nsSum.elementsHit; //yeah ok bad variable name
	var nslen = elshit.length;
	var numels = nsSum.elements;
	var last = elshit[$scope.summaryEnd];
	var p2 = Math.round((last/numels) * 100);
	if(nslen > 1) {
	  //compare last two and look for change
	  var notlast = elshit[$scope.summaryStart];
	  if( last > notlast) {
	    var p1 = Math.round((notlast/numels) * 100);
	    return p1 + " -> " + p2;
	  }
	}
	return p2;
      }
    };

    $scope.getAttrPercentage = function(nsSum) {
      if(nsSum != null && nsSum instanceof Object) {
	var attrshit = nsSum.attrsHit;
	var nslen = attrshit.length;
	var numattrs = nsSum.attributes;
	if(numattrs > 0) {
	  var last = attrshit[$scope.summaryEnd];
	  var p2 = Math.round((last/numattrs) * 100);
	  if(nslen > 1) {
	    //compare last two and look for change
	    var notlast = attrshit[$scope.summaryStart];
	    if( last > notlast) {
	      var p1 = Math.round((notlast/numattrs) * 100);
	      return p1 + " -> " + p2;
	    }
	  }
	  return p2;
	} else {
	  return 0;
	}
      }
    };

    $scope.getColour = function(ns) {
      //if the element or Attribute percetage has changed
      //show green
      var elp = $scope.getElementPercentage(ns);
      var attrp = $scope.getAttrPercentage(ns);
      if(typeof(elp) == "string" || typeof(attrp) == "string") {
	return "success";
      } else {
        return "warning";
      }
    }


    $scope.$on('Gauges.event')

    d3Service.d3().then(function(d3) {

      // d3.tip - need to figure out if I can really use this
      //and also how to use a function to get the tip function
// Copyright (c) 2013 Justin Palmer
//
// Tooltips for d3.js SVG visualizations
//d3.tip = function (d3) {

  // Public - contructs a new tooltip
  //
  // Returns a tip
//  return function() {
    d3.tip = d3Tip;

      var w = 960, h = 5000, i = 0, barHeight = 20, barWidth = w * .8, duration = 200, root;

      var src = null;
      var sources;
      var numSources = null;
      var hitsOnly = false;
      var mode = null;
                        var ns = "";

      var tree = d3.layout.tree().size([ h, 100 ]);

      var diagonal = d3.svg.diagonal().projection(function(d) {
        return [ d.y, d.x ];
      });

      var vis = d3.select("#chart").append("svg:svg").attr("width", w).attr("height",
          h).append("svg:g").attr("transform", "translate(0,10)");

      var jsonfile = "records/" + $scope.modeIn + '/' + $scope.doc + "/" + $scope.extract + "/odfegauges.json"

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

        //var summary = {};
        //summary.hits = Math.abs(getChildHits(d._children));
        //summary.num = namespaces._children.length;
        $scope.$apply(function() {
/*          namespaces._children.forEach( function(n) {
            var summary = {};
            summary.name = n.name;
            summary.num = n._children.length;
            summary.hits = Math.abs(getChildHits(n._children));
            summary.perc =  Math.round((summary.hits/summary.num) * 100);
            summary.attrCount = 0;
            summary.attrHits = 0;
            n._children.forEach( function(a) {
              if(a._children) {
                summary.attrCount += a._children.length;
                summary.attrHits += Math.abs(getChildHits(a._children));
              }
            });
            summary.avgAttrs = Math.round(summary.attrCount/summary.num);
            if(summary.attrCount > 0) {
              summary.percAttrsHit = Math.round((summary.attrHits/summary.attrCount) * 100);
            } else {
              summary.percAttrsHit = 0;
            }
            //$scope.namespaces.push(summary);
          }); */
            $scope.namespaces = root.summary;
            $scope.sources = root.children[0].children;
	    $scope.summaryStart = $scope.namespaces[0].elementsHit.length - $scope.sources.length;
	    $scope.summaryEnd = $scope.namespaces[0].elementsHit.length - 1;
        });

        update(root);
      });

      function update(source) {

          src = root.children[0];
          mode = root.mode; //just get it from there
          $scope.$apply(function() {
            $scope.mode = mode;
          });
          hitsOnly = root.hits;

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
              return "translate(" + source.y0 + "," + source.x0 + ")";
            }).style("opacity", 1e-6);

        // Enter any new nodes at the parent's previous position.
        nodeEnter.append("svg:rect").attr("y", -barHeight / 2).attr("height",
            barHeight).attr("width", barWidth).style("fill", color).on("click",
            click);

        nodeEnter.append("svg:text").attr("dy", 3.5).attr("dx", 5.5).text(
            function(d) {

              var txt = "";
              if (d.name == null) {
                txt = "Unknown";
              } else {
                  if(d.depth == 1) {
                    ns = d.name;
                  }
                txt = d.name;
                if (d.value != null) {
                  txt += " : " + d.value;
                }
              }
              //if diff mode will be more that one source
              if(d.diff && mode == 'DIFF') {
                var src = 1;
                d.hits.forEach(function(n) {
                  txt += " : source" + src + "hits = " + n;
                  src++;
                });
              }
/*               else {
                if (d.hits) {
/*                  if(d.hits.length > 0) {
//                    txt += " : hits = [";
                    var n = 0;
                    d.hits.forEach(function(h) {
                      if(n>0) {
                        txt += ", ";
                      }
                      txt += h;
                      n++;
                    });
                    txt += "]";
                  }
                }
              }*/
              if (hitsOnly == false) {
              if(d._children && d._children.length > 0) {
                //Should be able to use the depth to determine Element or Attribute
                if (d.depth == 2) {
                  txt += " ( " + Math.abs(getChildHits(d._children)) + " from " + d._children.length + " Elements)";
                }
                if (d.depth == 3) {
                  txt += " ( " + Math.abs(getChildHits(d._children)) + " from " + d._children.length + " Attributes)";
                }
              }
              if(d.children && d.children.length > 0) {
                if (d.depth == 2) {
                  txt += " ( " + Math.abs(getChildHits(d._children)) + " from " + d.children.length + " Elements)";
                }
                if (d.depth == 3) {
                  txt += " ( " + Math.abs(getChildHits(d._children)) + " from " + d.children.length + " Attributes)";
                }
              }
              }
              return txt;
            });

//        if (d.depth == 2) {
          nodeEnter.on('mouseover', function(d){
            if(d.depth >=2) {
              var arg1 = d.name;
              var htmlFunc = function(arg1) {
                //build up the list of documents that hit this node
                //also need to be at the correct level
                var me= false;
                var doctxt = "<ul>";
                var docs = [];
                if(d.hits) {
                  for(h=0; h<d.hits.length; h++) {
                    if(d.hits[h] > 0) {
                      me = true;
                      //docs.push(sources.children[h].docname);
                      doctxt += "<li><span style='color:red'>"+sources.children[h].docname+" - "+d.hits[h]+"</span></li>";
                    }
                    else {
                      doctxt += "<li><span style='color:white'>"+sources.children[h].docname+"</span></li>";
                    }
                  }
                }
                if(me == true) {
                  doctxt+="</ul>";
                  return "<strong>Contributing Document(s)</strong>" + doctxt;
                } else {
                  return "No hits";
                }
              };

              tip.html(htmlFunc);
              return tip.show();
            } else {
              var doNothing = function () {};
              return doNothing;
            }});
          nodeEnter.on('mouseout', tip.hide);
//        };


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
       * ns - hits  colour
       *     0    dark blue
       *     < max  yellow
       *     max    green
       *
       * Element hits          color
       *        0          brown
       *        >0          green
       *        0 and 0 attr hits  brown
       *        >0 and >0 attr hits  yellow
       *        >0 and max attr hits  green
       *
       * Attribute  hits      color
       *           0        brown
       *         >0        green
       *
       * Diff mode same but pink when difference
       *
       */
      function color(d) {
        var clr = "#3182bd";
        var diff = false;
        //cheat for top level
        if(d.depth == 0) {
         if(d._children) {
                clr = "#11AB00";
         } else {
                clr = "#1CFF03";
         }
        }
        else {
          if (mode == 'DIFF') { // look for diffs
            if (diffFound(d)) {
              clr = "red";
              if(d.children) {
                clr = "lightpink";
              }
              diff = true;
            }
          }

          if (diff == false) {
            if (d._children) { // Collapsed
              var hits = getChildHits(d._children);
              if (hits < 0) {
                clr = "red";
              } else if (hits == 0) {
                clr = "#3182bd";
              } else if (hits < d._children.length) {
                clr = "#FFEB03";
              } else {
                clr = "#11AB00"
              }
            } else { // Expanded
              if (d.children) {
                var hits = getChildHits(d.children);
                if (hits < 0) {
                  clr = "lightpink";
                } else if (hits == 0) {
                  clr = "lightblue";
                } else if (hits < d.children.length) {
                  clr = "#FAF07F";
                } else {
                  clr = "#1CFF03"
                }
              } else {
                if (d.hits && d.hits.length > 0) {
                  clr = "#1CFF03";
                } else {
                  clr = "#fd8d3c";
                }
              }
            }
          }
        }
        return clr;
      }

      // Get how many children are below this point
      function deepLength (d) {
        var numChildren = (d.children ? d.children.length : 0) + (d._children ? d._children.length : 0);

        return   numChildren;
      }

      // We can use negative return to indicate difference
      // More playing about for multisource diff
      //
      // return the number of children with hits
      //
      // this needs to be recursive
      //
      //
      //  !!!!!!!!!!! don't need to get the child hits this has already been done in the JSON!!!!!?
      function getChildHits(children) {
        var nodeshit = 0;
        var diff = false;
        if(children) {
          children.forEach(function(d) {
            // just need to count the existence of an hits array
            if (d.hits && d.hits.length > 0) {
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
        }
        return nodeshit;
      }

      function diffFound(d) {
        var diff = false;
        if (d.hits) {
          //var ref = d.hits[0];
          // iterate hits and compare to ref value
          for ( var i = 0; i < (d.hits.length - 1); i++) {
            if (d.hits[i] != d.hits[i + 1]) {
              diff = true;
              d.diff = true;
            }
          }
        }
        return diff;
      }
    });
}]).directive('myGuess', function() {
  return {
    restrict: 'E',
    scope: {doc: '=info'},
    template: 'Info from: {{doc}}'
  };
})
.service('nsSummary', function() {
    var summary = [];
    this.init = function() {
      summary = [];
    };
    this.setSummary = function(ns) {
      summary = ns;
    };
    this.getSummary = function() {
      return summary;
    };
});
