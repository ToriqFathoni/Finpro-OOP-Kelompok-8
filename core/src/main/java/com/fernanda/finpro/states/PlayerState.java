package com.fernanda.finpro.states;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface PlayerState {
    void enter();
    void update(float dt);
    TextureRegion getCurrentFrame(float stateTime);
}
