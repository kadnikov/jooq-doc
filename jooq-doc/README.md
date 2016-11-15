Prerequisites
===============

You need to install the following tools in order to run this application:

Backend
---------

* [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Maven 3](http://maven.apache.org/)

Frontend
----------

* [Node.js](http://nodejs.org/)
* [NPM](https://www.npmjs.org/)
* [Bower](http://bower.io/)
* [Gulp](http://gulpjs.com/)

You can install these tools by following these steps:

1.  Install Node.js by using a [downloaded binary](http://nodejs.org/download/) or a [package manager](https://github.com/joyent/node/wiki/Installing-Node.js-via-package-manager).
    You can also read this blog post: [How to install Node.js and NPM](http://blog.nodeknockout.com/post/65463770933/how-to-install-node-js-and-npm)

2.  Install Bower by using the following command:

        npm install -g bower

3. Install Gulp by using the following command:

        npm install -g gulp

Running the Application
=========================

After you have installed the tools that are required the build the application, you can run the application by invoking
the following command on command prompt:

        mvn jetty:run -P dev
