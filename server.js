/*
Copyright 2015 Ian Cunningham

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.Copyright [yyyy] [name of copyright owner]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

var express = require('express');
var path = require('path');
var favicon = require('static-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
// var bodyParser = require('body-parser');

var routes = require('./routes/index');
var users = require('./routes/users');

var spawn = require('child_process').spawn;

var Busboy = require('busboy');
var inspect = require('util').inspect;
var os = require('os');
var fs = require('fs');

// See if we have the required jar file
// if not use maven to build it before starting the server
fs.stat("odfe.jar", odfeJar);

var server  = express();

function odfeJar(err, stats) {
  if(err != null) {
    console.log("No executable jar found so going to maven it\n");
    var mvn = require('maven').create({
	cwd: './mvn'
    });
    mvn.execute(['clean', 'package'], { 'skipTests': true }).then(function(result) {
      odfeBuilt = true;
      console.log("Now going to copy it to the desired location\n");
      console.log('Please visit http://localhost:3000/app/index.html');
      //ok this is a major hack because doesn't cater for maven failing.
      //but the server will stop with a nasty error message anyway
      fs.createReadStream('mvn/target/ODFE-0.0.1-SNAPSHOT-jar-with-dependencies.jar').pipe(fs.createWriteStream('odfe.jar'));
      startServer();
   }, function(err) {
      console.log("Maven Error\n" + err);
    });
  } else {
      startServer();
  }
}


function startServer() {

  console.log('Setting up the server');


var odfFile = "";

// view engine setup
// server.set('views', path.join(__dirname, 'views'));
server.set('view engine', 'jade');

// server.use(favicon());
server.use(logger('dev'));
// server.use(bodyParser.json());
// server.use(bodyParser.urlencoded());
// server.use(cookieParser());
server.use(express.static(path.join(__dirname, 'public')));

server.use('/', routes);
// server.use('/users', users);

server.get('/app', function(req, res, next) {
    req.redirect('app/index.html');
    next(err);
});

server.post('/process', function(req, res) {
	var bb = new Busboy({
		headers : req.headers
	});
	
	var cmdArgs = ['-jar', 'odfe.jar'];
	
	bb.on('file', function(fieldname, file, filename, encoding, mimetype) {
		console.log("Filename " + filename + " field " + fieldname);
		odfFile = path.join('input', path.basename(filename));
		file.pipe(fs.createWriteStream(odfFile));
		if(fieldname == 'file1') {
			cmdArgs.push('-f');
		}
		cmdArgs.push(odfFile);
	});
	
	bb.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
        console.log('Field [' + fieldname + ']: value: ' + inspect(val));
        
        var newAgg = false;
        switch(fieldname){
    	case 'incLegend':
    		cmdArgs.push('-incLegend');
    		cmdArgs.push(val=='on' ? 'true' : 'false');
			break;
    	case 'hitsOnly':
            console.log('hits only case');
    		cmdArgs.push('-g');
			break;
    	case 'mode':
            console.log('mode case '+ val);
    		if(val=='Comparisons') {
    			cmdArgs.push('-d');
    			// need to make sure we have two files
    		}
    		else if (val=='Aggregations') {
    			cmdArgs.push('-a');
    		}
			break;        	
    	case 'depth':
            console.log('depthcase '+ val);	
    		if(val=='content') {
                console.log('content');
    			cmdArgs.push('-c');
    			// need to make sure we have two files
    		}
    		else if (val=='styles') {
                console.log('styles');
    			cmdArgs.push('-s');
    		}
    		else if (val=='meta') {
                console.log('meta');
    			cmdArgs.push('-m');
    		}
			break;        	
    	case 'dropAtts':
            console.log('include attributes case ' + val);
    		cmdArgs.push('-attson');
    		cmdArgs.push((val=='on' ? 'N' : 'Y'));
			break;
		case 'note':
	        console.log('note' + val);
			if(val.length > 0) {
				cmdArgs.push('-n');
				cmdArgs.push(val);
			}
			break;
		case 'XPathChanges':
	        console.log('XPathChanges');
			cmdArgs.push('-x');
			break;
                case 'commented':
                console.log('commented');
                        cmdArgs.push('-commentedDoc');
                cmdArgs.push((val=='on' ? 'Y' : 'N'));
                        break;
                case 'newAggName':
                  console.log('newAggName');
                  if(val.length > 0) {
                    cmdArgs.push('-o');
                    cmdArgs.push(val);
                    newAgg = true;
                  }
                  break;
                case 'aggName':
                  console.log('aggName');
                  if(newAgg == false) {
                    cmdArgs.push('-o');
                    cmdArgs.push(val);
                  }
                  else {
                    console.log('Ignored');
                  }
                  break;
	    }
      });
	
	bb.on('finish', function() {
		runodfe(cmdArgs, res, 'p');
	});
	
	req.pipe(bb);
});

server.post('/filter', function(req, res) {
        console.log("received filter post");
        var bb = new Busboy({
                headers : req.headers
        });
        
        var doc;
        var extract;
        
        var cmdArgs = ['-jar', 'odfe.jar'];
        
        bb.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
          console.log('Field ' + fieldname + ': value: ' + inspect(val));
        
          switch(fieldname){
            case 'doc':
                console.log('filter document ' + val);
                doc = val;
                cmdArgs.push('-doc');
                cmdArgs.push(val);
            break;
            
            case 'extract':
                console.log('extract directory ' + val);
                extract = val;
                cmdArgs.push('-extract');
                cmdArgs.push(val);
            break;
            
            case 'filters':
                console.log('filters length ' + val.length + val);
                cmdArgs.push('-filters');
                if(val.length == 0)
                {
                    cmdArgs.push(' ');
                } else {
                    cmdArgs.push(val);
                }
            break;
            
    		case 'XPathChanges':
    	        console.log('XPathChanges:' + val);
    	        if(val == 'on') {
    	        	cmdArgs.push('-x');
    	        }
   			break;
          };
        });
        
        bb.on('finish', function() {
           console.log('cmd ' + cmdArgs);
           runodfe(cmdArgs, res, 'f', doc , extract);
        });
        
        req.pipe(bb);
});

server.post('/help', function(req, res) {
        console.log("received help post");
        var bb = new Busboy({
                headers : req.headers
        });
        
        var doc = "";
        var extract = "";
        bb.on('field', function(fieldname, val, fieldnameTruncated, valTruncated) {
          console.log('Field ' + fieldname + ': value: ' + inspect(val));
        
        });
        
        var cmdArgs = ['-jar', 'odfe.jar', '-help'];
        
        bb.on('finish', function() {
          console.log('cmd ' + cmdArgs);
          ls    = spawn('java', cmdArgs);

          var title="ODFE Version Info";
          var info="";
          ls.stdout.on('data', function (data) {
                console.log('stdout: ' + data);
                info = info.concat( data );
          });

          ls.stderr.on('data', function (data) {
                console.log('stdout: ' + data);
                info = info.concat( data );
          });

          ls.on('close', function (code) {
              console.log('child process exited with code ' + code);
              res.render('help', {
                message: title,
                helpinfo: info
              });
              res.end();
          });        
        });
        req.pipe(bb);
});



// / catch 404 and forward to error handler
server.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});



// / error handlers

// development error handler
// will print stacktrace
if (server.get('env') === 'development') {
    server.use(function(err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
server.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});
}


var runodfe = function(cmdArgs, res, from, doc, extract) {
    console.log('process ' + cmdArgs.toString());
    ls    = spawn('java', cmdArgs);

    ls.stdout.on('data', function (data) {
              console.log('stdout: ' + data);
    });

    ls.stderr.on('data', function (data) {
              console.log('stderr: ' + data);
    });

    ls.on('close', function (code) {
        console.log('child process exited with code ' + code);
        if(from == 'p') {
          res.redirect('app/index.html');
        } else {
          res.redirect('app/index.html#/xpaths/' + doc + '/' + extract);
        }
    	res.end();
    });
}

module.exports = server;
