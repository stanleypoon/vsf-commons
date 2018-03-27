package vsf.commons

import java.io._
import scala.collection.mutable.ListBuffer

/*
Null– Its a Trait.
null– Its an instance of Null- Similar to Java null.
Nil– Represents an emptry List of anything of zero length. Its not that it refers to nothing but it refers to List which has no contents.
Nothing is a Trait. Its a subtype of everything. But not superclass of anything. There are no instances of Nothing.
None– Used to represent a sensible return value. Just to avoid null pointer exception. Option has exactly 2 subclasses- Some and None. None signifies no result from the method.
Unit– Type of method that doesn’t return a value of anys sort.
 */
object ClassLoader {
  val classpath = ListBuffer[String]()

  // Try to add these as default classpath
  List("target/scala-2.11/test-classes",
      "target/scala-2.11/classes",
      "bin").foreach(addClassPath(_))

  def addClassPath(path: String = ""): List[String] = {
    if (pathExists(path)) {
      classpath += path
    }
    classpath.toList
  }

  def loadClass(s:String): Option[Class[_]] = (new ClassLoader).load(s)

  // createNewAs with no constructor parameters
  def createNewAs[T >: Null](s:String): T = {
    val cl = loadClass(s)
    var o: T = null
    try {
      o = if (cl.isDefined)
            cl.get
            .newInstance
            .asInstanceOf[T]
          else null
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    o
  }

  /*
    createNewAs with constructor parameters
    https://stackoverflow.com/questions/1641104/instantiate-object-with-reflection-using-constructor-arguments?lq=1

  */
  def createNewAs[T >: Null](s:String, varargs: Object*): T = {
    val cl = loadClass(s)
    var o: T = null
    try {
      o = if (cl.isDefined)
            cl.get.getConstructors
            .head
            .newInstance(varargs:_*)
            .asInstanceOf[T]
          else null
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    o
  }

  def pathExists(path: String): Boolean = {
    new java.io.File(path).exists
  }

  // For testing purpose
  trait DummyTrait {
  def value1: Integer
  def value2: String
  }

}

class ClassLoader extends java.lang.ClassLoader {
  def load(s:String) : Option[Class[_]] = {
    val bytes = loadClassData(s)
    load(s, bytes)
  }

  def load(s:String, classData:Array[Byte]) : Option[Class[_]] = {
    var cl: Option[Class[_]] = None
    try {
      cl = Some(defineClass(s, classData, 0, classData.length))
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    cl
  }

  def loadClassData(className : String): Array[Byte]  = {
    var data: Option[Array[Byte]] = None
    try {
      /*
       * takeWhile will still scan all element but below will stop as long as
       * it find one.  Since number of paths is not many, so we didn't use this
       *
       * paths.toIterator.takeWhile(ClassLoader.pathExists(_)).take(1).toList(0)
       */
      val classpath = ClassLoader.classpath.takeWhile(ClassLoader.pathExists(_))
      if (!classpath.isEmpty) {
        val fn = classpath.head + "/" + className.replaceAll("\\.", "/") + ".class"
        val is = new BufferedInputStream(new FileInputStream(fn))
        val bytes: Array[Byte] = Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray
        data = Some(bytes)
      }
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    if (data.isDefined) data.get else Array[Byte]()
  }
}
