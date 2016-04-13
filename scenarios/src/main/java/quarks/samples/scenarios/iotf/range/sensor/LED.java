package quarks.samples.scenarios.iotf.range.sensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class LED {
	private final GpioController gpio = GpioFactory.getInstance();
	private GpioPinDigitalOutput pin;
	
	public LED(Pin pin){
		this.pin = gpio.provisionDigitalOutputPin(pin, "LED", PinState.HIGH);
		this.pin.setShutdownOptions(true, PinState.LOW);
		this.pin.low();
	}
	
	public void on(){
		this.pin.high();
	}
	
	public void off(){
		this.pin.low();
	}
	
	public void toggle(){
		this.pin.toggle();
	}
	
	public void flash(long ms){
		this.pin.pulse(ms);
	}
}
