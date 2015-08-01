'use strict';

var d3Controllers = angular.module('d3', [])
.factory('d3Service', [function(){
  var d3;
  // insert d3 code here
  alert("register me?");
  return d3;
}]);


d3Controllers.value('d3Alive', 'almose');

var injector = angular.injector(['ng', 'd3'])

d3Controllers.controller('d3Ctrl', ['$scope', 'd3Service',
                                              function($scope, d3Service) {
                               			
                               				$scope.odfName = 'test';
                               				
                               				$scope.clicked = function(src) {
                               					alert("click");
                               				}
                               }]);

angular.module('d3', [])
.factory('d3Service', ['$document', '$q', '$rootScope',
  function($document, $q, $rootScope) {
    var d = $q.defer();
    function onScriptLoad() {
      // Load client in the browser
      $rootScope.$apply(function() { d.resolve(window.d3); });
    }
    // Create a script tag with d3 as the source
    // and call our onScriptLoad callback when it
    // has been loaded
    var scriptTag = $document[0].createElement('script');
    scriptTag.type = 'text/javascript'; 
    scriptTag.async = true;
    scriptTag.src = 'd3/d3.v3.min.js';
    scriptTag.onreadystatechange = function () {
      if (this.readyState == 'complete') onScriptLoad();
    }
    scriptTag.onload = onScriptLoad;

    var s = $document[0].getElementsByTagName('body')[0];
    s.appendChild(scriptTag);

    return {
      d3: function() { return d.promise; }
    };
}]);