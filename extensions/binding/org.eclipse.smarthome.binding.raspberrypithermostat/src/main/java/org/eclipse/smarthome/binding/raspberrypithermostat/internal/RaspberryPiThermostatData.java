/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.raspberrypithermostat.internal;

/**
 * The {@link RaspberryPiThermostatData} is the data passed to the REST API
 *
 * @author Ryan Adams - Initial contribution
 */
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
