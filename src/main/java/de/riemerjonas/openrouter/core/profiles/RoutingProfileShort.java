package de.riemerjonas.openrouter.core.profiles;

import de.riemerjonas.openrouter.core.OpenRouterEdge;
import de.riemerjonas.openrouter.core.iface.OpenRouterRoutingProfile;

public class RoutingProfileShort implements OpenRouterRoutingProfile
{

    @Override
    public short getWeight(OpenRouterEdge edge) {
        return edge.getDistance();
    }

}
