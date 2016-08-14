package pnr.fpgas

import java.util

import pnr.components.{GlobalInput, GlobalOutput}
import pnr.components.circuit.{CircuitLut, ICircuitComponent}
import pnr.components.fpga.{FpgaLut, IFpgaComponent}
import pnr.fpgas.tci.InternalDom
import pnr.misc.{Defs, Helpers}

/**
  * Created by jack on 8/14/16.
  */
class TritoncoreI(placeAndRouter: InternalDom) extends Fpga {
  private val globalInputs: Array[GlobalInput] = new Array[GlobalInput](16) // 16 global inputs
  private val globalOutputs: Array[GlobalOutput] = new Array[GlobalOutput](16)
  private val lutGroups: Array[util.HashSet[FpgaLut]] = new Array[util.HashSet[FpgaLut]](4)

  for (i <- 0 to lutGroups.length - 1) {
    val grouping: util.HashSet[FpgaLut] = new util.HashSet[FpgaLut]
    for (j <- 0 to 59)
      grouping.add(new FpgaLut)
    lutGroups(i) = grouping
  }

  def getComponents: util.ArrayList[IFpgaComponent] = new util.ArrayList[IFpgaComponent]

  def placeInitialComponentsHard() {
  }

  def placeInitialComponentsSoft(numTries: Int) {
  }

  @throws[DoesNotMapException]
  def getNextItemToPlace(placements: util.ArrayList[ICircuitComponent]): ICircuitComponent = null

  def inferPlacements(placements: util.ArrayList[ICircuitComponent], numTries: Int) {
  }

  @throws[CannotPlaceException]
  @throws[DoesNotMapException]
  def makePlacement(component: ICircuitComponent, numTries: Int): Boolean = {
    val c: Class[_] = component.getClass
    if (c eq classOf[CircuitLut]) return makePlacement(component.asInstanceOf[CircuitLut])
    if (c eq classOf[GlobalInput]) return makePlacement(component.asInstanceOf[GlobalInput])
    if (c eq classOf[GlobalOutput]) return makePlacement(component.asInstanceOf[GlobalOutput])
    throw new DoesNotMapException("Unrecognized component for TCI: " + c.getName)
  }

  @throws[CannotPlaceException]
  private def makePlacement(l: CircuitLut): Boolean = {
    import scala.collection.JavaConversions._
    val ouputs: util.AbstractMap[Integer, util.ArrayList[ICircuitComponent]] = l.getOutputs
    // count up the number of this circuit's outputs that want it to be in each group,
    // and place it in the grouping that most would like.
    var votesForWhereItShouldGo = List[Int](0, 0, 0, 0)
    if (ouputs.size == 1) {
      // only one output
      val outputsOnOne: util.ArrayList[ICircuitComponent] = ouputs.get(0)
      if (Defs.verbose) {
        println("deciding where to place CircuitLut: " + l.getId)
        println("the output goes to " + outputsOnOne.size + " places.")
      }
      // lets look at what the outputs are for this LUT. If it goes to a GlobalOutput, there is only
      // one place we can put it. Otherwise, we should place it based on what other components the
      // output routes to.


      // find all the outputs that are placed.
      val placedOutputs = outputsOnOne.filter(_.getPlacedOn != null)
      if (Defs.verbose)
        println("in: makePlacement. found " + placedOutputs.size + " placed outputs for: "
          + Helpers.getComponentName(l))
      // find inputs for those outputs, and their indices
      votesForWhereItShouldGo = placedOutputs.foldLeft(List[Int](0, 0, 0, 0))((values, placedOutput) =>
        {
          val inputs = placedOutput.getInputs
          val len = inputs.size()
          List[Int] (
            if (len > 0 && inputs(0) == l) values(0) + 1 else values(0), // a vote for group 0
            if (len > 1 && inputs(1) == l) values(1) + 1 else values(1), // a vote for group 1
            if (len > 2 && inputs(2) == l) values(2) + 1 else values(2), // a vote for group 2
            if (len > 3 && inputs(3) == l) values(3) + 1 else values(3)  // a vote for group 3
          )
        }
      )
    }
    else throw new CannotPlaceException("The LUT has more than one output wire! we only have single output wires" +
      " for the tritoncore-I.")

    // get the position that has the highest value. tuple t is: (bestIdx,bestIdxVal,thisIdx). v = valOfThisIdx
    def indexOfHighestVal(arr : List[Int]) = (arr.foldLeft((0,0,0))
    ((t,v) => if (v > t._2) (t._3,v, t._3 + 1) else (t._1,t._2, t._3 + 1)))._1

    val placementLocation = indexOfHighestVal(votesForWhereItShouldGo)

    placeOnNextAvailableForGrouping(placementLocation, l)
  }

  @throws[CannotPlaceException]
  private def placeOnNextAvailableForGrouping(groupNum: Int, toPlace: CircuitLut): Boolean = {
    println("")
    var placed: Boolean = false
    val group: util.HashSet[FpgaLut] = lutGroups(groupNum)
    println("size: " + group.size())
    import scala.collection.JavaConversions._

    // get the next available LUT in the group, and assign to that.
    group.find(!_.getIsMapped) match {
      case None => throw new CannotPlaceException("The LUT grouping number: " + groupNum + " is already full.")
      case Some(nextAvailable) => toPlace.mapTo(nextAvailable)
    }
    true
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbi: GlobalInput): Boolean = {
    var i: Int = 0
    while (globalInputs(i) != null) {
      i += 1
      if (i == globalInputs.length - 1) throw new DoesNotMapException("We can't map more than " + globalInputs.length + " input pins.")
    }
    globalInputs(i) = new GlobalInput
    gbi.mapTo(globalInputs(i))
    true
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbo: GlobalOutput): Boolean = {
    var i: Int = 0
    while (globalOutputs(i) != null) {
      i += 1
      if (i == globalOutputs.length - 1) throw new DoesNotMapException("We can't map more than " + globalOutputs.length + " output pins.")
    }
    globalOutputs(i) = new GlobalOutput
    gbo.mapTo(globalOutputs(i))
    true
  }

  @throws[DoesNotMapException]
  def isDone: Boolean = {
    return true
  }

  def getBitstream: String = {
    return ""
  }

  def getDebuggingRepresentation: String = {
    val result: StringBuilder = new StringBuilder
    return result.toString
  }
}
