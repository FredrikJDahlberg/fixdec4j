Fixed Decimal Arithmetic
========================

Implemented data types:

* Decimal 64 bits, where each value stores its number of decimals (0 - 7): immutable `Decimal64`, mutable `MutableDecimal64` and flyweight `Decimal64Flyweight`
* Fixed-scale decimal 64 bits, where the number of decimals (0 - 9) belongs to the type: flyweight `FixedDecimal` and typed mutable `MutableFixed64`, see [Fixed-scale decimals](#fixed-scale-decimals)

### Decimal64

This library implements fixed decimal arithmetic using a 61-bit (two's complement) mantissa and 3-bit unsigned value for decimals. 

The mantissa ranges from -2^60+2 to 2^60-1 and the decimals from 0 to 7, and Not a Number is represented by the mantissa -2^60+1. `MIN_VALUE` and `MAX_VALUE` are the smallest and largest mantissas with no decimals.

The multiplication and division methods will round the result properly to the largest precision of its operands. Note that only the number of decimals is stored, e.g. 100e0 and 1e2 are normalized to 100e0.

Values with different numbers of decimals keep them, e.g. 1.0 and 1.00 print differently, but they are equal: `equals` and `hashCode` compare the numeric value, consistent with `compareTo`, so they can be mixed as keys of a `HashMap` or `TreeMap` (unlike `BigDecimal`). For the flyweight, `Decimal64Flyweight.equals(long, long)` compares numerically, while `==` also requires the same number of decimals; `stripTrailingZeros` returns the representation with the fewest decimals.

Values convert to and from `BigDecimal` with `toBigDecimal()` and `valueOf(BigDecimal, DecimalContext)` (`Decimal64Flyweight.toBigDecimal(long)` for the flyweight). The number of decimals is kept, and a `BigDecimal` with more than 7 decimals is rounded to 7 using the rounding mode of the context. A value out of range becomes NaN, and converting NaN to `BigDecimal` or to an integer (`longValue`, `intValue`, `shortValue`, `byteValue`) throws `ArithmeticException`.

The arithmetic operations will not overflow unless the result cannot be represented within the limits above (intermediate values use 128-bit arithmetic when necessary).

The same convenience operations as the fixed-scale types are available on all three `Decimal64` types: `multiplyByInteger` (exact) and `divideByInteger` (rounded) keep the decimals of the value, `roundToIncrement` (e.g. to a tick size) and the exact `remainder` (with the sign of the dividend) take the largest number of decimals of the two operands, and `floor` and `ceil` return an integer with no decimals, like `round(0)`. `toBytes(byte[], offset)` writes the same ASCII as `toString()` without allocating, at most `Decimal64Flyweight.STRING_LENGTH_MAX` (21) bytes.

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

final DecimalContext context = DecimalContext.HALF_UP;
final long price = PRICE.valueOf("101.25", context);                           // 101.25000000 (raw 10125000000)
final long quantity = QTY.valueOf("300", context);                             // 300.00 (raw 30000)
final long total = PRICE.add(price, price);                                    // 202.50000000
final long notional = NOTIONAL.multiply(price, PRICE, quantity, QTY, context); // 30375.00
final long rounded = QTY.convert(price, PRICE, context);                       // 101.25
PRICE.toString(total);                                                         // "202.50000000"
```

Values also combine with plain integers and round within their scale, without a second `FixedDecimal`:

```java
final long lots = PRICE.multiplyByInteger(price, 10);                          // 1012.50000000, exact
final long third = PRICE.divideByInteger(price, 3, context);                   // 33.75000000
final long cents = PRICE.round(price, 2, context);                             // 101.25000000, rounded to 2 decimals
final long tick = PRICE.roundToIncrement(price, PRICE.valueOf("0.2", context), context); // 101.20000000
final long whole = PRICE.floor(price);                                         // 101.00000000, ceil gives 102.00000000
final long left = PRICE.remainder(price, PRICE.valueOf("2", context));         // 1.25000000, with the sign of price
```

Add and subtract are plain `long` arithmetic with an overflow check, as the scales need no alignment. Multiply, divide and conversions round with the rounding mode of the `DecimalContext` and use 128-bit intermediates. The divisions by powers of ten, in both kinds of types, multiply by a precomputed reciprocal instead of using a hardware division or a branch on the exponent, so a scale read from configuration is as fast as a `static final` one, and mixing scales costs no mispredicted branches.

The raw `long` values carry no scale, so passing a quantity where a price is expected is not detected. `MutableFixed64` prevents that at compile time: its type parameter (`Price`, `Qty` above) names what the value represents, so values of different kinds cannot be mixed, even with the same number of decimals:

```java
final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "101.25", context);
final MutableFixed64<Qty> quantity = MutableFixed64.valueOf(QTY, "300", context);
final MutableFixed64<Notional> notional = new MutableFixed64<>(NOTIONAL);

price.add(quantity);                          // compile error: MutableFixed64<Qty> cannot be converted to MutableFixed64<Price>
notional.multiply(price, quantity, context);  // 30375.00, kinds combined explicitly in the scale of notional
```

`add`, `subtract`, `set` and `compareTo` require the same kind, while `multiply`, `divide` and `convert` combine any kinds and write the result in the scale of the instance. The operations update the instance and do not allocate. The type parameter is erased at runtime, and two scales of one kind may differ in decimals, so same-kind operations also check the number of decimals and throw `IllegalArgumentException` if they differ. `multiplyByInteger`, `divideByInteger`, `round`, `roundToIncrement`, `floor`, `ceil` and `remainder` are also available on the instance. `raw()` and `fromRaw(scale, raw)` convert to and from the raw `long`, e.g. to store values in arrays or messages.

To halve the memory of large arrays or to read 4-byte prices from messages, `FixedDecimal.toInt(long)` narrows a value to an `int` in the same scale, and `FixedDecimal.fromInt(int)` widens it again. An `int` holds -2,147,483,647 to 2,147,483,647 units, e.g. ±21,474,836.47 with 2 decimals. `Integer.MIN_VALUE` (`FixedDecimal.INT_NAN`) is NaN, and a value out of range narrows to NaN. Calculate with the 64-bit values and narrow the results, since products rarely fit in 32 bits.

Fixed-scale values convert to and from strings and `BigDecimal` with `valueOf(CharSequence, DecimalContext)`, `valueOf(BigDecimal, DecimalContext)`, `toString(long)` and `toBigDecimal(long)`. Parsing a plain decimal (`-101.25`) does not allocate and rounds any decimals beyond the scale with the rounding mode of the context; exponent notation (`1.5E3`) is also accepted but goes through `BigDecimal`. `valueOf(byte[], offset, length, DecimalContext)` parses ASCII bytes in place, e.g. a field of a FIX message, and converts up to eight integer digits and the decimals eight bytes at a time (SWAR, SIMD within a register). `toString(long)` also writes eight digits at a time and allocates only the result, and `toBytes(long, byte[], offset)` writes the same ASCII into a buffer without allocating, at most `STRING_LENGTH_MAX` (21) bytes, returning the number written.

### Rounding

Multiplication, division, rounding and conversions of both kinds of types use the rounding mode of the `DecimalContext` they are given. `DecimalRounding` supports the same modes as `java.math.RoundingMode`: `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN` (banker's rounding) and `UNNECESSARY`, which returns NaN when the result is inexact. `DecimalRounding.toRoundingMode()` and `DecimalRounding.valueOf(RoundingMode)` convert between the two. There is one immutable `DecimalContext` per rounding mode, shared by all threads: use the constants, e.g. `DecimalContext.HALF_UP`, or `DecimalContext.of(DecimalRounding)` for a mode read from configuration.

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

`Decimal64FlyweightBenchmark` and `FixedDecimalBenchmark` also take the rounding mode as a parameter, `HALF_UP` and `HALF_EVEN`, since `HALF_UP` is inlined into the arithmetic and the other modes take a separate path. The fixed operands are the same in every iteration, so every branch is predicted; `MixedOperandsBenchmark` instead walks arrays of random operands with random numbers of decimals, which is closer to a stream of instruments with different scales, and reports the time per operation.

The benchmarks run with the JMH `gc` profiler, so each result is reported together with its heap allocation per operation (`gc.alloc.rate.norm`). Results are written to `fixdec4j-benchmarks/build/results/jmh/results.txt`.

### Results

Apple M1 Pro (10 cores, 32 GB), macOS 27.0, Zulu JDK 21.0.12.1, JMH 1.36, 3 forks × 5 warmup and 5 measurement iterations of 1 s, all from one run. Average time per operation in ns (lower is better); the error is below ±0.21 ns for all results. The `baseline` benchmarks, which only return the operand, measure 0.57 - 0.59 ns (0.72 ns for `MutableDecimal64`, which resets its result first). Anything close to that is at the resolution of the benchmark.

The fixed-scale results use the largest number of decimals of the operands as their scale (4 for `SMALL`, 6 for `LARGE`), so they compute the same results as `Decimal64`. Their scales are fields rather than constants, the least favourable case for the JIT. The rounding mode is `HALF_UP` unless stated otherwise.

#### Decimal arithmetic

Time in ns/op, shown as `SMALL` / `LARGE` operands:

| Implementation                               | add         | subtract    | multiply     | divide       |
|----------------------------------------------|-------------|-------------|--------------|--------------|
| `Decimal64Flyweight`                         | 2.04 / 2.00 | 2.06 / 1.99 | 3.15 / 3.99  | 3.07 / 6.64  |
| `MutableDecimal64`                           | 2.26 / 2.18 | 2.29 / 2.22 | 3.55 / 4.47  | 3.39 / 7.34  |
| `Decimal64`                                  | 3.41 / 3.28 | 3.40 / 3.30 | 4.49 / 5.30  | 4.21 / 7.95  |
| `FixedDecimal`, operands in the result scale | 0.94 / 0.93 | 0.93 / 0.93 | 2.23 / 3.18  | 2.34 / 4.64  |
| `FixedDecimal`, operands in their own scales | –           | –           | 2.80 / 3.55  | 2.36 / 5.18  |
| `MutableFixed64`                             | 1.06 / 1.06 | –           | 2.83 / 3.68  | –            |
| `BigDecimal`                                 | 3.97 / 3.97 | 4.00 / 4.01 | 6.35 / 15.73 | 5.93 / 13.50 |
| `double` (approximate)                       | 1.00 / 1.00 | 0.98 / 0.97 | 0.97 / 0.97  | 1.11 / 1.13  |

Heap allocation in B/op, shown as `SMALL` / `LARGE` operands:

| Implementation       | add     | subtract | multiply | divide  |
|----------------------|---------|----------|----------|---------|
| `Decimal64Flyweight` | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `MutableDecimal64`   | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `Decimal64`          | 24 / 24 | 24 / 24  | 24 / 24  | 24 / 24 |
| `FixedDecimal`       | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |
| `MutableFixed64`     | 0 / 0   | –        | 0 / 0    | –       |
| `BigDecimal`         | 40 / 40 | 40 / 40  | 40 / 128 | 40 / 40 |
| `double`             | 0 / 0   | 0 / 0    | 0 / 0    | 0 / 0   |

Fixed-scale add and subtract are about twice as fast as `Decimal64Flyweight`, since the scales need no alignment, multiply is 20 - 29% faster and divide 24 - 30% faster. Operands in their own scales, which `Decimal64` handles at runtime, cost up to 0.6 ns more. `MutableFixed64` was measured for add and multiply only.

`Decimal64` allocates exactly one 24-byte result object per operation, even when 128-bit intermediates are needed. `BigDecimal` allocates at least 40 bytes, and 128 bytes when a `LARGE` product no longer fits in a `long`.

The divisions by powers of ten multiply by a reciprocal rather than use the hardware division. The M1 divides quickly, and with the exponent constant in these benchmarks the reciprocal costs it up to 0.4 ns per multiply, while values of varying scales (below) convert about 25% faster and x86 avoids a 64-bit division of 14 - 90 cycles.

#### Rounding modes

`HALF_UP` is inlined into the arithmetic; the other modes, measured here with `HALF_EVEN`, take a separate path. Time in ns/op, shown as `SMALL` / `LARGE` operands:

| Implementation       | multiply `HALF_UP` | multiply `HALF_EVEN` | divide `HALF_UP` | divide `HALF_EVEN` |
|----------------------|--------------------|----------------------|------------------|--------------------|
| `Decimal64Flyweight` | 3.15 / 3.99        | 3.87 / 4.66          | 3.07 / 6.64      | 3.57 / 7.47        |
| `FixedDecimal`       | 2.23 / 3.18        | 3.12 / 3.74          | 2.34 / 4.64      | 2.95 / 5.73        |

#### Mixed operands

`MixedOperandsBenchmark` walks arrays of 1024 random operands of 1 - 48 bits with 0 - 7 (`Decimal64`) or 0 - 9 (fixed-scale) decimals, the fixed-scale results in 4 decimals, so that the branches on the magnitudes and scales are not predicted as they are above. Time per operation in ns, shown as `HALF_UP` / `HALF_EVEN`:

| Operation                              | Time        |
|----------------------------------------|-------------|
| `Decimal64Flyweight` multiply          | 3.69 / 3.75 |
| `Decimal64Flyweight` divide            | 4.80 / 5.45 |
| `FixedDecimal` multiply, mixed scales  | 5.10 / 5.65 |
| `FixedDecimal` divide, mixed scales    | 4.77 / 5.44 |
| `FixedDecimal` convert, mixed scales   | 1.91 / 1.97 |

#### Strings

Fixed-scale conversion of the first operand in the result scale, `"12345.6789"` for `SMALL` and `"12345678.901200"` for `LARGE`. The bytes are parsed from a FIX field (`44=12345.6789<SOH>`). Time in ns/op and heap allocation in B/op, shown as `SMALL` / `LARGE`:

| Operation                                         | Time          | Allocation |
|---------------------------------------------------|---------------|------------|
| `valueOf(CharSequence, DecimalContext)`           | 11.93 / 17.96 | 0 / 0      |
| `valueOf(byte[], offset, length, DecimalContext)` | 10.21 / 10.19 | 0 / 0      |
| `toString(long)`                                  | 18.88 / 18.92 | 104 / 104  |
| `toBytes(long, byte[], offset)`                   | 15.10 / 15.51 | 0 / 0      |
| previous `valueOf(String)`, via `BigDecimal`      | 21.66 / 28.54 | 80 / 88    |
| previous `toString(long)`, via `BigDecimal`       | 22.10 / 26.76 | 176 / 176  |

`toBytes` was measured in a later run, in which `toString` measured within 0.1 ns of the value above. A `String` is parsed one character at a time, since copying it to bytes for SWAR costs more than it saves. Parsing bytes takes the same time for any number of digits that fits the eight-byte fast path. `toString` allocates the result `String` and a 25-byte buffer.

#### Memory footprint

Size of one value on the heap, measured with [JOL](https://github.com/openjdk/jol) 0.17 on the same JVM (64-bit, compressed references, 8-byte alignment). The deep size includes referenced objects such as the `BigInteger` and `int[]` inside a `BigDecimal`.

| Type                                                  | Bytes per value          |
|-------------------------------------------------------|--------------------------|
| `Decimal64Flyweight`, `FixedDecimal` (`long`)         | 8 (primitive, no object) |
| `Decimal64`                                           | 24                       |
| `MutableDecimal64`                                    | 24                       |
| `MutableFixed64` (holds the number of decimals)       | 24                       |
| `Long`, `Double` (boxed, for comparison)              | 24                       |
| `BigDecimal`, unscaled value fits in a `long`         | 40                       |
| `BigDecimal`, 24 digits (`BigInteger` unscaled value) | 112                      |

A `DecimalContext` holds only the rounding mode, 16 bytes, and is shared: the operations above keep their 128-bit intermediates in registers and do not allocate.

License (See LICENSE file for full license)
-------------------------------------------

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
