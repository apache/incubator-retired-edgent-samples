# quarks.IoTFRangeSensor

## Requirements: 
* You must have Pi4J installed on the device (if you are running outside of a Raspberry Pi, you will have to download the JARs and include them in your classpath)
* You must have Quarks downloaded and built
* You will need to have an HC-SR04 Range sensor hooked up with your EchoPin set to pin 18 and your TripPin at pin 16 (see these instructions on hardware setup: http://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi). To use a simulated sensor, pass in true as your second argument. 
* You will need to have an LED hooked up to pin 12 (See these instructions to set up an LED, however use pin 12 as your control pin: https://projects.drogon.net/raspberry-pi/gpio-examples/tux-crossing/gpio-examples-1-a-single-led/). To use a simulated LED, pass in true as your third argument. 
* You will need to have your device registered with Watson IoTF and a device.cfg file, or you can use a quickstart version by passing in "quickstart" as your first argument. 
To compile, export your Quarks install and PI4J libraries (on Raspberry Pi, the default Pi4J location is `/opt/pi4j/lib`):
$ export QUARKS=<quarks-install>
$ export PI4J_LIB=<Pi4J-libs>
$ javac -cp $QUARKS/target/java8/samples/lib/quarks.samples.apps.jar:$QUARKS/target/java8/samples/lib/quarks.samples.connectors.jar:$PI4J_LIB/'*' -d bin/ src/com/ibm/streamsx/iotf/range/*.java

To run: 
$ java -cp $QUARKS/target/java8/samples/lib/quarks.samples.apps.jar:$QUARKS/target/java8/samples/lib/quarks.samples.connectors.jar:$PI4J_LIB/'*':bin/ com.ibm.streamsx.iotf.range.IotfRangeSensor device.cfg false

To run in simluated mode (no sensors): 
$ java -cp $QUARKS/target/java8/samples/lib/quarks.samples.apps.jar:$QUARKS/target/java8/samples/lib/quarks.samples.connectors.jar:$PI4J_LIB/'*':bin/ com.ibm.streamsx.iotf.range.IotfRangeSensor device.cfg true

