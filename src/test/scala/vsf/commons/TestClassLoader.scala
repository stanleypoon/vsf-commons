package vsf.commons

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import vsf.commons.ClassLoader.DummyTrait

class DummyClass1 extends DummyTrait {
  def value1 = 111
  def value2 = "aaa"
}

class DummyClass2 extends DummyTrait {
  def value1 = 222
  def value2 = "bbb"
}

class DummyClass3(field1: Integer) extends DummyTrait {
  def value1 = 333
  def value2 = "ccc"
}

class DummyClass4(field1: Integer, field2: String) extends DummyTrait {
  def value1 = 444
  def value2 = "ddd"
}

class DummyClass5 {
  def value1 = 555
  def value2 = "eee"
}

case class DummyClass6(value1: Integer = 666, value2: String = "fff") extends DummyTrait

case class DummyClass7(value1: Integer, value2: String)

/* Console
import vsf.commons.ClassLoader
import vsf.commons.ClassLoader.DummyTrait

val cl = new ClassLoader
val data = cl.loadClassData("vsf.commons.DummyClass1")
val c = cl.load("vsf.commons.DummyClass1", data)

(new ClassLoader).load("vsf.commons.DummyClass1")

https://github.com/sbt/sbt/issues/3306
  - classloader errors between test runs
*/
class TestClassLoader extends JUnitSuite with AssertionsForJUnit {
  @Test
  def smokeTest {
    val o1: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass1")
    assertEquals(111, o1.value1)
    assertEquals("aaa", o1.value2)

    val o2: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass2")
    assertEquals(222, o2.value1)
    assertEquals("bbb", o2.value2)

    // Return null instance for class does not exist
    val o3: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.NoSuchClass")
    assertNull(o3)

    // Class3 exists, but can't instantiate without constructor parameter.
    val o4: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass3")
    assertNull(o4)

    // Full length for diagnostic
    // var o = ClassLoader.loadClass("vsf.commons.DummyClass1").get.newInstance.asInstanceOf[DummyTrait]
    val o: DummyTrait =
      ClassLoader
        .loadClass("vsf.commons.DummyClass1")
        .get
        .newInstance
        .asInstanceOf[DummyTrait]

    assertEquals(111, o.value1)
    assertEquals("aaa", o.value2)
  }

  @Test(expected=classOf[ClassCastException])
  def testWithCaseClass {
    var o: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass6", 123:Integer, "CL6")
    assertEquals(123, o.value1)
    assertEquals("CL6", o.value2)

    // Class7 can be created by o1, but can't be DummyTrait
    val args = Array[AnyRef](123:Integer, "Foobar")
    var o1: Any = ClassLoader.loadClass("vsf.commons.DummyClass7").get.getConstructors.head.newInstance(args:_*)

    // Class7 exists, but can't instantiate without constructor parameter.
    val o4: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass3")
    assertNull(o4)

    // This will throws exception
    o = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass7", 123:Integer, "Foobar")
  }

  @Test
  def testConstructor {
    // Full length for diagnostic
    // var o = ClassLoader.loadClass("vsf.commons.DummyClass4").get.getConstructors.head.newInstance(123:Integer, "abc").asInstanceOf[DummyTrait]

    // Since 123 is a primitive that not under Object, we need to explicitly
    // declare 123 as Integer so that it will be passed as Object.
    var o: DummyTrait = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass3", 123:Integer)
    assertEquals(333, o.value1)
    assertEquals("ccc", o.value2)

    o = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass4", 123:Integer, "abc")
    assertEquals(444, o.value1)
    assertEquals("ddd", o.value2)

    // No argument will be passed to constructor, it is using different method.
    // It is nice it looks natural.
    o = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass2")
    assertEquals(222, o.value1)
    assertEquals("bbb", o.value2)
  }

  @Test(expected=classOf[ClassCastException])
  def testConstructor2 {
    // var o = ClassLoader.loadClass("vsf.commons.DummyClass5").get.newInstance.asInstanceOf[DummyTrait]
    var o: DummyTrait = null
    o = ClassLoader.createNewAs[DummyTrait]("vsf.commons.DummyClass5")
  }

  @Test(expected=classOf[NoSuchMethodException])
  def testConstructor3 {
    // o1 will be created but it is an Any so we can't access its methods
    val args = Array[AnyRef](123:Integer, "Foobar")
    var o1: Any = ClassLoader.loadClass("vsf.commons.DummyClass4").get.getConstructors.head.newInstance(args:_*)

    // o2 is not dynamically loaded, but statically compiled into the jar
    var o2: DummyClass4 = classOf[DummyClass4].getConstructors.head.newInstance(args:_*).asInstanceOf[DummyClass4]
    assertEquals(444, o2.value1)
    assertEquals("ddd", o2.value2)

    var o = ClassLoader.loadClass("vsf.commons.DummyClass3").get.getConstructor(classOf[DummyClass3])
      // Here it throws exception as the class loaded dynamically is different
      // than the statically loaded even the class name is the same.

    // code wouldn't get here
    val args1 = Array[AnyRef](123:Integer, "Foobar")
    var o3: DummyClass3 = ClassLoader.loadClass("vsf.commons.DummyClass3").get.getConstructors.head.newInstance(args1:_*).asInstanceOf[DummyClass3]
  }

  @Test(expected=classOf[ClassCastException])
  def testConstructor4 {
    // Similar test like the exception thrown on testContructor3
    val args = Array[AnyRef](123:Integer, "Foobar")
    var o: DummyClass4 = ClassLoader.loadClass("vsf.commons.DummyClass4").get.getConstructors.head.newInstance(args:_*).asInstanceOf[DummyClass4]
  }

  @Test
  def testOther {
    assertTrue(ClassLoader.pathExists("src"))
    assertFalse(ClassLoader.pathExists("notsuchpathexist"))
  }

}
