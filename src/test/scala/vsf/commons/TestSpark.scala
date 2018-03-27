package vsf.commons

import org.scalatest.junit.JUnitSuite
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.BeforeClass
import org.apache.spark.SparkContext

// @TODO Should use holdenkarau for testing, didn't get it to work
// import com.holdenkarau.spark.testing.SharedSparkContext

object TestSpark {
  import org.apache.spark.SparkContext
  import org.apache.spark.SparkContext._
  import org.apache.spark.SparkConf
  import org.apache.spark.rdd._

  var sc:SparkContext = null

  val case8    = List(s"""{"KEY1":"VALUE1"}""", "[corrupted]", "another_corrupted")

  @BeforeClass
  def setup {
    val conf = new SparkConf().setMaster("local[2]").setAppName("testing")
    sc = new SparkContext(conf)
  }

}

class TestSpark extends JUnitSuite with AssertionsForJUnit {
  @Test
  def smokeTest {
  // "test initializing spark context"
    val list = List(1, 2, 3, 4)
    val rdd = TestSpark.sc.parallelize(list)

    assert(rdd.count === list.length)
  }

  @Test
  def testJson {
    // two corrupted jsons and one good json
    val sc = TestSpark.sc
  	val total = sc.accumulator(0l, "total")

    val rdd = sc.parallelize(TestSpark.case8)

    val jsonRDD = rdd.map{
      l =>
       total += 1
       l
    }

    assertEquals(3, jsonRDD.count())
    //assert(total.value == 3)
  }

}
