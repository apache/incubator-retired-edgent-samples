/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.edgent.samples.scenarios.iotp.range.sensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class LED {
    private final GpioController gpio = GpioFactory.getInstance();
    private GpioPinDigitalOutput pin;

    public LED(Pin pin) {
        this.pin = gpio.provisionDigitalOutputPin(pin, "LED", PinState.HIGH);
        this.pin.setShutdownOptions(true, PinState.LOW);
        this.pin.low();
    }

    public void on() {
        this.pin.high();
    }

    public void off() {
        this.pin.low();
    }

    public void toggle() {
        this.pin.toggle();
    }

    public void flash(long ms) {
        this.pin.pulse(ms);
    }
}
