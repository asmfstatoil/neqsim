package neqsim.processsimulation.util.example;

import neqsim.processsimulation.processequipment.heatExchanger.Heater;
import neqsim.processsimulation.processequipment.mixer.StaticMixer;
import neqsim.processsimulation.processequipment.separator.ThreePhaseSeparator;
import neqsim.processsimulation.processequipment.splitter.Splitter;
import neqsim.processsimulation.processequipment.stream.Stream;
import neqsim.processsimulation.processequipment.util.Adjuster;
import neqsim.processsimulation.processequipment.valve.ThrottlingValve;

/**
 * <p>
 * MEGinjection class.
 * </p>
 *
 * @author asmund
 * @version $Id: $Id
 * @since 2.2.3
 */
public class MEGinjection {
  /**
   * <p>
   * main.
   * </p>
   *
   * @param args an array of {@link java.lang.String} objects
   */
  public static void main(String[] args) {
    // Create the input fluid to the TEG process and saturate it with water at
    // scrubber conditions
    neqsim.thermo.system.SystemInterface feedGas =
        new neqsim.thermo.system.SystemSrkCPAstatoil(273.15 + 42.0, 10.00);
    feedGas.addComponent("nitrogen", 1.03);
    feedGas.addComponent("CO2", 1.42);
    feedGas.addComponent("methane", 83.88);
    feedGas.addComponent("ethane", 8.07);
    feedGas.addComponent("propane", 3.54);
    feedGas.addComponent("i-butane", 0.54);
    feedGas.addComponent("n-butane", 0.84);
    feedGas.addComponent("i-pentane", 0.21);
    feedGas.addComponent("n-pentane", 0.19);
    feedGas.addComponent("n-hexane", 0.28);
    feedGas.addComponent("n-heptane", 1.28);
    feedGas.addComponent("n-octane", 1.28);
    feedGas.addComponent("n-nonane", 2.28);
    feedGas.addComponent("water", 2.0);
    feedGas.addComponent("MEG", 0.0);
    feedGas.createDatabase(true);
    feedGas.setMixingRule(10);
    feedGas.setMultiPhaseCheck(true);

    Stream feedGasStream = new Stream("feed fluid", feedGas);
    feedGasStream.run();
    feedGasStream.setFlowRate(11.23, "MSm3/day");
    feedGasStream.setTemperature(50.0, "C");
    feedGasStream.setPressure(55.00, "bara");

    neqsim.thermo.system.SystemInterface feedMEG = feedGas.clone();
    feedMEG.setMolarComposition(
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.9});

    Stream MEGFeed = new Stream("lean MEG feed stream", feedMEG);
    MEGFeed.setFlowRate(1000.0, "kg/hr");
    MEGFeed.setTemperature(50.0, "C");
    MEGFeed.setPressure(55.0, "bara");

    StaticMixer feedGasMEGmixer = new StaticMixer("MEG-gas mixer");
    feedGasMEGmixer.addStream(feedGasStream);
    feedGasMEGmixer.addStream(MEGFeed);

    Heater pipeline = new Heater("gas-MEG pipeline", feedGasMEGmixer.getOutletStream());
    pipeline.setOutTemperature(273.15 + 35.5);
    pipeline.setOutPressure(80.2);

    Stream mixerStream = new Stream("feed gas and MEG", pipeline.getOutletStream());

    Adjuster adjuster = new Adjuster("MEG adjuster");
    adjuster.setAdjustedVariable(MEGFeed, "mass flow");
    adjuster.setTargetVariable(mixerStream, "mass fraction", 0.6, "-", "aqueous", "MEG");

    neqsim.processsimulation.processsystem.ProcessSystem MEGwelloperations =
        new neqsim.processsimulation.processsystem.ProcessSystem();
    MEGwelloperations.add(feedGasStream);
    MEGwelloperations.add(MEGFeed);
    MEGwelloperations.add(feedGasMEGmixer);
    MEGwelloperations.add(pipeline);
    MEGwelloperations.add(mixerStream);
    MEGwelloperations.add(adjuster);

    MEGwelloperations.run();
    // operations.run();

    MEGwelloperations.save("c:/temp/MEGinjection.neqsim");

    Stream feedStream =
        new Stream("feed to onhore", (Stream) MEGwelloperations.getUnit("feed gas and MEG"));

    ThrottlingValve onshoreChockeValve = new ThrottlingValve("onshore choke valve", feedStream);
    onshoreChockeValve.setOutletPressure(70.3);

    ThreePhaseSeparator slugCatcher =
        new ThreePhaseSeparator("slug catcher", onshoreChockeValve.getOutletStream());

    neqsim.thermo.system.SystemInterface feedMEGOnshore = feedGas.clone();
    feedMEG.setMolarComposition(
        new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.9});

    Stream MEGFeedOnshore = new Stream("lean MEG feed stream", feedMEGOnshore);
    MEGFeedOnshore.setFlowRate(1000.0, "kg/hr");
    MEGFeedOnshore.setTemperature(35.0, "C");
    MEGFeedOnshore.setPressure(80.0, "bara");

    neqsim.processsimulation.processequipment.splitter.Splitter MEGsplitter =
        new Splitter("MEG splitter", MEGFeedOnshore);
    MEGsplitter.setSplitFactors(new double[] {0.1, 0.1, 0.8});

    StaticMixer MEGmixer1 = new StaticMixer("MEG mixer 1");
    MEGmixer1.addStream(slugCatcher.getGasOutStream());
    MEGmixer1.addStream(MEGsplitter.getSplitStream(0));

    ThrottlingValve DPvalve1 = new ThrottlingValve("DP valve 1", MEGmixer1.getOutletStream());
    DPvalve1.setOutletPressure(70.0);

    neqsim.processsimulation.processsystem.ProcessSystem onshoreOperations =
        new neqsim.processsimulation.processsystem.ProcessSystem();
    onshoreOperations.add(feedStream);
    onshoreOperations.add(onshoreChockeValve);
    onshoreOperations.add(slugCatcher);
    onshoreOperations.add(MEGFeedOnshore);
    onshoreOperations.add(MEGsplitter);
    onshoreOperations.add(MEGmixer1);
    onshoreOperations.add(DPvalve1);

    onshoreOperations.run();

    onshoreOperations.save("c:/temp/MEGonshore.neqsim");

    // feedGasMEGmixer.getThermoSystem().display();
    DPvalve1.getThermoSystem().display();
  }
}