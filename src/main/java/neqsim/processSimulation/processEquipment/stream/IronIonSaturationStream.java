/*
 * IronIonSaturationStream.java
 *
 * Created on 12. mars 2001, 13:11
 */

package neqsim.processSimulation.processEquipment.stream;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 * <p>
 * IronIonSaturationStream class.
 * </p>
 *
 * @author Even Solbraa
 * @version $Id: $Id
 */
public class IronIonSaturationStream extends Stream {
  private static final long serialVersionUID = 1000;
  static Logger logger = LogManager.getLogger(IronIonSaturationStream.class);

  protected SystemInterface reactiveThermoSystem;

  /**
   * <p>
   * Constructor for IronIonSaturationStream.
   * </p>
   */
  @Deprecated
  public IronIonSaturationStream() {
    this("IronIonSaturationStream");
  }

  /**
   * <p>
   * Constructor for IronIonSaturationStream.
   * </p>
   *
   * @param thermoSystem a {@link neqsim.thermo.system.SystemInterface} object
   */
  @Deprecated
  public IronIonSaturationStream(SystemInterface thermoSystem) {
    this("IronIonSaturationStream", thermoSystem);
  }

  /**
   * <p>
   * Constructor for IronIonSaturationStream.
   * </p>
   *
   * @param stream a {@link neqsim.processSimulation.processEquipment.stream.StreamInterface} object
   */
  @Deprecated
  public IronIonSaturationStream(StreamInterface stream) {
    this("IronIonSaturationStream", stream);
  }

  /**
   * Constructor for IronIonSaturationStream.
   *
   * @param name name of stream
   */
  public IronIonSaturationStream(String name) {
    super(name);
  }

  /**
   * Constructor for IronIonSaturationStream.
   *
   * @param name name of stream
   * @param stream input stream
   */
  public IronIonSaturationStream(String name, StreamInterface stream) {
    super(name, stream);
  }

  /**
   * <p>
   * Constructor for IronIonSaturationStream.
   * </p>
   *
   * @param name a {@link java.lang.String} object
   * @param thermoSystem a {@link neqsim.thermo.system.SystemInterface} object
   */
  public IronIonSaturationStream(String name, SystemInterface thermoSystem) {
    super(name, thermoSystem);
  }

  /** {@inheritDoc} */
  @Override
  public IronIonSaturationStream clone() {
    IronIonSaturationStream clonedSystem = null;
    try {
      clonedSystem = (IronIonSaturationStream) super.clone();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return clonedSystem;
  }

  /** {@inheritDoc} */
  @Override
  public void run(UUID id) {
    System.out.println("start flashing stream... " + streamNumber);
    if (stream != null) {
      thermoSystem = this.stream.getThermoSystem().clone();
    }
    if (stream != null) {
      reactiveThermoSystem = this.stream.getThermoSystem().setModel("Electrolyte-CPA-EOS-statoil");
    }
    reactiveThermoSystem.addComponent("Fe++", 1e-6);
    // reactiveThermoSystem.chemicalReactionInit();
    // reactiveThermoSystem.createDatabase(true);
    // reactiveThermoSystem.addComponent("water",
    // reactiveThermoSystem.getPhase(0).getComponent("MEG").getNumberOfmoles());
    ThermodynamicOperations thermoOps = new ThermodynamicOperations(reactiveThermoSystem);
    thermoOps.TPflash();
    reactiveThermoSystem.display();
    try {
      System.out
          .println("aqueous phase number " + reactiveThermoSystem.getPhaseNumberOfPhase("aqueous"));
      thermoOps.addIonToScaleSaturation(reactiveThermoSystem.getPhaseNumberOfPhase("aqueous"),
          "FeCO3", "Fe++");
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    reactiveThermoSystem.display();
    System.out.println("number of phases: " + reactiveThermoSystem.getNumberOfPhases());
    System.out.println("beta: " + reactiveThermoSystem.getBeta());
    setCalculationIdentifier(id);
  }

  /** {@inheritDoc} */
  @Override
  public void displayResult() {
    reactiveThermoSystem.display(name);
  }
}
