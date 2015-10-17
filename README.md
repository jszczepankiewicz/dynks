# Dynks

[![https://travis-ci.org/jszczepankiewicz/dynks](https://img.shields.io/travis/rust-lang/rust.svg?style=flat-square)]()
[![DUB](https://img.shields.io/dub/l/vibe-d.svg?style=flat-square)]()
[![VersionEye](https://img.shields.io/versioneye/d/ruby/rails.svg?style=flat-square)]()

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
Currently no maven release available so first you need to build it with Gradle:

```
cd filter
gradle jar
```

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
