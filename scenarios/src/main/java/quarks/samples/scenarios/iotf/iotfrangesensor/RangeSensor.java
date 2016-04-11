package quarks.samples.scenarios.iotf.iotfrangesensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

import quarks.function.Supplier;

public class RangeSensor implements Supplier<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Double SPEED_OF_SOUND = 340.29; //speed of sound
	private final GpioPinDigitalInput echoPin;
    private final GpioPinDigitalOutput trigPin;
    private final int MAX_CYCLES = 3000;
	
    private final static GpioController gpio = GpioFactory.getInstance();
    
	public RangeSensor(Pin echoPin, Pin trigPin){
        this.echoPin = gpio.provisionDigitalInputPin( echoPin );
        this.trigPin = gpio.provisionDigitalOutputPin( trigPin );
        this.trigPin.low();
	}
	
	/*
	 * Get the distance in cm. 
	 * Distance is (timeOfSignal * SpeedOfSound) / 2 (divide by 10000 is for units)
	 */
	public Double getDistance() {
		triggerSensor();

		long reboundTimeMicroSeconds = getSignalDuration();
		Double distance = reboundTimeMicroSeconds * SPEED_OF_SOUND / (2 * 10000); //gives distance in cm
		return distance;
	}

	/*
	 * Send signal for 10 microseconds
	 */
	private void triggerSensor() {
		this.trigPin.high();
		try {
			Thread.sleep( 0, 10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.trigPin.low();
	}
	
	/*
	 * Measure signal duration
	 */
	private long getSignalDuration() {
		int cycles = 0;
		long start = System.nanoTime();
		
		while( this.echoPin.isLow() && cycles < MAX_CYCLES){
			//only iterate through MAX_CYCLES times before giving up
			start = System.nanoTime();
			cycles++;
		}
		
		cycles = 0;
		
		while( this.echoPin.isHigh() && cycles < MAX_CYCLES){
			//only iterate through MAX_CYCLES times before giving up
			cycles++;
		}
		long end = System.nanoTime();
		long microSeconds = (long)Math.ceil( ( end - start ) / 1000.0 );
		return microSeconds;
	}

	@Override
	public Double get() {
		return getDistance();
	}

}
