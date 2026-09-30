Fixed Decimal Arithmetic
========================

_Experimental_

Implemented data types:

* Decimal 64 bits, where each value stores its number of decimals (0 - 7): immutable `Decimal64`, mutable `MutableDecimal64` and flyweight `Decimal64Flyweight`
* Fixed-scale decimal 64 bits, where the number of decimals (0 - 9) belongs to the type: flyweight `FixedDecimal` and typed mutable `MutableFixed64`, see [Fixed-scale decimals](#fixed-scale-decimals)

### Decimal64

This library implements fixed decimal arithmetic using a 61-bit (two's complement) mantissa and 3-bit unsigned value for decimals. 

The largest mantissa ranges from -2^60-2 to 2^60-1, decimals 0 - 7 and Not a Number is represented as 2^60-1.

The multiplication and division methods will round the result properly to the largest precision of its operands. Note that only the number of decimals is stored, e.g. 100e0 and 1e2 are normalized to 100e0.

Values convert to and from `BigDecimal` with `toBigDecimal()` and `valueOf(BigDecimal, DecimalContext)` (`Decimal64Flyweight.toBigDecimal(long)` for the flyweight). The number of decimals is kept, and a `BigDecimal` with more than 7 decimals is rounded to 7 using the rounding mode of the context. A value out of range becomes NaN, and converting NaN to `BigDecimal` throws `ArithmeticException`.

The arithmetic operations will not overflow unless the result cannot be represented within the limits above (intermediate values use 128-bit arithmetic when necessary).

### Fixed-scale decimals

A fixed-scale value is a `long` holding the value times 10^decimals, e.g. 101.25 with 2 decimals is 10125. The number of decimals is not stored in the value but in a `FixedDecimal`, so the full 63 bits hold the value: ±92,233,720,368.54775807 with 8 decimals. `Long.MIN_VALUE` is NaN, and a result out of range becomes NaN. Use it when the number of decimals is known for each kind of value, e.g. prices with 8 decimals and quantities with 2, or when more than 7 decimals are needed.

A `FixedDecimal` holds the number of decimals (0 - 9) and performs the arithmetic on raw `long` values of that scale. Operations that combine scales return a value in the scale of the receiver:

```java
interface Price {}      // any types naming the kinds of values
interface Qty {}
interface Notional {}

static final FixedDecimal<Price> PRICE = FixedDecimal.of(8);
static final FixedDecimal<Qty> QTY = FixedDecimal.of(2);
static final FixedDecimal<Notional> NOTIONAL = FixedDecimal.of(2);

final DecimalContext context = new DecimalContext(DecimalRounding.HALF_UP);
final long price = PRICE.valueOf("101.25", context);                           // 101.25000000 (raw 10125000000)
final long quantity = QTY.valueOf("300", context);                             // 300.00 (raw 30000)
final long total = PRICE.add(price, price);                                    // 202.50000000
final long notional = NOTIONAL.multiply(price, PRICE, quantity, QTY, context); // 30375.00
final long rounded = QTY.convert(price, PRICE, context);                       // 101.25
PRICE.toString(total);                                                         // "202.50000000"
```

Add and subtract are plain `long` arithmetic with an overflow check, as the scales need no alignment. Multiply, divide and conversions round with the rounding mode of the `DecimalContext` and use 128-bit intermediates. The divisions by powers of ten compile to multiplications whether or not the `FixedDecimal` is a constant, so a scale read from configuration is as fast as a `static final` one.

The raw `long` values carry no scale, so passing a quantity where a price is expected is not detected. `MutableFixed64` prevents that at compile time: its type parameter (`Price`, `Qty` above) names what the value represents, so values of different kinds cannot be mixed, even with the same number of decimals:

```java
final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "101.25", context);
final MutableFixed64<Qty> quantity = MutableFixed64.valueOf(QTY, "300", context);
final MutableFixed64<Notional> notional = new MutableFixed64<>(NOTIONAL);

price.add(quantity);                          // compile error: MutableFixed64<Qty> cannot be converted to MutableFixed64<Price>
notional.multiply(price, quantity, context);  // 30375.00, kinds combined explicitly in the scale of notional
```

`add`, `subtract`, `set` and `compareTo` require the same kind, while `multiply`, `divide` and `convert` combine any kinds and write the result in the scale of the instance. The operations update the instance and do not allocate. The type parameter is erased at runtime, so raw types and unchecked casts bypass the check; with assertions enabled, same-kind operations also check the number of decimals. `raw()` and `fromRaw(scale, raw)` convert to and from the raw `long`, e.g. to store values in arrays or messages.

Fixed-scale values convert to and from strings and `BigDecimal` with `valueOf(String, DecimalContext)`, `valueOf(BigDecimal, DecimalContext)`, `toString(long)` and `toBigDecimal(long)`, which currently go through `BigDecimal` and allocate.

### Rounding

Multiplication, division, rounding and conversions of both kinds of types use the rounding mode of the `DecimalContext` they are given. `DecimalRounding` supports the same modes as `java.math.RoundingMode`: `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN` (banker's rounding) and `UNNECESSARY`, which returns NaN when the result is inexact. `DecimalRounding.toRoundingMode()` and `DecimalRounding.valueOf(RoundingMode)` convert between the two. A `DecimalContext` is not thread safe; allocate one per thread and reuse it.

Build
-----

### Java Build

Build the project with [Gradle](http://gradle.org/) using this [build.gradle](https://github.com/fredrikjdahlberg/fixdec4j/blob/main/build.gradle) file.

You require the following to build fixdec4j

* Java 21 or later (the build uses a Java 21 toolchain). fixdec4j is tested with Java 21.

Full clean and build:

    $ ./gradlew

Benchmarks
----------

### Jmh

Run benchmarks:

    $ ./gradlew jmh

Run a subset of the benchmarks (regexp):

    $ ./gradlew jmh -Pbenchmarks=Decimal64Flyweight

The decimal benchmarks are parameterized with the same operands (see `Operands`), `SMALL` fits the 64-bit fast path and `LARGE` requires 128-bit intermediates, so timings are comparable across implementations. The fixed decimal results are verified against `BigDecimal` before each run. `double` is approximate and may differ in the last decimal (e.g. the `LARGE` product).

The benchmarks run with the JMH `gc` profiler, so each result is reported together with its heap allocation per operation (`gc.alloc.rate.norm`). Results are written to `fixdec4j-benchmarks/build/results/jmh/results.txt`.

### Results

Apple M1 Pro (10 cores, 32 GB), macOS 27.0, Zulu JDK 21.0.12.1, JMH 1.36, 3 forks × 5 warmup and 5 measurement iterations of 1 s. Average time per operation in ns (lower is better); the error is below ±0.2 ns for all results. The `baseline` benchmarks, which only return the operand, measure 0.57 ns (0.73 ns for `MutableDecimal64`, which resets its result first). Anything close to that is at the resolution of the benchmark.

The fixed-scale results use the largest number of decimals of the operands as their scale (4 for `SMALL`, 6 for `LARGE`), so they compute the same results as `Decimal64`. Their scales are fields rather than constants, the least favourable case for the JIT. They are from a later run, in which `Decimal64Flyweight` measured within 0.12 ns of the values below.

#### Decimal arithmetic

Time in ns/op, shown as `SMALL` / `LARGE` operands:

| Implementation                               | add         | subtract    | multiply     | divide       |
|----------------------------------------------|-------------|-------------|--------------|--------------|
| `Decimal64Flyweight`                         | 2.02 / 1.94 | 2.07 / 1.98 | 2.93 / 3.67  | 3.08 / 8.26  |
| `MutableDecimal64`                           | 2.25 / 2.18 | 2.29 / 2.22 | 3.17 / 3.95  | 3.39 / 8.61  |
| `Decimal64`                                  | 3.47 / 3.31 | 3.38 / 3.30 | 3.83 / 4.62  | 4.21 / 9.26  |
| `FixedDecimal`, operands in the result scale | 0.94 / 0.93 | 0.93 / 0.93 | 2.11 / 3.10  | 2.34 / 6.29  |
| `FixedDecimal`, operands in their own scales | –           | –           | 2.49 / 3.44  | 2.36 / 6.90  |
| `MutableFixed64`                             | 1.06 / 1.06 | –           | 2.93 / 4.09  | –            |
| `BigDecimal`                                 | 3.87 / 3.90 | 3.95 / 3.99 | 6.14 / 27.10 | 5.80 / 13.29 |
| `double` (approximate)                       | 0.97 / 0.97 | 0.97 / 0.96 | 0.97 / 0.97  | 1.10 / 1.11  |

Heap allocation in B/op, shown as `SMALL` / `LARGE` operands:

| Implementation       | add     | subtract | multiply | divide  |
|----------------------|---------|----------|----------|---------|
| `Decimal64Flyweight` | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `MutableDecimal64`   | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `Decimal64`          | 24 / 24 | 24 / 24  | 24 / 24  | 24 / 24 |
| `FixedDecimal`       | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `MutableFixed64`     | 0 / 0   | –        | 0 / 0    | –       |
| `BigDecimal`         | 40 / 40 | 40 / 40  | 40 / 224 | 40 / 40 |
| `double`             | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |

Fixed-scale add and subtract are about twice as fast as `Decimal64Flyweight`, since the scales need no alignment, and multiply and divide are 13 - 25% faster than `Decimal64Flyweight` measured in the same run. Operands in their own scales, which `Decimal64` handles at runtime, cost up to 0.6 ns more. `MutableFixed64` was measured for add and multiply only.

`Decimal64` allocates exactly one 24-byte result object per operation, even when 128-bit intermediates are needed. `BigDecimal` allocates at least 40 bytes, and 224 bytes when a `LARGE` product no longer fits in a `long`.

#### Memory footprint

Size of one value on the heap, measured with [JOL](https://github.com/openjdk/jol) 0.17 on the same JVM (64-bit, compressed references, 8-byte alignment). The deep size includes referenced objects such as the `BigInteger` and `int[]` inside a `BigDecimal`.

| Type                                                  | Bytes per value          |
|-------------------------------------------------------|--------------------------|
| `Decimal64Flyweight`, `FixedDecimal` (`long`)         | 8 (primitive, no object) |
| `Decimal64`                                           | 24                       |
| `MutableDecimal64`                                    | 24                       |
| `MutableFixed64` (the `FixedDecimal` is shared)       | 24                       |
| `Long`, `Double` (boxed, for comparison)              | 24                       |
| `BigDecimal`, unscaled value fits in a `long`         | 40                       |
| `BigDecimal`, 24 digits (`BigInteger` unscaled value) | 112                      |

A `DecimalContext` holds the rounding mode and the 128-bit scratch value used by divide, 56 bytes. It is allocated once per thread and reused, which is why the operations above do not allocate.

License (See LICENSE file for full license)
-------------------------------------------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
