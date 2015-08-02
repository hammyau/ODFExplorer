var gulp = require('gulp');

var install = require("gulp-install");
var download = require("gulp-download");

gulp.task('odfe', ['bower'], function() {
    download('https://github.com/hammyau/ODFExplorer/releases/download/v0.1-init/odfe.jar')
    .pipe(gulp.dest("."));
})

gulp.task('libs', ['odfe'], function() {
    download(['https://github.com/hammyau/ODFExplorer/releases/download/v0.1-init/commons-cli-1.2.jar',
              'https://github.com/hammyau/ODFExplorer/releases/download/v0.1-init/jackson-all-1.9.11.jar',
              'https://github.com/hammyau/ODFExplorer/releases/download/v0.1-init/simple-odf-0.8-incubating-jar-with-dependencies.jar'])
    .pipe(gulp.dest("./odfe_lib"));
})

gulp.task('bower', function() {
    process.chdir('./public/app/');
    gulp.src(['./bower.json'])
    .pipe(install());
    process.chdir('../../');
});

gulp.task('default',  ['bower', 'odfe', 'libs'], function() {
// sequence is doing the stuff in order

});
