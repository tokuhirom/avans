module.exports = function (grunt) {
  grunt.initConfig({
    bower: {
      install: {
        options: {
          targetDir: './src/main/webapp/components/',
          layout: 'byComponent',
          install: true,
          verbose: false,
          cleanTargetDir: true,
          cleanBowerDir: true
        }
      }
    }
  });
  grunt.loadNpmTasks('grunt-bower-task');
  grunt.registerTask('default', ['bower:install']);
};
