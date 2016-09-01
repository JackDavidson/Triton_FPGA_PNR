package pnr.fpgas.tci

import java.lang.Boolean
import java.util

import pnr.actions.circuitlut.{ActionExtractToIdentityLut, ActionSwapInput}
import pnr.actions.{ActionMapTo, IAction}
import pnr.components.circuit.{CircuitLut, ICircuitComponent}
import pnr.components.fpga.{FpgaLut, IFpgaComponent}
import pnr.components.{GlobalFalseConst, GlobalInput, GlobalOutput}
import pnr.fpgas.{CannotPlaceException, DoesNotMapException, Fpga}
import pnr.misc.{Defs, Helpers, Pair}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.runtime.RichInt

/**
  * Created by jack on 8/14/16.
  */
class TritoncoreI(placeAndRouter: InternalDom) extends Fpga {

  private val DEFAULT_ROUTING_BITS = "000000"


  private val globalInputs: List[GlobalInput] = List.fill(16){val g = new GlobalInput;
  println("created: "+ Helpers.getComponentName(g))
    g;
  } // 16 global inputs
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

  @throws[DoesNotMapException]
  def getNextItemToPlace(placements: util.ArrayList[ICircuitComponent]) : ICircuitComponent = {
    val globalOutput = placements.find(_ match { case c : GlobalOutput => true case _ => false})
    globalOutput match {
      case Some(c : GlobalOutput) => return c
      case _ =>
    }
    return null
  }

  def inferPlacements(placements: util.ArrayList[ICircuitComponent], numTries: Int): Unit = {
  }

  @throws[CannotPlaceException]
  @throws[DoesNotMapException]
  def makePlacement(component: ICircuitComponent, components: util.List[ICircuitComponent], numTries: Int): util.List[IAction] = {
    val placement = component match {
      case c : CircuitLut => makePlacement(c)
      case c : GlobalInput => makePlacement(c)
      case c : GlobalOutput => makePlacement(c)
      case c => throw new DoesNotMapException("Unrecognized component for TCI: " + component.getClass.getName)
    }


    // we also need to check to see if there are any placed components with their inputs on the wrong spot.
    // thanks to functional programming, this can be done simultaneously!
    // if we find that we have a situation where something cant actually get to its input, we just need to
    // duplicate the output of that LUT. (or just move around inputs)

    def singleInputOnWrongSpot(item: ICircuitComponent, inputPos : Int): Boolean = {
      item match {
        case x: GlobalInput => inputPos != 0 // if the input is a global input, it has to be on LSB
        case x: CircuitLut => lutGroupsForLooup.get(0).get(x) != inputPos
        case _ => false
      }
    }

    def itemHasInputOnWrongSpot = (item: ICircuitComponent) => {
      item match {
        case i : CircuitLut =>
          if (i.getPlacedOn == null) false else
          i.getInputs.zipWithIndex.filter((item) => singleInputOnWrongSpot(item._1, item._2)).size > 0
        case _ => false
      }
    }

    val itemsThatNeedInputMoved = components.filter(itemHasInputOnWrongSpot(_))

    for (item <- itemsThatNeedInputMoved) {
      println("TODO: fix inputs on " + Helpers.getComponentName(item))
    }

    if (placement != null)
      List(placement)
    else
      null
  }

  @throws[CannotPlaceException]
  private def makePlacement(l: CircuitLut): IAction = {
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
          " the global output id is: " + globalOutput.getId)
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
      case Some(outputLut : FpgaLut) => //l.mapTo(outputLut)
        if (Defs.debug) {
          println("We're actually placing LUT on group 0, idx: " + lutGroupsForLooup(0).get(outputLut))
          println("The circuitlut ID is: " + l.getId)
        }
        return new ActionMapTo(l, outputLut)// ========= RETURN HERE ========== since we found where we need to go already
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

    placeOnNextAvailableForGrouping(placementLocation, l) // note this actually returns an Action.
  }

  @throws[CannotPlaceException]
  private def placeOnNextAvailableForGrouping(groupNum: Int, toPlace: CircuitLut): IAction = {
    val group = lutGroupsForLooup(groupNum)
    println("size: " + group.size())
    import scala.collection.JavaConversions._

    // get the next available LUT in the group, and assign to that.
    group.entrySet().find(_.getKey.getCircuitMapping == null) match {
      case None => throw new CannotPlaceException("The LUT grouping number: " + groupNum + " is already full.")
      case Some(nextAvailable) => //toPlace.mapTo(nextAvailable.getKey)
        new ActionMapTo(toPlace, nextAvailable.getKey)
    }
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbi: GlobalInput): IAction = {
    // find the first null input
    val firstNullInput = globalInputs.zipWithIndex.find(_._1.getCircuitMapping == null)
    firstNullInput match { // found anything thats null?
      case Some((_,idx)) => // gbi.mapTo(globalInputs(idx))
        new ActionMapTo(gbi, globalInputs(idx))
      case None => throw new DoesNotMapException("We can't map more than " + globalInputs.length + " input pins.")
    }
  }

  @throws[DoesNotMapException]
  private def makePlacement(gbo: GlobalOutput): IAction = {
    // find the first null input
    val firstNullInput = globalOutputs.zipWithIndex.find(_._1.getCircuitMapping == null)
    firstNullInput match { // found anything thats null?
      case Some((_,idx)) => //gbo.mapTo(globalOutputs(idx))
        return new ActionMapTo(gbo, globalOutputs(idx))
      case None => throw new DoesNotMapException("We can't map more than " + globalOutputs.length + " output pins.")
    }
  }

  @throws[DoesNotMapException]
  def isDone: Boolean = {
    return true
  }

  def getBitstream: String = {

    def toSixBitString = (i : RichInt)=>
    {
      if (i <= 0)
        throw new Exception("converting a negative number to a string is undefined.")
      val str = i.toBinaryString

      if (str.length > 6 || str.length < 0)
        throw new Exception("cant extend a string with " + str.length + " length to length of 6")
      else
        "0"*(6-str.length)+str
    }
    // first, lets get the list of all the LUTs
    val luts = lutGroups.flatten
    val routing : List[List[String]] = luts.zipWithIndex.map(lut => { // iterate over each LUT
      // find the item which is the input
      val circuitItem = lut._1.getCircuitMapping() // find the circuit object that the LUT got mapped to it
      if (circuitItem != null) { // if there is such an item, emit the bitstream for it
        // === sanity check ===
        val outputList = circuitItem.getOutputs
        if (outputList.size != 1) { // if there is more than one output wire, we know there is an unexpected issue
          throw new RuntimeException("we only have a single output on each LUT")
        }
        for (output <- outputList.get(0)) { // we also need to check
          output match {
            case c : GlobalOutput => assert(lutGroupsForLooup(0).get(lut._1) != null, "Found lut connected to output that " +
              "is not in group 0. id:" + lut._1.getCircuitMapping.getId) // sanity check
              assert(lut._2 <= 15, "Found lut that is connected to output that is not at numbered 0 to 15.")
            case _ =>
          }
        }
        val inputList = circuitItem.getInputs
        if (inputList.size != 4) {
          throw new RuntimeException("we only have 4 inputs to each lut. found: " + inputList.size)
        }
        // === end sanity check ===
        inputList.zipWithIndex.map((input) => { // go through all the inputs and generate the bits for routing
          input._1 match {
            case x:CircuitLut => if (input != null) {
              val idxOfInput = lutGroupsForLooup(input._2).get(x.getPlacedOn)
              assert(idxOfInput > 0, "We expected the input number " + input._2
                + " for lut " + lut._1.getCircuitMapping.getId + " to be found in group: " + input._2
                + " but it seems it is not.")
              toSixBitString(idxOfInput) // get the binary string, and extend to 6 bits.
            } else DEFAULT_ROUTING_BITS
            case x:GlobalInput => assert(input._2 == 0, "We expect that a global input will always be on input 0, but "
              + "instead it seems that it is on input " + input._2 + " for: " + Helpers.getComponentName(circuitItem))
              x.getPlacedOn match {
                case y : GlobalInput =>
                  if (Defs.debug)
                    println("looking for GlobalInput: " + Helpers.getComponentName(y))
                  for (i <- globalInputs) {
                    println("found: " + Helpers.getComponentName(i))
                  }
                  toSixBitString(globalInputs.zipWithIndex.find(_._1 == y).getOrElse(
                    throw new RuntimeException("Failed to find the globalInput we mapped to in the list of global inputs.")
                  )._2)
                case y => throw new RuntimeException("It looks like a global input was actually connected to a: "
                  + y.getClass.toString)
              }
            case _:GlobalFalseConst => DEFAULT_ROUTING_BITS // just use the default for 'false' const (desn't matter)
            case _ => throw new RuntimeException("unrecognized class: " + input._1.getClass)
          }
        }).toList
      } else List.fill(4)(DEFAULT_ROUTING_BITS) // repeat the default routing 4 times otherwise
    })
    return routing.toString
  }

  def getDebuggingRepresentation: String = {
    val result: StringBuilder = new StringBuilder
    return result.toString
  }

  override def performInitialActions(circutItems: util.List[ICircuitComponent]): Pair[Boolean, util.List[IAction]] = {
    // first, lets just check to see if there are any items with global inputs that are on the wrong position.
    val itemsWithOneGbiOnWrongSpot = circutItems.filter((circuitComponent : ICircuitComponent) => {
      circuitComponent match {
        case c : CircuitLut => val cInputs = c.getInputs
          cInputs.length == 4 && !cInputs(0).isInstanceOf[GlobalInput] && (
            cInputs(1).isInstanceOf[GlobalInput]
            || cInputs(2).isInstanceOf[GlobalInput]
            || cInputs(3).isInstanceOf[GlobalInput]
            )
        case _ => false
      }
    })
    if (itemsWithOneGbiOnWrongSpot.length > 0) { // generate the IActions to move the global inputs to the proper directory
      val actionListToMoveGbiToFront = itemsWithOneGbiOnWrongSpot.map(
        _ match {
          case c: CircuitLut => val firstIdxOfGbi = c.getInputs.subList (1, 4).zipWithIndex.find(_._1.isInstanceOf[GlobalInput])
            .get._2 + 1
            if (Defs.debug)
              println("we are swapping input: 0  with input: " + firstIdxOfGbi + " for: " + Helpers.getComponentName(c))
            new ActionSwapInput (c, 0, firstIdxOfGbi)
          case _ => throw new Error("looks like I wrote some bad scala");
        }
      )
      return new Pair(true, actionListToMoveGbiToFront)
    }

    // note that by the time that we get to here, we have already moved the Gbi's that we can to the LSB input.
    val itemsWithGbiOnNotLsb = circutItems.filter((circuitComponent : ICircuitComponent) => {
      circuitComponent match {
        case c : CircuitLut => val cInputs = c.getInputs
          cInputs.length == 4 && (
            cInputs(1).isInstanceOf[GlobalInput]
              || cInputs(2).isInstanceOf[GlobalInput]
              || cInputs(3).isInstanceOf[GlobalInput]
            )
        case _ => false
      }
    })
    if (itemsWithGbiOnNotLsb.length > 0) {
      val gibiExtractionItems = itemsWithGbiOnNotLsb.map(
        _ match {
          case c: CircuitLut => c.getInputs.subList(1, 4).filter(_.isInstanceOf[GlobalInput])
          case _ => throw new Error("looks like I wrote some bad scala");
        }
      ).flatten.distinct.map(
        _ match {
          case globalInput : GlobalInput => new ActionExtractToIdentityLut(globalInput, 4, 1)
          case _ => throw new Error("looks like I wrote some bad scala");
        }
      )
      return new Pair(true, gibiExtractionItems)
    }



    return new Pair(false, null)
  }
}
