# FastLock

# FastLock性能测试

<a name="RpYUT"></a>
#### 动机：
模拟jvm对synchronized的优化，同理运用到ReentrantLock上，先cas自旋取锁20次，再排队等待锁。

#### 功能：
1. 支持锁升级，并发度很大时升级为重量级锁，跳过自旋直接排队等待。
2. 支持可中断的获取锁。
3. 支持超时。


<a name="gp7no"></a>
#### 结论：
一点也不fast，各种并发度下性能均略低于ReentrantLock。看来juc工具类还是牛逼，不是随随便便写个锁就能被超越的。


<a name="Ocxgj"></a>
#### 压测结果：
<a name="FwLMK"></a>
无并发：
Benchmark                   Mode  Cnt  Score   Error  Units<br />TestFastLock.fastlock       avgt   10  0.097 ± 0.002  us/op<br />TestFastLock.reentrantLock  avgt   10  0.092 ± 0.003  us/op<br />TestFastLock.synchronized   avgt   10  0.164 ± 0.003  us/op<br />
<br />

<a name="aIrpd"></a>
10并发：
Benchmark                   Mode  Cnt  Score   Error  Units<br />TestFastLock.fastlock       avgt    5  0.286 ± 0.017  us/op<br />TestFastLock.reentrantLock  avgt    5  0.280 ± 0.002  us/op<br />TestFastLock.synchronized   avgt    5  0.445 ± 0.003  us/op<br />

<a name="IU514"></a>
20并发 ：
Benchmark                   Mode  Cnt  Score   Error  Units<br />TestFastLock.fastlock       avgt    5  0.481 ± 0.010  us/op<br />TestFastLock.reentrantLock  avgt    5  0.461 ± 0.040  us/op<br />TestFastLock.synchronized   avgt    5  0.815 ± 0.031  us/op<br />
<br />

<a name="savg2"></a>
50并发：
Benchmark                   Mode  Cnt  Score   Error  Units<br />TestFastLock.fastlock       avgt    5  1.307 ± 0.128  us/op<br />TestFastLock.reentrantLock  avgt    5  1.209 ± 0.163  us/op<br />TestFastLock.synchronized   avgt    5  2.139 ± 0.104  us/op<br />

<a name="Kni5E"></a>
100并发
Benchmark                   Mode  Cnt  Score   Error  Units<br />TestFastLock.fastlock       avgt    5  2.233 ± 0.083  us/op<br />TestFastLock.reentrantLock  avgt    5  2.330 ± 0.332  us/op<br />TestFastLock.synchronized   avgt    5  3.747 ± 0.090  us/op<br />

<a name="L1ybr"></a>
500并发
Benchmark                   Mode  Cnt   Score   Error  Units<br />TestFastLock.fastlock       avgt    5  12.141 ± 1.225  us/op<br />TestFastLock.reentrantLock  avgt    5  11.515 ± 0.956  us/op<br />TestFastLock.synchronized   avgt    5  12.854 ± 0.120  us/op
