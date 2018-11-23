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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RaspberryPiThermostatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ryan Adams - Initial contribution
 */
@NonNullByDefault
public class RaspberryPiThermostatBindingConstants {

    private static final String BINDING_ID = "raspberrypithermostat";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");

    // List of all Channel ids
    public static final String CHANNEL_TEMP_AVG = "temperatureAverage";
    public static final String CHANNEL_TEMP_UP = "temperatureUpstairs";
    public static final String CHANNEL_TEMP_DOWN = "temperatureDownstairs";
    public static final String CHANNEL_TEMP_MIN = "minTemp";
    public static final String CHANNEL_TEMP_MAX = "maxTemp";
    public static final String CHANNEL_TEMP_MAX_DIFF = "maxTempDiff";
    public static final String CHANNEL_HUM_UP = "humidityUpstairs";
    public static final String CHANNEL_HUM_DOWN = "humidityDownstairs";
    public static final String CHANNEL_SCHEDULE_ENABLED = "scheduleEnabled";
}
