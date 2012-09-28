# lessnow

_Less Now_ is a LESS CSS compiler written in Java. It is a fork of [L&eacute;-css](https://github.com/lukasdietrich/Le-css). The license is CC BY.

It uses the following libraries:

* [lesscss-java](https://github.com/marceloverdijk/lesscss-java)
* [Apache Commons IO 2.1](http://commons.apache.org/io/)
* [Apache Commons Lang 3.1](http://commons.apache.org/lang/)
* [Apache Commons Logging 1.1.1](http://commons.apache.org/logging/)
* [Rhino: JavaScript for Java 1.7R3](http://www.mozilla.org/rhino/)
* [filedrop](http://iharder.sourceforge.net/current/java/filedrop/)
* [json-simple: Json parser for Java 1.1.1](http://code.google.com/p/json-simple/)

> __[The _Less Now_ project page](http://creapage.net/opensource/2012-lessnow/)__.

## Getting started

### Write a configuration file "lessnow-config.json"

	{
		"gui-config": {
			"height": 700,
			"width": 500,
			"location-x": "left",
			"location-y": "bottom"
		},
		"project-defaults": {
			"charset": "UTF-8",
			"minify": true,
			"recursive": false,
			"scan-delay-dir-s": 10,
			"scan-delay-files-s": 3,
			"show-updated-files": "1d",
			"auto-add-dir-as-projects": false,
			"auto-add-dir-regexp": ".*mydir.*",
			"auto-add-dir-name-count": 2
		},
		"projects": {
			"p1": {
				"path": "/path/to/project1/"
			},
			"p2": {
				"path": "/path/to/project2/dir-with-less/",
				"minify": false
			}
		}
	}

### Start _Less Now_

	java -jar lessnow-N.jar -conf lessnow-config.json
