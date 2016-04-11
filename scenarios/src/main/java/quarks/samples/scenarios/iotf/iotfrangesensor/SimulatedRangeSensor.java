package quarks.samples.scenarios.iotf.iotfrangesensor;

import java.util.Random;

import quarks.function.Supplier;

public class SimulatedRangeSensor implements Supplier<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Random rand;
	
	public SimulatedRangeSensor(){
		rand = new Random();
	}
	
	@Override
	public Double get() {
		Double distance = 20 + rand.nextDouble() * 20 ;
		return distance;
	}

}
