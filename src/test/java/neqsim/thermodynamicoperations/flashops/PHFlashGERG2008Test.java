package neqsim.thermodynamicoperations.flashops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import neqsim.thermodynamicoperations.ThermodynamicOperations;

/**
 * @author ESOL
 */
class PHFlashGERG2008Test {
  static neqsim.thermo.system.SystemInterface testSystem = null;
  static ThermodynamicOperations testOps = null;

  /**
   * @throws java.lang.Exception
   */
  @BeforeEach
  void setUp() {
    testSystem = new neqsim.thermo.system.SystemPrEos(298.0, 50.0);
    testSystem.addComponent("nitrogen", 0.01);
    testSystem.addComponent("CO2", 0.01);
    testSystem.addComponent("methane", 0.98);
    testSystem.setMixingRule("classic");
    testOps = new ThermodynamicOperations(testSystem);
    testOps.TPflash();
    testSystem.initProperties();
  }

  @Test
  void testRun() {
    double[] gergProps = testSystem.getPhase(0).getProperties_GERG2008();
    double gergEnthalpy = gergProps[7] * testSystem.getPhase(0).getNumberOfMolesInPhase(); // J/mol
                                                                                           // K
    testSystem.setPressure(10.0);
    testOps.PHflashGERG2008(gergEnthalpy);
    gergProps = testSystem.getPhase(0).getProperties_GERG2008();
    double gergEnthalpy2 = gergProps[7] * testSystem.getPhase(0).getNumberOfMolesInPhase();
    assertEquals(gergEnthalpy, gergEnthalpy2, Math.abs(gergEnthalpy2) / 1000.0);

    testOps.PHflashGERG2008(gergEnthalpy + 100.0);
    gergProps = testSystem.getPhase(0).getProperties_GERG2008();
    double gergEnthalpy3 = gergProps[7] * testSystem.getPhase(0).getNumberOfMolesInPhase();
    assertEquals(gergEnthalpy3, gergEnthalpy2 + 100.0, Math.abs(gergEnthalpy2) / 1000.0);
  }
}
