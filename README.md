Fixed Decimal Arithmetic
========================

_Experimental_

Implemented data types:

* Immutable fixed decimal 64 bits
* Mutable fixed decimal 64 bits
* Mutable unsigned integer 128 bits
* Flyweight fixed decimal 64 bits
* Flyweight unsigned integer 64 bits

This library implements fixed decimal arithmetic using a 61-bit (two's complement) mantissa and 3-bit unsigned value for decimals. 

The largest mantissa ranges from -2^60-2 to 2^60-1, decimals 0 - 7 and Not a Number is represented as 2^60-1.

The multiplication and division methods will round the result properly to the largest precision of its operands. Note that only the number of decimals is stored, e.g. 100e0 and 1e2 are normalized to 100e0.

The supported decimal rounding modes are Up and Down, see RoundingMode.

The arithmetic operations will not overflow unless the result cannot be represented within the limits above (intermediate values use 128-bit arithmetic when necessary).

Build
-----

### Java Build

Build the project with [Gradle](http://gradle.org/) using this [build.gradle](https://github.com/fredrikjdahlberg/fixdec4j/blob/main/build.gradle) file.

You require the following to build fixdec4j

* The Latest release of Java 8. fsmp4j is tested with Java 8.

Full clean and build:

    $ ./gradlew

Benchmarks
----------

### Jmh

Run benchmarks:

    $ ./gradlew jmh

License (See LICENSE file for full license)
-------------------------------------------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
