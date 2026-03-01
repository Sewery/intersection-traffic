package com.seweryn.tasior.model;

import com.seweryn.tasior.controller.TrafficDefaults;
import com.seweryn.tasior.model.TrafficLight;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrafficLightTest {

    // stan początkowy

    @Test
    void newTrafficLightShouldBeRed() {
        TrafficLight light = new TrafficLight(TrafficLight.State.RED);
        assertEquals(TrafficLight.State.RED, light.getState());
    }

    @Test
    void newTrafficLightShouldNotBeInTransition() {
        TrafficLight light = new TrafficLight(TrafficLight.State.RED);
        assertFalse(light.isInTransition());
    }

    // przejście do czerwonego

    @Test
    void startTransitionToRedShouldSetYellowToRed() {
        TrafficLight light = new TrafficLight(TrafficLight.State.GREEN);
        light.startTransitionToRed();

        assertEquals(TrafficLight.State.YELLOW_TO_RED, light.getState());
        assertTrue(light.isInTransition());
    }

    @Test
    void afterYellowTimeShouldBecomeRed() {
        TrafficLight light = new TrafficLight(TrafficLight.State.GREEN);
        light.startTransitionToRed();

        for (int i = 0; i < TrafficDefaults.YELLOW_TIME; i++) light.tick();

        assertEquals(TrafficLight.State.RED, light.getState());
        assertFalse(light.isInTransition());
    }

    @Test
    void shouldStillBeYellowBeforeYellowTimeExpires() {
        TrafficLight light = new TrafficLight(TrafficLight.State.GREEN);
        light.startTransitionToRed();

        for (int i = 0; i < TrafficDefaults.YELLOW_TIME - 1; i++) light.tick();

        assertEquals(TrafficLight.State.YELLOW_TO_RED, light.getState());
        assertTrue(light.isInTransition());
    }

    // przejście do zielonego

    @Test
    void startTransitionToGreenShouldSetYellowToGreen() {
        TrafficLight light = new TrafficLight(TrafficLight.State.RED);
        light.startTransitionToGreen();

        assertEquals(TrafficLight.State.YELLOW_TO_GREEN, light.getState());
        assertTrue(light.isInTransition());
    }

    @Test
    void afterYellowTimeShouldBecomeGreen() {
        TrafficLight light = new TrafficLight(TrafficLight.State.RED);
        light.startTransitionToGreen();

        for (int i = 0; i < TrafficDefaults.YELLOW_TIME; i++) light.tick();

        assertEquals(TrafficLight.State.GREEN, light.getState());
        assertTrue(light.isGreen());
        assertFalse(light.isInTransition());
    }

    // tick na stabilnym stanie

    @Test
    void tickOnGreenShouldStayGreen() {
        TrafficLight light = new TrafficLight(TrafficLight.State.GREEN);
        light.tick();

        assertEquals(TrafficLight.State.GREEN, light.getState());
    }

    @Test
    void tickOnRedShouldStayRed() {
        TrafficLight light = new TrafficLight(TrafficLight.State.RED);
        light.tick();

        assertEquals(TrafficLight.State.RED, light.getState());
    }

    // pełny cykl

    @Test
    void fullCycleGreenToRedToGreen() {
        TrafficLight light = new TrafficLight(TrafficLight.State.GREEN);

        light.startTransitionToRed();
        for (int i = 0; i < TrafficDefaults.YELLOW_TIME; i++) light.tick();
        assertEquals(TrafficLight.State.RED, light.getState());

        light.startTransitionToGreen();
        for (int i = 0; i < TrafficDefaults.YELLOW_TIME; i++) light.tick();
        assertEquals(TrafficLight.State.GREEN, light.getState());
    }
}