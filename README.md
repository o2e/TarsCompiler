# TarsCompiler
Tars generated readFrom writeTo method by inherit 

```
    implementation project(':TarsCompiler')
    annotationProcessor project(':TarsCompiler')
//  kotlin use kapt project(':TarsCompiler')
```
kotlin
```java
@Tars
open class Test : TarsStructBase() {

    @JvmField
    @TarsId(tag = 1)
    var field1 = "s"

    @JvmField
    @TarsId(tag = 2)
    var field2 = 188

    @JvmField
//    @TarsId(tag = 3)
    var field3 = "188"

    override fun writeTo(output: TarsOutputStream) {
//        custom field3 code 
        if (field3 != "xxx")
            output.write(field3, 3)
    }
}
```
java
```java
@Tars
public class Test1 extends TarsStructBase {
    @TarsId(tag = 1, require = true)
    protected String txt;
    @TarsId(tag = 3)
    public int anInt;
    @TarsId(tag = 6, require = true)
    char aChar;
    @TarsId(tag = 10)
    Map<String, Object> map;
}
```
`Attention modifier not support private`

call
```java
    // original
    println(Test().toByteArray())
    // new add Tars suffix
    println(TestTars().toByteArray())
```
generated result
```java
// This codes are generated automatically from Ore. Do not modify!
package xxx;

import java.lang.Object;
import java.lang.String;
import java.util.Map;

import moe.ore.tars.TarsInputStream;
import moe.ore.tars.TarsOutputStream;

public final class Test1Tars extends Test1 {
    public void writeTo(TarsOutputStream output) {
        super.writeTo(output);
        if (map != null) {
            output.write(map, 10);
        }
        if (txt != null) {
            output.write(txt, 1);
        }
        output.write(anInt, 3);
        output.write(aChar, 6);
    }

    public void readFrom(TarsInputStream input) {
        super.readFrom(input);
        map = (Map<String, Object>) input.read(map, 10, false);
        txt = (String) input.read(txt, 1, true);
        anInt = (int) input.read(anInt, 3, false);
        aChar = (char) input.read(aChar, 6, true);
    }
}
```
算了 不放洋屁了
就是自动生成一个包装类到build文件夹 类里面就是实现了两个方法而已 但是因为是继承实体类的 字段不能用private修饰 你不需要做其他操作 只管导包调用就行了
