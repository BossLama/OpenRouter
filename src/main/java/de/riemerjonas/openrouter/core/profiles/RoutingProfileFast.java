package de.riemerjonas.openrouter.core.profiles;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.ifaces.IRoutingProfile;

public class RoutingProfileFast implements IRoutingProfile
{

    @Override
    public double getWeight(OpenRouterEdge edge)
    {
        short speed = edge.getMetaDataAsObject().getMaxSpeedMs();
        short distance = edge.getMetaDataAsObject().getDistanceMeter();
        return (short) Math.max(1, distance / speed);
    }

    @Override
    public String getName() {
        return "RoutingProfileFast";
    }
}
