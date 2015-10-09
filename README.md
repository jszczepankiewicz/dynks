# Redmonkey

[![][travis img]][travis]
[![][license img]][license]
[![Dependency Status](https://www.versioneye.com/user/projects/555a43d7634daa5dc80000c7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/555a43d7634daa5dc80000c7)

TODO: cleanings:
---------------------
- fix double entering to filter for one request due to async request
- urwisy change to @since @author
- remove obsolete / unused
- unused imports
'
- IMPLEMENT: 
a) remove hardcoded UTF-8 contentType
b) add multitenancy
similar concept to: https://github.com/TrigonicSolutions/jedis-namespace
why we do not use it? because if we control the application level we may do this much simpler than using this complex
objects
c) add support for full regexp + 

Testing and building
---------------------
Requirements: 

+	Java SDK 8 
+	Gradle 2.4
+	Redis 2.8

Prereqisites:
Please make sure you have working Gradle build with at least Java SDK 7. For testing running Redis on localhost is 
required.

Analyzing coverage
---------------------
Coverage can be calculated on module filter: 

```
gradle test jacocoTestReport
```

[travis]:https://travis-ci.org/jszczepankiewicz/redmonkey
[travis img]:https://travis-ci.org/jszczepankiewicz/redmonkey.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/github/license/mashape/apistatus.svg
