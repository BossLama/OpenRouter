package de.riemerjonas.openrouter.core.ifaces;

import de.riemerjonas.openrouter.core.OpenRouterEdge;

public interface IRoutingProfile {

    double getWeight(OpenRouterEdge edge);
    String getName();
}
