package neqsim.processSimulation.processEquipment.valve;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkEos;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

public class ThrottlingValveTest {
  static ThrottlingValve valve;
  static StreamInterface inletStream;

  @BeforeAll
  static void initStream() {
    SystemInterface system = new SystemSrkEos(100 + 273.15, 135 + 1.01325);
    system.addComponent("N2", 1.255E-02); // 1,05E-02
    system.addComponent("CO2", 1.20E-02); // 9,31E-03
    system.addComponent("methane", 7.79E-01);// 7,94E-01
    system.addComponent("ethane", 9.52E-02); // 9,15E-02
    system.addComponent("propane", 6.07E-02); // 5,70E-02
    system.addComponent("i-butane", 6.81E-03); // 5,99E-03
    system.addComponent("n-butane", 1.94E-02); // 1,74E-02
    system.addComponent("i-pentane", 4.34E-03); // 4,02E-03
    system.addComponent("n-pentane", 5.46E-03); // 5,18E-03
    system.addComponent("C6", 4.48E-03); // 5,00E-03
    system.addComponent("H2O", 1.40E-05); // 1,40E-05

    system.init(0);
    system.initPhysicalProperties();
    ThermodynamicOperations ops = new ThermodynamicOperations(system);
    ops.TPflash();
    double densityIdealGas =
        system.getPressure() * 1e5 / 8.314 / system.getTemperature(0) * system.getMolarMass();
    double Z = densityIdealGas / system.getDensity();
    System.out.println(system.getMolarMass() * 1000);
    System.out.println(Z);
    System.out.println(densityIdealGas);
    System.out.println(system.getDensity());
    inletStream = new Stream("inletStream", system);
  }

  @BeforeEach
  void setUp() {
    valve = new ThrottlingValve("testValue", inletStream);
    valve.setCv(22.3);
  }

  @Test
  void testCv() {
    double oldCv = valve.getCv();
    double newCv = 5;
    valve.setCv(newCv);
    Assertions.assertEquals(newCv, valve.getCv());
    valve.setCv(oldCv);
    Assertions.assertEquals(oldCv, valve.getCv());
  }

  @Test
  void testGetPercentValveOpening() {
    double oldOpening = valve.getPercentValveOpening();
    double newOpening = 5;
    valve.setPercentValveOpening(newOpening);
    Assertions.assertEquals(newOpening, valve.getPercentValveOpening());
    valve.setPercentValveOpening(oldOpening);
    Assertions.assertEquals(oldOpening, valve.getPercentValveOpening());
  }

  @Test
  void testRun() {
    valve.setAcceptNegativeDP(false);
    valve.setOutletPressure(90 + 1.01325);
    double Cv = 5.0;
    valve.setCv(Cv);
    valve.run();
    double rate = valve.getOutletStream().getFlowRate("kg/hr");
    double rate_s = valve.getOutletStream().getFlowRate("Sm3/hr");
    double rho = valve.getInletStream().getThermoSystem().getDensity();
    double Puc = valve.getInletPressure();
    double Pdc = valve.getOutletPressure();
    double dpC = Math.min(Puc - Pdc, 0.5 * Puc);
    double simple = 0.0865 * Cv * valve.getPercentValveOpening() * Math.sqrt(rho * dpC * 1E5);
    System.out.println(simple);
  }
}
