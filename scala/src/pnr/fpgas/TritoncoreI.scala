package pnr.fpgas

import java.util

import pnr.components
import pnr.components.{GlobalInput, GlobalOutput}
import pnr.components.circuit.{CircuitLut, ICircuitComponent}
import pnr.components.fpga.{FpgaLut, IFpgaComponent}
import pnr.fpgas.tci.InternalDom
import pnr.misc.{Defs, Helpers}

import scala.collection.JavaConversions._

/**
  * Created by jack on 8/14/16.
  */
class TritoncoreI(placeAndRouter: InternalDom) extends Fpga {
  private val globalInputs: List[GlobalInput] = List.fill(16)(new GlobalInput) // 16 global inputs
  private val globalOutputs: List[GlobalOutput] = List.fill(16)(new GlobalOutput)
  private val lutGroupsForLooup: List[util.HashMap[FpgaLut, Int]] = List.fill(4)(new util.HashMap[FpgaLut, Int])
  private val lutGroups: List[List[FpgaLut]] = List.fill(4)(List.fill(60)(new FpgaLut))


  // iterate over our two representations together
  for (lutGroups : (List[FpgaLut],util.HashMap[FpgaLut, Int]) <- (lutGroups, lutGroupsForLooup).zipped) {
    for (lutWithIdx <- lutGroups._1.zipWithIndex) {
      lutGroups._2.put(lutWithIdx._1, lutWithIdx._2)
    }
  }

  def getComponents: util.ArrayList[IFpgaComponent] = new util.ArrayList[IFpgaComponent]

  def placeInitialComponentsHard() {
  }

  def placeInitialComponentsSoft(numTries: Int) {
  }

  @throws[DoesNotMapException]
  def getNextItemToPlace(placements: util.ArrayList[ICircuitComponent]) : ICircuitComponent = {
    val globalOutput = placements.find(_ match { case c : GlobalOutput => true case _ => false})
    globalOutput match {
      case Some(c : GlobalOutput) => return c
      case _ =>
    }
    return null
  }

  def inferPlacements(placements: util.ArrayList[ICircuitComponent], numTries: Int) {
  }

  @throws[CannotPlaceException]
  @throws[DoesNotMapException]
  def makePlacement(component: ICircuitComponent, numTries: Int): Boolean = {
    component match {
      case c : CircuitLut => makePlacement(c)
      case c : GlobalInput => makePlacement(c)
      case c : GlobalOutput => makePlacement(c)
      case c => throw new DoesNotMapException("Unrecognized component for TCI: " + component.getClass.getName)
    }
  }

  @throws[CannotPlaceException]
  private def makePlacement(l: CircuitLut): Boolean = {
    val ouputs = l.getOutputs



    /*val globalOutputConnection = ouputs.entrySet().find(_.getValue.getClass == GlobalOutput)
    globalOutputConnection match {
      Some() =>
    }*/

    // count up the number of this circuit's outputs that want it to be in each group,
    // and place it in the grouping that most would like.
    val placedOutputs = if (ouputs.size == 1) {
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
      outputsOnOne.filter(_.getPlacedOn != null)

    } else throw new CannotPlaceException("The LUT has more than one output wire! we only have single output wires" +
      " for the tritoncore-I.")

    if (Defs.verbose)
      println("in: makePlacement. found " + placedOutputs.size + " placed outputs for: "
        + Helpers.getComponentName(l))


    val globalOutputFound = placedOutputs.find(_ match {case c : GlobalOutput => true case _ => false})
    val forcedPlacementBecauseOutput : Option[FpgaLut] = globalOutputFound match {
      case Some(globalOutput : GlobalOutput) =>  if (Defs.verbose)
        println("in: makePlacement. found a global output, which means we have to place at a particular spot. " +
          " the number is: " + globalOutput.getId)
        if (globalOutput.getPlacedOn == null)
          throw new CannotPlaceException("Unexpected issue! we are placing a lut that connects to an output, " +
            "but the output has not been placed! (unknown TCI PNR bug!)")

        println("gbout len is: " + globalOutputs.length)
        val indexOfGlobalOutput = globalOutputs.zipWithIndex.find(
          (globalWithIdx) => {
            println("##" + globalWithIdx._1 + "   " + globalOutput.getPlacedOn())
            globalWithIdx._1 == globalOutput.getPlacedOn()
          }
        )
        indexOfGlobalOutput match {
          case Some((_, pos : Int)) =>
            println("found that we will need to place this at: group 0, idx " + pos)
            Some(lutGroups(0)(pos))
          case None => throw new CannotPlaceException("Unexpected issue! we looked for a globalOutput in the list, and " +
            "did not find it! (unknown TCI PNR bug!)")
        }
      case _ => None
    }

    forcedPlacementBecauseOutput match {
      case Some(outputLut : FpgaLut) => l.mapTo(outputLut)
        return true// ========= RETURN HERE ========== since we found where we need to go already
      case _ =>
    }


    // find inputs for those outputs, and their indices
    val votesForWhereItShouldGo = placedOutputs.foldLeft(List[Int](0, 0, 0, 0))((values, placedOutput) =>
      {
        val inputs = placedOutput.getInputs
        val len = inputs.size()
        // for every output, get its index and
        List[Int] (
          if (len > 0 && inputs(0) == l) values(0) + 1 else values(0), // a vote for group 0
          if (len > 1 && inputs(1) == l) values(1) + 1 else values(1), // a vote for group 1
          if (len > 2 && inputs(2) == l) values(2) + 1 else values(2), // a vote for group 2
          if (len > 3 && inputs(3) == l) values(3) + 1 else values(3)  // a vote for group 3
        )
      }
    )

    // get the position that has the highest value. tuple t is: (bestIdx,bestIdxVal). v = valOfThisIdx
    def indexOfHighestVal(arr : List[Int]) = (arr.zipWithIndex.foldLeft((0,0))
    ((t,v) => if (v._1 > t._2) (v._2,v._1) else (t._1,t._2)))._1

    val placementLocation = indexOfHighestVal(votesForWhereItShouldGo)

    placeOnNextAvailableForGrouping(placementLocation, l)
  }

  @throws[CannotPlaceException]
  private def placeOnNextAvailableForGrouping(groupNum: Int, toPlace: CircuitLut): Boolean = {
    println("")
    var placed: Boolean = false
    val group = lutGroupsForLooup(groupNum)
    println("size: " + group.size())
    import scala.collection.JavaConversions._

    // get the next available LUT in the group, and assign to that.
    group.entrySet().find(_.getKey.getCircuitMapping == null) match {
      case None => throw new CannotPlaceException("The LUT grouping number: " + groupNum + " is already full.")
      case Some(nextAvailable) => toPlace.mapTo(nextAvailable.getKey)
    }
    true
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbi: GlobalInput): Boolean = {
    // find the first null input
    val firstNullInput = globalInputs.zipWithIndex.find(_._1.getCircuitMapping == null)
    firstNullInput match { // found anything thats null?
      case Some((_,idx)) => gbi.mapTo(globalInputs(idx))
      case None => throw new DoesNotMapException("We can't map more than " + globalInputs.length + " input pins.")
    }
    true
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbo: GlobalOutput): Boolean = {
    // find the first null input
    val firstNullInput = globalOutputs.zipWithIndex.find(_._1.getCircuitMapping == null)
    firstNullInput match { // found anything thats null?
      case Some((_,idx)) => gbo.mapTo(globalOutputs(idx))
      case None => throw new DoesNotMapException("We can't map more than " + globalOutputs.length + " output pins.")
    }
    true
  }

  @throws[DoesNotMapException]
  def isDone: Boolean = {
    return true
  }

  def getBitstream: String = {
    val result = new StringBuilder
    for (lutGroup <-  lutGroups.zipWithIndex) {
      for (lut <- lutGroup._1.zipWithIndex) {
        // find the item which is the input
        val circuitItem = lut._1.getCircuitMapping()
        if (circuitItem != null) {
          // === sanity check ===
          val outputList = circuitItem.getOutputs
          if (outputList.size != 1) {
            throw new RuntimeException("we only have a single output on each LUT");
          }
          for (output <- outputList.get(0)) {
            output match {
              case c : GlobalOutput => assert(lutGroup._2 == 0, "Found lut connected to output that is not in group 0.") // sanity check
                assert(lut._2 <= 15, "Found lut that is connected to output that is not at numbered 0 to 15.")
              case _ =>
            }
          }
          // === end sanity check ===
          val inputList = circuitItem.getInputs
          /*for (input <- inputList.) { // go through all the inputs and

          }*/
        }
      }
    }
    return result.toString
  }

  def getDebuggingRepresentation: String = {
    val result: StringBuilder = new StringBuilder
    return result.toString
  }
}
