package de.riemerjonas.openrouter.core.profiles;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.ifaces.IRoutingProfile;

public class RoutingProfileShort implements IRoutingProfile
{

    @Override
    public double getWeight(OpenRouterEdge edge)
    {
        return edge.getMetaDataAsObject().getDistanceMeter();
    }

    @Override
    public String getName() {
        return "RoutingProfileShort";
    }
}
