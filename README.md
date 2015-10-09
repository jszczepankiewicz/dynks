# Dynks

[![][travis img]][travis]
[![][license img]][license]
[![Dependency Status](https://www.versioneye.com/user/projects/5617a5c8a193340f320001f6/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5617a5c8a193340f320001f6)



Testing and building
---------------------
Requirements: 

+	Java SDK 8 
+	Gradle 2.4
+	Redis 2.8

Prereqisites:
Please make sure you have working Gradle build with at least Java SDK 8. For testing running Redis on localhost is 
required.

Analyzing coverage
---------------------
Coverage can be calculated on module filter: 

```
gradle test jacocoTestReport
```

[travis]:https://travis-ci.org/jszczepankiewicz/dynks
[travis img]:https://travis-ci.org/jszczepankiewicz/dynks.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/github/license/mashape/apistatus.svg
