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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link RaspberryPiThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Ryan Adams - Initial contribution
 */
@NonNullByDefault
public class RaspberryPiThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RaspberryPiThermostatHandler.class);

    private final HttpClient httpClient;

    @Nullable
    private RaspberryPiThermostatConfiguration config;

    public RaspberryPiThermostatHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Nullable
    ScheduledFuture<?> refreshJob;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            fetchAndUpdate();
        } else {
            switch (channelUID.getId()) {
                case RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MIN:
                    if (command instanceof QuantityType<?>) {
                        QuantityType<?> quantityCommand = (QuantityType<?>) command;
                        double farenheit = quantityCommand.toUnit(ImperialUnits.FAHRENHEIT).doubleValue();
                        RaspberryPiThermostatData data = fetch();
                        if (data != null) {
                            data.minTemp = farenheit;
                            data = post(data);
                            if (data != null) {
                                updateValues(data);
                            }
                        }
                    }
                    break;
                case RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MAX:
                    if (command instanceof QuantityType<?>) {
                        QuantityType<?> quantityCommand = (QuantityType<?>) command;
                        double farenheit = quantityCommand.toUnit(ImperialUnits.FAHRENHEIT).doubleValue();
                        RaspberryPiThermostatData data = fetch();
                        if (data != null) {
                            data.maxTemp = farenheit;
                            data = post(data);
                            if (data != null) {
                                updateValues(data);
                            }
                        }
                    }
                    break;
                case RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MAX_DIFF:
                    if (command instanceof QuantityType<?>) {
                        QuantityType<?> quantityCommand = (QuantityType<?>) command;
                        double farenheit = quantityCommand.toUnit(ImperialUnits.FAHRENHEIT).doubleValue();
                        RaspberryPiThermostatData data = fetch();
                        if (data != null) {
                            data.maxTempDifferential = farenheit;
                            data = post(data);
                            if (data != null) {
                                updateValues(data);
                            }
                        }
                    }
                    break;
                case RaspberryPiThermostatBindingConstants.CHANNEL_SCHEDULE_ENABLED:
                    if (command instanceof OnOffType) {
                        boolean scheduleEnabled;
                        if (command == OnOffType.ON) {
                            scheduleEnabled = true;
                        } else {
                            scheduleEnabled = false;
                        }
                        RaspberryPiThermostatData data = fetch();
                        if (data != null) {
                            data.scheduleEnabled = scheduleEnabled;
                            data = post(data);
                            if (data != null) {
                                updateValues(data);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void updateValues(RaspberryPiThermostatData data) {
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_AVG,
                new QuantityType<Temperature>(data.temperatureAverage, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_DOWN,
                new QuantityType<Temperature>(data.temperatureLocal, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_UP,
                new QuantityType<Temperature>(data.temperatureRemote, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MIN,
                new QuantityType<Temperature>(data.minTemp, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MAX,
                new QuantityType<Temperature>(data.maxTemp, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_TEMP_MAX_DIFF,
                new QuantityType<Temperature>(data.maxTempDifferential, ImperialUnits.FAHRENHEIT));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_HUM_DOWN,
                new QuantityType<Dimensionless>(Double.toString(data.relativeHumidityLocal)));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_HUM_UP,
                new QuantityType<Dimensionless>(Double.toString(data.relativeHumidityRemote)));
        updateState(RaspberryPiThermostatBindingConstants.CHANNEL_SCHEDULE_ENABLED,
                OnOffType.from(data.scheduleEnabled));
    }

    private void fetchAndUpdate() {
        RaspberryPiThermostatData data = fetch();
        if (data != null) {
            updateValues(data);
        }
    }

    @Nullable
    private RaspberryPiThermostatData post(RaspberryPiThermostatData data) {
        String url = config.uri;
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();

            Request request = httpClient.POST(url);
            // Set request body
            request.content(new StringContentProvider(gson.toJson(data)), "application/json");
            ContentResponse response = request.send();
            if (response.getStatus() == 200) {
                String responseString = response.getContentAsString();

                RaspberryPiThermostatData data2 = gson.fromJson(responseString, RaspberryPiThermostatData.class);

                return data2;
            }
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    @Nullable
    private RaspberryPiThermostatData fetch() {
        String url = config.uri;
        ContentResponse response;
        try {
            response = httpClient.GET(url);
            if (response.getStatus() == 200) {
                String responseString = response.getContentAsString();

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                RaspberryPiThermostatData data = gson.fromJson(responseString, RaspberryPiThermostatData.class);

                return data;
            }
        } catch (InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(RaspberryPiThermostatConfiguration.class);

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error(e.toString());
            updateStatus(ThingStatus.OFFLINE);
        }

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = (fetch() != null); // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        startAutomaticRefresh();

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            fetchAndUpdate();
        }, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
            }
        }
        super.dispose();
    }
}
