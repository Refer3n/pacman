package game.ghosts;

/**
 * Represents the different states a ghost can be in
 */
public enum GhostState {
    IN_HOME,        // Ghost is in the ghost house
    LEAVING_HOME,   // Ghost is exiting the ghost house
    CHASE,          // Ghost is actively chasing Pacman
    SCATTER,        // Ghost is moving to its scatter corner
    FRIGHTENED      // Ghost is blue and fleeing from Pacman (after power pellet)
}
