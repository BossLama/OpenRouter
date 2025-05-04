package de.riemerjonas.openrouter.core.iface;

import de.riemerjonas.openrouter.core.OpenRouterEdge;

public interface OpenRouterRoutingProfile
{
    short getWeight(OpenRouterEdge edge);
}
