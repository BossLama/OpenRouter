package de.riemerjonas.openrouter.core.profiles;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;

public class RoutingProfileFast implements OpenRouterRoutingProfile
{

    @Override
    public short getWeight(OpenRouterEdge edge)
    {
        return (short) (edge.getDistance() / (short)(edge.getMaxSpeed() / 3.6));
    }
}
