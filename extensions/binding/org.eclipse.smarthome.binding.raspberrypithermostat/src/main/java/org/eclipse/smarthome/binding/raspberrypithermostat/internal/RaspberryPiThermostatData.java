package org.eclipse.smarthome.binding.raspberrypithermostat.internal;

public class RaspberryPiThermostatData {
    public double temperatureLocal;
    public double temperatureRemote;
    public double temperatureAverage;
    public double relativeHumidityLocal;
    public double relativeHumidityRemote;
    public boolean heat;
    public boolean cool;
    public boolean fan;
    public boolean scheduleEnabled;
    public double minTemp;
    public double maxTemp;
    public double maxTempDifferential;
}
