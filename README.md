# Dynks


[![Travis](https://img.shields.io/travis/rust-lang/rust.svg?style=flat-square)](https://travis-ci.org/jszczepankiewicz/dynks)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jszczepankiewicz/dynks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jszczepankiewicz/dynks)
[![DUB](https://img.shields.io/dub/l/vibe-d.svg?style=flat-square)](LICENSE)
[![VersionEye](https://img.shields.io/versioneye/d/ruby/rails.svg?style=flat-square)](https://www.versioneye.com/user/projects/5617a5c8a193340f320001f6)

Dynks is Java Servlet Container web cache using Redis as distributed storage.

Dynks is implemented as Servlet Filter to be integrated in Java web application.

Dynks was designed with perfomance in mind and at the same time easy to integrate.

What can I cache with dynks?
---------------------
The following operations are supported with dynks:

- specify which URI of web application accessed with GET can be cached
- caching is fully supporting ETAG based cache validation (https://en.wikipedia.org/wiki/HTTP_ETag) to save resources
- working as java filter so that can be easily integrated with other layers like security, compression etc. fully transparent to them
- TTL based cache eviction is supported with arbitrary time unit

Prerequisites
---------------------
- Java SDK 8
- Gradle 2.4
- Redis 2.8 running on localhost on default port (for integration testing)

How do I use it?
---------------------
For Maven:

```
<dependency>
    <groupId>com.github.jszczepankiewicz</groupId>
    <artifactId>dynks</artifactId>
    <version>0.9.8</version>
</dependency>
```

For Gradle:

```
compile 'com.github.jszczepankiewicz:dynks:0.9.8'
```

NOTE: before actually using please check latest release version on: 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jszczepankiewicz/dynks/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jszczepankiewicz/dynks)

How to test
---------------------
Some integration tests require Redis server listening on localhost on default port.

How to get coverage
---------------------
Coverage does not measue all coverage due to lack of support for integration testing. This mostly affects testing the main filter class. The rest of resources should be easily testable.

```
gradle test jacocoTestReport
```


[travis]:https://travis-ci.org/jszczepankiewicz/dynks
[travis img]:https://travis-ci.org/jszczepankiewicz/dynks.svg?branch=master
[license]:LICENSE
[license img]:https://img.shields.io/github/license/mashape/apistatus.svg
