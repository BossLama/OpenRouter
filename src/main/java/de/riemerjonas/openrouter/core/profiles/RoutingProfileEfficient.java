package de.riemerjonas.openrouter.core.profiles;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;

public class RoutingProfileEfficient implements OpenRouterRoutingProfile
{
    private final short baseConsumption = 100;

    @Override
    public short getWeight(OpenRouterEdge edge) {
        return (short) (edge.getDistance() * estimateFuelConsumption(edge.getMaxSpeed()));
    }

    private short estimateFuelConsumption(short maxSpeed)
    {
        if(maxSpeed <= 10) return (short) (baseConsumption * 0.3);
        if(maxSpeed <= 20) return (short) (baseConsumption * 0.25);
        if(maxSpeed <= 30) return (short) (baseConsumption * 0.2);
        if(maxSpeed <= 40) return (short) (baseConsumption * 0.18);
        if(maxSpeed <= 50) return (short) (baseConsumption * 0.17);
        if(maxSpeed <= 60) return (short) (baseConsumption * 0.16);
        if(maxSpeed <= 70) return (short) (baseConsumption * 0.18);
        if(maxSpeed <= 80) return (short) (baseConsumption * 0.2);
        if(maxSpeed <= 90) return (short) (baseConsumption * 0.25);
        if(maxSpeed <= 100) return baseConsumption;
        if(maxSpeed <= 110) return (short) (baseConsumption * 1.15);
        if(maxSpeed <= 120) return (short) (baseConsumption * 1.3);
        if(maxSpeed <= 130) return (short) (baseConsumption * 1.45);
        if(maxSpeed <= 140) return (short) (baseConsumption * 1.5);
        if(maxSpeed <= 150) return (short) (baseConsumption * 1.55);
        return (short) (baseConsumption * 1.6);
    }
}
